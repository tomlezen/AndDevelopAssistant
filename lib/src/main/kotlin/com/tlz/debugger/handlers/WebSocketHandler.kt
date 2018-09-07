package com.tlz.debugger.handlers

import com.tlz.debugger.LogcatReader
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
			wsd.send(wrapLog(it))
		}
	}

	/**
	 * 包装下.
	 * @return String
	 */
	private fun wrapLog(str: String): String {
		if (str.contains("E/")) {
			return "<p style='color: #FF3030'>$str</p>"
		}
		if (str.contains("W/")) {
			return "<p style='color: #FA8072'>$str</p>"
		}
		return str
	}

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			wsd.onRequest(session)?.also { logcatReader.start() }
}