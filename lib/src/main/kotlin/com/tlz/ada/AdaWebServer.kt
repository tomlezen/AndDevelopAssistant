package com.tlz.ada

import android.Manifest
import android.content.Context
import android.util.Log
import android.util.Pair
import com.tlz.ada.db.AdaDataProvider
import com.tlz.ada.db.AdaDataProviderImpl
import com.tlz.ada.handlers.*
import com.tlz.ada.socket.AdaWSD
import fi.iki.elonen.NanoHTTPD
import java.io.File


/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 15:00.
 */
class AdaWebServer internal constructor(internal val ctx: Context, port: Int) : NanoHTTPD(port) {

  private val dataProvider: AdaDataProvider by lazy { AdaDataProviderImpl(ctx) }
  private val appManager by lazy { ApplicationManager(ctx) }
  private val activityLifeCycleHooker by lazy { ActivityLifeCycleListener(ctx) }

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
    activityLifeCycleHooker.install()
    tempFileManagerFactory = AdaTempFileManagerFactory(ctx)
    serverAddress = "${AdaProvider.getPhoneIp()}:$port"
    Ada.adaExcutorService.submit {
      runCatching {
        // 为了加快应用列表api的访问速度，先加载所有的应用再启动服务器
        appManager.readApplicationList()
        // 注册各种处理器
        val wsd = AdaWSD()
        handlers.add(LogRequestHandler(ctx, wsd))
        handlers.add(InitRequestHandler(ctx, dataProvider, appManager))
        handlers.add(DbRequestHandler(dataProvider))
        handlers.add(AppRequestHandler(ctx, appManager))
        handlers.add(FileRequestHandler(this))
        handlers.add(ScreenShotHandler(activityLifeCycleHooker))
        handlers.add(DefaultRequestHandler(ctx))

        start(10000)
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

  override fun serve(session: IHTTPSession?): Response {
    session?.run {
      handlers.forEach {
        it.onRequest(session)?.let { resp ->
          return resp
        }
      }
    }
    return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "不支持该请求")
  }

  companion object {
    const val TAG = "AdaWebServer"
  }

}