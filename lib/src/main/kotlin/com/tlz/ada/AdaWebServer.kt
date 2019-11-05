package com.tlz.ada

import android.Manifest
import android.arch.persistence.db.SupportSQLiteDatabase
import android.content.Context
import android.util.Log
import android.util.Pair
import com.tlz.ada.db.AdaDataProvider
import com.tlz.ada.db.AdaDataProviderImpl
import com.tlz.ada.handlers.*
import com.tlz.ada.socket.AdaWSD
import org.nanohttpd2.protocols.http.IHTTPSession
import org.nanohttpd2.protocols.http.NanoHTTPD
import org.nanohttpd2.protocols.http.response.Response
import org.nanohttpd2.protocols.http.response.Response.newFixedLengthResponse
import org.nanohttpd2.protocols.http.response.Status
import java.io.File


/**
 * 服务器.
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 15:00.
 */
class AdaWebServer internal constructor(internal val ctx: Context, port: Int) : NanoHTTPD(port) {

  private val dataProvider: AdaDataProvider by lazy { AdaDataProviderImpl(ctx) }
  private val appManager by lazy { AdaApplicationManager(ctx) }
  private val activityLifeCycleHooker by lazy { AdaActivityLifeCycleListener(ctx) }

  /** 所有请求处理器. */
  private val handlers = mutableListOf<RequestHandler>()

  /** 服务器地址. */
  var serverAddress: String = ""
    private set

  /** 文件读写权限是否通过. */
  var filePermissionGranted = false
    get() {
      if (!field) {
        field = ctx.isPermissionsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
      }
      return field
    }

  init {
    serverAddress = "${AdaProvider.getPhoneIp()}:$port"
    activityLifeCycleHooker.install()
    tempFileManagerFactory = AdaTempFileManagerFactory(ctx)
    Ada.submitTask {
      runCatching {
        // 注册各种处理器
        val wsd = AdaWSD(port)
        handlers.add(LogRequestHandler(ctx, wsd))
        handlers.add(InitRequestHandler(ctx, dataProvider, appManager))
        handlers.add(DbRequestHandler(dataProvider))
        handlers.add(AppRequestHandler(ctx, appManager))
        handlers.add(FileRequestHandler(this))
        handlers.add(ScreenShotHandler(activityLifeCycleHooker))
        handlers.add(DefaultRequestHandler(ctx))

        start(10000, false)
        wsd.start(this)
        Log.e(TAG, "address: $serverAddress")
      }.onFailure {
        Log.e(TAG, "Android调试辅助初始化失败")
      }
    }
  }

  /**
   * 设置自定义数据库.
   * @param files Map<String, Pair<File, String>>
   */
  fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
    if (files.isEmpty()) return
    val newFiles = mutableMapOf<String, kotlin.Pair<File, String>>()
    files.forEach {
      newFiles[it.key] = it.value.first to it.value.second
    }
    dataProvider.setCustomDatabaseFiles(newFiles)
  }

  /**
   * 设置内存数据库.
   * @param databases Map<String, SupportSQLiteDatabase>
   */
  fun setInMemoryRoomDatabases(databases: Map<String, SupportSQLiteDatabase>) {
    if (databases.isEmpty()) return
    dataProvider.setInMemoryRoomDatabases(databases)
  }

  override fun serve(session: IHTTPSession?): Response {
    session?.run {
      val startTimeMillis = System.currentTimeMillis()
      handlers.forEach {
        it.onRequest(session)?.let { resp ->
          if (BuildConfig.DEBUG) {
            Log.i(TAG, "${session.uri} response time ${System.currentTimeMillis() - startTimeMillis}")
          }
          return resp
        }
      }
    }
    return newFixedLengthResponse(Status.FORBIDDEN, MIME_PLAINTEXT, "不支持该请求")
  }

  companion object {
    const val TAG = "AdaWebServer"
  }

}