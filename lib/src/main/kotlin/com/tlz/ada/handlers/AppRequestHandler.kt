package com.tlz.ada.handlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.tlz.ada.ApplicationManager
import com.tlz.ada.ConstUtils.FILTER
import com.tlz.ada.ConstUtils.PAGE_INDEX
import com.tlz.ada.ConstUtils.PAGE_SIZE
import com.tlz.ada.ConstUtils.PKG
import com.tlz.ada.ConstUtils.SEARCH
import com.tlz.ada.handleRequestSafely
import com.tlz.ada.models.Response
import com.tlz.ada.verifyParams
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min

/**
 * 应用请求处理.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 16:04.
 */
class AppRequestHandler(
		private val ctx: Context,
		private val appManager: ApplicationManager
) : RequestHandler {
	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			when (session.uri) {
				"/api/app/list" -> session.verifyParams(::handleAppListRequest, PAGE_SIZE, PAGE_SIZE, FILTER)
				"/api/app/info" -> session.verifyParams(::handleAppInfoRequest, PKG)
				"/api/app/install" -> session.verifyParams(::handleAppInstallRequest)
				"/api/app/download" -> session.verifyParams(::handleAppDownloadRequest, PKG)
				"/api/app/icon" -> session.verifyParams(::handleAppIconRequest, PKG)
				else -> null
			}

	/**
	 * 处理应用列表请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleAppListRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val pageIndex = session.parms[PAGE_INDEX]?.toInt() ?: 1
				val pageSize = session.parms[PAGE_SIZE]?.toInt() ?: 10
				val search = session.parms[SEARCH] ?: ""
				val filter = session.parms[FILTER] ?: "all"
				var sortDir: String? = null
				var sortField: String? = null
				session.parms.forEach {
					if (it.value == "asc" || it.value == "desc") {
						sortField = it.key
						sortDir = it.value
						return@forEach
					}
				}

				val filteredData = when (filter) {
					"system" -> appManager.applicationList.filter { it.isSystemApp }
					"non-system" -> appManager.applicationList.filter { !it.isSystemApp }
					else -> appManager.applicationList
				}.filter { search.isEmpty() || it.name.toUpperCase().contains(search.toUpperCase()) }
				val data = if (sortDir != null && sortField != null) {
					if (sortDir == "asc") {
						filteredData.sortedBy {
							when (sortField) {
								"name" -> it.name
								"size" -> it.size.toString()
								"pkg" -> it.pkg
								else -> 1.toString()
							}
						}
					} else {
						filteredData.sortedByDescending {
							when (sortField) {
								"name" -> it.name
								"size" -> it.size.toString()
								"pkg" -> it.pkg
								else -> 1.toString()
							}
						}
					}
				} else filteredData
				val resultData = data.subList((pageIndex - 1) * pageSize, min(pageIndex * pageSize, data.size))
				responseData(Response(data = resultData, total = data.size))
			}

	/**
	 * 处理应用信息请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleAppInfoRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				responseData(com.tlz.ada.models.Response(data = appManager.getApplicationInfoByPkg(session.parms[PKG]
						?: "")))
			}

	/**
	 * 处理应用安装请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleAppInstallRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val apkFilePath = ctx.externalCacheDir.absolutePath + "/install_apk.apk"
				val apkFile = File(apkFilePath)
				if (apkFile.exists()) {
					apkFile.delete()
				}
				val parseBody = hashMapOf<String, String>()
				session.parseBody(parseBody)
				if (parseBody.keys.contains("file")) {
					File(parseBody["file"]).renameTo(apkFile)
				}
				ctx.installApk(apkFile)
				responseData("success".toResponse())
			}

	/**
	 * 处理应用下载请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleAppDownloadRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val pkg = session.parms["pkg"] ?: ""
				val appInfo = appManager.getApplicationInfoByPkg(pkg)
				if (appInfo?.path.isNullOrEmpty()) {
					responseError(errorMsg = "不存在该应用")
				} else {
					NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "application/octet-stream", File(appInfo!!.path).inputStream()).apply {
						addHeader("Content-Disposition", "attachment; filename=${appInfo.name}.apk")
					}
				}
			}

	/**
	 * 处理应用logo请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response
	 */
	private fun handleAppIconRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val pkg = session.parms["pkg"]
				val os = ByteArrayOutputStream()
				val bitmapDrawable = appManager.getApplicationInfoByPkg(pkg!!)!!.applicationInfo.loadIcon(ctx.packageManager) as BitmapDrawable
				bitmapDrawable.bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
				val ins = ByteArrayInputStream(os.toByteArray())
				NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/png", ins)
			}
}