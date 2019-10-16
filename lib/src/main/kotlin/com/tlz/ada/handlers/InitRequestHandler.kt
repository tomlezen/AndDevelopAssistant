package com.tlz.ada.handlers

import android.content.Context
import com.tlz.ada.*
import com.tlz.ada.db.AdaDataProvider
import com.tlz.ada.models.Db
import com.tlz.ada.models.InitInfo
import org.nanohttpd.protocols.http.IHTTPSession
import org.nanohttpd.protocols.http.response.Response

/**
 * 初始化请求处理.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 15:18.
 */
class InitRequestHandler(
    private val ctx: Context,
    private val dataProvider: AdaDataProvider,
    private val appManager: AdaApplicationManager
) : RequestHandler {

  override fun onRequest(session: IHTTPSession): Response? =
      when (session.uri) {
        "/api/init" -> handleInitRequest()
        else -> null
      }

  /**
   * 处理初始化请求.
   * @return AdaResponse
   */
  private fun handleInitRequest(): Response {
    val dbs = mutableListOf<Db>()
    dataProvider.getAllDatabase().forEach {
      runCatching {
        val tabWrapper = dataProvider.getAllTable(it)
        dbs.add(Db(it, tabWrapper.version, tabWrapper.tableInfos))
      }
    }
    return appManager.getApplicationInfoByPkg(ctx.packageName)?.run {
      responseData(
          InitInfo(
              icon,
              name,
              pkg,
              verName,
              verCode,
              isSystemApp,
              size,
              "${Ada.adaWebServer.serverAddress}/api/log",
              dbs
          ).toResponse())
    } ?: responseError(errorMsg = "未获取到应用信息")
  }

}