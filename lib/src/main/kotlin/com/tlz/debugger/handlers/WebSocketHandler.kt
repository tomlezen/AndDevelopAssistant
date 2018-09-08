package com.tlz.debugger.handlers

import android.util.Log.*
import com.tlz.debugger.LogcatReader
import com.tlz.debugger.models.Log
import com.tlz.debugger.socket.DebuggerWSD
import fi.iki.elonen.NanoHTTPD

/**
 * Created by Tomlezen.
 * Date: 2018/9/6.
 * Time: 下午9:47.
 */
class WebSocketHandler(private val wsd: DebuggerWSD) : RequestHandler {

	private val logcatReader by lazy {
		LogcatReader {
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
		}
		return log
	}

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			wsd.onRequest(session)?.also { logcatReader.start() }
}