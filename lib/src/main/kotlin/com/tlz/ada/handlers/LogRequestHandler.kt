package com.tlz.ada.handlers

import android.content.Context
import com.tlz.ada.*
import com.tlz.ada.models.FileInfo
import com.tlz.ada.socket.AdaWSD
import org.nanohttpd2.protocols.http.IHTTPSession
import org.nanohttpd2.protocols.http.response.Response
import org.nanohttpd2.protocols.http.response.Response.newChunkedResponse
import org.nanohttpd2.protocols.http.response.Status
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志处理.
 *
 * Created by Tomlezen.
 * Date: 2018/9/6.
 * Time: 下午9:47.
 */
class LogRequestHandler(
    private val ctx: Context,
    private val wsd: AdaWSD
) : RequestHandler {

  /** 日志文件，根据具体时间来生成. */
  private val logFileName by lazy {
    val format = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
    format.format(Date())
  }

  /** 日志缓存文件夹. */
  private val logCacheFolder by lazy {
    ctx.externalCacheDir?.absolutePath + "/log/"
  }

  /** 每次初始化都新建一个日志文件. */
  private val logFile by lazy {
    File(logCacheFolder, "$logFileName.txt").apply {
      if (!exists()) {
        parentFile.mkdirs()
        createNewFile()
      }
    }
  }

  init {
    // 启动日志记录功能
    Ada.submitTask {
      cmd("logcat -v time") {
        // 写入到文件中
        logFile.appendText("\n" + it)
        wsd.send(it)
      }
    }
  }

  override fun onRequest(session: IHTTPSession): Response? =
      when (session.uri) {
        "/api/log/list" -> handleLogListRequest()
        "/api/log/delete" -> session.verifyParams(::handleLogDeleteRequest, AdaConstUtils.FILES)
        "/api/log/download" -> session.verifyParams(::handleLogDownloadRequest, AdaConstUtils.FILE_NAME)
        else -> wsd.onRequest(session)
      }

  /**
   * 处理日志列表请求.
   * @return AdaResponse
   */
  private fun handleLogListRequest(): Response =
      handleRequestSafely {
        val files = mutableListOf<FileInfo>()
        val logCache = File(logCacheFolder)
        if (logCache.exists()) {
          logCache.listFiles()
              .sortedByDescending { it.lastModified() }
              .filter { !it.isDirectory }
              .mapTo(files) {
                FileInfo(
                    it.name,
                    it.isDirectory,
                    it.absolutePath,
                    it.length(),
                    it.canRead(),
                    it.canWrite(),
                    it.isHidden,
                    it.lastModified()
                )
              }
        }
        responseData(files.toResponse())
      }

  /**
   * 处理日志删除请求.
   * @param session: IHTTPSession
   */
  private fun handleLogDeleteRequest(session: IHTTPSession): Response =
      handleRequestSafely {
        val files = Ada.adaGson.fromJson<Array<String>>(session.parms["files"]
            ?: "{}", Array<String>::class.java)
        File(logCacheFolder).listFiles()
            .filter { it.name in files }
            .forEach {
              it.delete()
            }
        handleLogListRequest()
      }

  /**
   * 处理日志文件下载请求.
   * @param session IHTTPSession
   * @return AdaResponse
   */
  private fun handleLogDownloadRequest(session: IHTTPSession): Response =
      handleRequestSafely {
        val fileName = session.parms[AdaConstUtils.FILE_NAME] ?: ""
        val file = File(logCacheFolder, fileName)
        if (!file.exists()) {
          responseError(errorMsg = "文件不存在")
        } else if (!file.canRead()) {
          responseError(errorMsg = "文件不可读取")
        } else {
          newChunkedResponse(Status.OK, "*/*", file.inputStream()).apply {
            addHeader("Content-Disposition", "attachment; filename=${file.name}")
          }
        }
      }
}