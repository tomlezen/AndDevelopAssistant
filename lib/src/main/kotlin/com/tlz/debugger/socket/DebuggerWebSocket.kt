package com.tlz.debugger.socket

import android.util.Log
import com.tlz.debugger.gson
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException

/**
 * Created by Tomlezen.
 * Data: 2018/9/7.
 * Time: 11:27.
 */
class DebuggerWebSocket(handsShakeRequest: NanoHTTPD.IHTTPSession) : NanoWSD.WebSocket(handsShakeRequest) {

	/** ping次数. */
	private var pingCount = 0
	/** 被ping次数. */
	private var pongCount = 0

	private val connectSuccessLog by lazy {
		com.tlz.debugger.models.Log(
				"I",
				Log.INFO,
				"----------------------------------连接成功----------------------------------\n",
				"----------------------------------连接成功----------------------------------\n"
		)
	}

	override fun onOpen() {
//		Log.d(TAG, "web socket open")
		send(gson.toJson(connectSuccessLog))
	}

	override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode?, reason: String?, initiatedByRemote: Boolean) {
//		Log.d(TAG, "web socket close, reason: $reason")
	}

	override fun onMessage(message: NanoWSD.WebSocketFrame?) {
//		Log.d(TAG, "web socket message: $message")
	}

	override fun onPong(pong: NanoWSD.WebSocketFrame?) {
//		Log.d(TAG, "web socket pong")
		pongCount++
	}

	override fun onException(exception: IOException?) {
		Log.e(TAG, "web socket exception", exception)
	}

	override fun ping(payload: ByteArray?) {
		super.ping(payload)
//		Log.d(TAG, "web socket ping")
		pingCount++
		if (pingCount - pingCount > 3) {
			close(NanoWSD.WebSocketFrame.CloseCode.GoingAway, "Missed too many ping requests.", false)
		}
	}

	companion object {
		private const val TAG = "DebuggerWebSocket"
	}

}