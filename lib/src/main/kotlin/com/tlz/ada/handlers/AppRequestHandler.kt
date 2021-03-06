package com.tlz.ada.handlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.tlz.ada.*
import com.tlz.ada.AdaConstUtils.FILTER
import com.tlz.ada.AdaConstUtils.PAGE_INDEX
import com.tlz.ada.AdaConstUtils.PAGE_SIZE
import com.tlz.ada.AdaConstUtils.PKG
import com.tlz.ada.AdaConstUtils.SEARCH
import com.tlz.ada.models.AdaResponse
import org.nanohttpd2.protocols.http.IHTTPSession
import org.nanohttpd2.protocols.http.response.Response
import org.nanohttpd2.protocols.http.response.Response.newChunkedResponse
import org.nanohttpd2.protocols.http.response.Status
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
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
        private val appManager: AdaApplicationManager
) : RequestHandler {
    override fun onRequest(session: IHTTPSession): Response? =
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
     * @param session IHTTPSession
     * @return AdaResponse
     */
    private fun handleAppListRequest(session: IHTTPSession): Response =
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
                }.filter { search.isEmpty() || it.name.toUpperCase(Locale.getDefault()).contains(search.toUpperCase(Locale.getDefault())) }
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
                responseData(AdaResponse(data = resultData, total = data.size))
            }

    /**
     * 处理应用信息请求.
     * @param session IHTTPSession
     * @return AdaResponse
     */
    private fun handleAppInfoRequest(session: IHTTPSession): Response =
            handleRequestSafely {
                responseData(AdaResponse(data = appManager.getApplicationInfoByPkg(session.parms[PKG]
                        ?: "")))
            }

    /**
     * 处理应用安装请求.
     * @param session IHTTPSession
     * @return AdaResponse
     */
    private fun handleAppInstallRequest(session: IHTTPSession): Response =
            handleRequestSafely {
                val apkFilePath = ctx.externalCacheDir?.absolutePath + "/install_apk.apk"
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
     * @param session IHTTPSession
     * @return AdaResponse
     */
    private fun handleAppDownloadRequest(session: IHTTPSession): Response =
            handleRequestSafely {
                val pkg = session.parms["pkg"] ?: ""
                val appInfo = appManager.getApplicationInfoByPkg(pkg)
                if (appInfo?.path.isNullOrEmpty()) {
                    responseError(errorMsg = "不存在该应用")
                } else {
                    newChunkedResponse(Status.OK, "application/octet-stream", File(appInfo!!.path).inputStream()).apply {
                        addHeader("Content-Disposition", "attachment; filename=${appInfo.name}.apk")
                    }
                }
            }

    /**
     * 处理应用logo请求.
     * @param session IHTTPSession
     * @return AdaResponse
     */
    private fun handleAppIconRequest(session: IHTTPSession): Response =
            handleRequestSafely {
                val pkg = session.parms["pkg"] ?: ""
                val os = ByteArrayOutputStream()
                val bitmapDrawable = appManager.getApplicationInfoByPkg(pkg)?.applicationInfo?.loadIcon(ctx.packageManager) as? BitmapDrawable
                bitmapDrawable?.bitmap?.compress(Bitmap.CompressFormat.PNG, 100, os)
                val ins = ByteArrayInputStream(os.toByteArray())
                newChunkedResponse(Status.OK, "image/png", ins)
            }
}