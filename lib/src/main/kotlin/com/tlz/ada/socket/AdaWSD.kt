package com.tlz.ada.socket

import com.tlz.ada.Ada
import com.tlz.ada.models.Log
import org.nanohttpd.protocols.http.IHTTPSession
import org.nanohttpd.protocols.http.NanoHTTPD
import org.nanohttpd.protocols.http.response.Response
import org.nanohttpd.protocols.websockets.NanoWSD
import org.nanohttpd.protocols.websockets.WebSocket


/**
 * Created by Tomlezen.
 * Data: 2018/9/7.
 * Time: 11:25.
 */
class AdaWSD(port: Int) : NanoWSD(port) {

  private val active = mutableListOf<AdaWebSocket>()
  private val toAdd = mutableListOf<AdaWebSocket>()
  private val toRemove = mutableListOf<AdaWebSocket>()

  /**
   * 启动.
   * @param nanoHTTPD NanoHTTPD
   */
  fun start(nanoHTTPD: NanoHTTPD) {
    toAdd.clear()
    toRemove.clear()
    active.clear()
    Ada.submitTask {
      var nextTime = System.currentTimeMillis()
      while (nanoHTTPD.isAlive) {
        nextTime += 4000L
        while (System.currentTimeMillis() < nextTime) {
          try {
            Thread.sleep(nextTime - System.currentTimeMillis())
          } catch (ignored: InterruptedException) {
          }
        }
        synchronized(toAdd) {
          active.addAll(toAdd)
          toAdd.clear()
        }
        synchronized(toRemove) {
          active.removeAll(toRemove)
          toRemove.clear()
          for (ws in active) {
            try {
              ws.ping(pingPayload)
            } catch (e: Exception) {
              toRemove.add(ws)
            }
          }
        }
      }
    }
  }

  /**
   * 请求.
   * @param session IHTTPSession
   * @return AdaResponse?
   */
  fun onRequest(session: IHTTPSession): Response? =
      if (session.uri == "/api/log") handleWebSocket(session) else null

  override fun openWebSocket(handshake: IHTTPSession): WebSocket {
    val socket = AdaWebSocket(handshake)
    synchronized(toAdd) {
      if (!toAdd.contains(socket))
        toAdd.add(socket)
    }
    return socket
  }

  /**
   * 关闭socket.
   * @param webSocket DebuggerWebSocket
   */
  fun closeSocket(socket: AdaWebSocket) {
    synchronized(toRemove) {
      if (!toRemove.contains(socket))
        toRemove.add(socket)
    }
  }

  /**
   * 发送消息.
   * @param log String
   */
  fun send(log: String) {
    val logJson by lazy { Ada.adaGson.toJson(wrapLog(log.toLogObj())) }
    active.filter { it.isOpen }
        .forEach {
          runCatching { it.send(logJson) }
        }
  }

  private fun String.toLogObj() =
      when {
        contains("V/") -> Log("V", android.util.Log.VERBOSE, this, this)
        contains("D/") -> Log("D", android.util.Log.DEBUG, this, this)
        contains("I/") -> Log("I", android.util.Log.INFO, this, this)
        contains("W/") -> Log("W", android.util.Log.WARN, this, this)
        contains("E/") -> Log("E", android.util.Log.ERROR, this, this)
        else -> Log("A", android.util.Log.ASSERT, this, this)
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

  companion object {
    private val pingPayload = "1337DEADBEEFC001".toByteArray()
  }

}