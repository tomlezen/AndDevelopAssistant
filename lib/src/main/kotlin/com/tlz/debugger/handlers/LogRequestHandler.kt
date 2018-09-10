package com.tlz.debugger.handlers

import android.content.Context
import android.util.Log.*
import com.tlz.debugger.LogcatReader
import com.tlz.debugger.handleRequestSafely
import com.tlz.debugger.models.FileInfo
import com.tlz.debugger.models.Log
import com.tlz.debugger.responseData
import com.tlz.debugger.socket.DebuggerWSD
import com.tlz.debugger.toResponse
import fi.iki.elonen.NanoHTTPD
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
class LogRequestHandler(private val ctx: Context, private val wsd: DebuggerWSD) : RequestHandler {

	/** 日志文件，根据具体时间来生成. */
	private val logFileName by lazy {
		val format = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA)
		format.format(Date())
	}

	/** 日志缓存文件夹. */
	private val logCacheFolder by lazy {
		ctx.externalCacheDir.absolutePath + "/log/"
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

	private val logcatReader by lazy {
		LogcatReader {
			// 写入到文件中
			logFile.appendText("\n" + it)
			wsd.send(wrapLog(it.toLogObj()))
		}
	}

	private fun String.toLogObj() =
			when {
				contains("V/") -> Log("V", VERBOSE, this, this)
				contains("D/") -> Log("D", DEBUG, this, this)
				contains("I/") -> Log("I", INFO, this, this)
				contains("W/") -> Log("W", WARN, this, this)
				contains("E/") -> Log("E", ERROR, this, this)
				else -> Log("A", ASSERT, this, this)
			}

	/**
	 * 包装下.
	 * @return String
	 */
	private fun wrapLog(log: Log): Log {
		when (log.type) {
			"E" -> log.content = "<p style='color: #FF3030'>${log.content}</p>"
			"W" -> log.content = "<p style='color: #FA8072'>${log.content}</p>"
			else -> log.content = "<p>${log.content}</p>"
		}
		return log
	}

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			when (session.uri) {
				"/api/log/list" -> handleLogListRequest()
				else -> wsd.onRequest(session)?.also { logcatReader.start() }
			}

	/**
	 * 处理日志列表请求.
	 * @return NanoHTTPD.Response
	 */
	private fun handleLogListRequest(): NanoHTTPD.Response =
			handleRequestSafely {
				val files = mutableListOf<FileInfo>()
				val logCache = File(logCacheFolder)
				if (logCache.exists()) {
					logCache.listFiles()
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
}