package com.tlz.debugger.handlers

import android.content.Context
import com.tlz.debugger.*
import com.tlz.debugger.models.InitInfo
import com.tlz.debugger.models.Db
import fi.iki.elonen.NanoHTTPD

/**
 * 初始化请求处理.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 15:18.
 */
class InitRequestHandler(
		private val ctx: Context,
		private val dataProvider: DataProvider,
		private val appManager: ApplicationManager
) : RequestHandler {

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			when (session.uri) {
				"/api/init" -> handleInitRequest()
				else -> null
			}

	/**
	 * 处理初始化请求.
	 * @return NanoHTTPD.Response
	 */
	private fun handleInitRequest(): NanoHTTPD.Response {
		val dbs = mutableListOf<Db>()
		dataProvider.getDatabaseList().forEach {
			executeSafely {
				val tabWrapper = dataProvider.getAllTable(it)
				dbs.add(Db(it, tabWrapper.verison, tabWrapper.tables))
			}
		}
		return appManager.getApplicationInfoByPkg(ctx.packageName)?.run {
			responseData(InitInfo(
					icon,
					name,
					pkg,
					verName,
					verCode,
					isSystemApp,
					size,
					"${DebuggerWebServer.serverAddress}/api/log",
					dbs
			).toResponse())
		} ?: responseError(errorMsg = "未获取到应用信息")
	}

}