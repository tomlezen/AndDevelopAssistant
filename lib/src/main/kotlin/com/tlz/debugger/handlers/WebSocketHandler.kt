package com.tlz.debugger.handlers

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException

/**
 * Created by Tomlezen.
 * Date: 2018/9/6.
 * Time: 下午9:47.
 */
class WebSocketHandler : NanoWSD(), RequestHandler {

	override fun openWebSocket(handshake: NanoHTTPD.IHTTPSession): WebSocket =
			DebuggerWebSocket(handshake)

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			if (session.uri == "/api/log/") serve(session) else null

	class DebuggerWebSocket(handshakeRequest: NanoHTTPD.IHTTPSession) :
			NanoWSD.WebSocket(handshakeRequest) {

		override fun onOpen() {
			Log.d(TAG, "onOpen")
		}

		override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode?, reason: String?, initiatedByRemote: Boolean) {
			Log.d(TAG, "onClose")
		}

		override fun onMessage(message: NanoWSD.WebSocketFrame?) {
			Log.d(TAG, "onMessage")
		}

		override fun onPong(pong: NanoWSD.WebSocketFrame?) {
			Log.d(TAG, "onPong")
		}

		override fun onException(exception: IOException?) {
			Log.d(TAG, "onException")
		}
	}

	companion object {
		private val TAG = WebSocketHandler::class.java.canonicalName
	}
}