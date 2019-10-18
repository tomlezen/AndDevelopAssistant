package com.tlz.ada.socket

import android.util.Log
import com.tlz.ada.Ada
import org.nanohttpd2.protocols.http.IHTTPSession
import org.nanohttpd2.protocols.websockets.CloseCode
import org.nanohttpd2.protocols.websockets.WebSocket
import org.nanohttpd2.protocols.websockets.WebSocketFrame
import java.io.IOException

/**
 * Created by Tomlezen.
 * Data: 2018/9/7.
 * Time: 11:27.
 */
class AdaWebSocket(handsShakeRequest: IHTTPSession) : WebSocket(handsShakeRequest) {

  /** ping次数. */
  private var pingCount = 0
  /** 被ping次数. */
  private var pongCount = 0

  private val connectSuccessLog by lazy {
    com.tlz.ada.models.Log(
        "I",
        Log.INFO,
        "----------------------------------连接成功----------------------------------\n",
        "----------------------------------连接成功----------------------------------\n"
    )
  }

	override fun onOpen() {
		send(Ada.adaGson.toJson(connectSuccessLog))
	}

	override fun onClose(code: CloseCode?, reason: String?, initiatedByRemote: Boolean) {
	}

	override fun onMessage(message: WebSocketFrame?) {
	}

	override fun onPong(pong: WebSocketFrame?) {
		pongCount++
	}

	override fun onException(exception: IOException?) {
//		Log.e(TAG, "web socket exception", exception)
	}

	override fun ping(payload: ByteArray?) {
		super.ping(payload)
		pingCount++
		if (pingCount - pongCount > 3) {
			close(CloseCode.GoingAway, "Missed too many ping requests.", false)
		}
	}

  companion object {
//		private const val TAG = "DebuggerWebSocket"
  }

}