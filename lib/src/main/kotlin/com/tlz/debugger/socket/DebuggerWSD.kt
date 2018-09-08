package com.tlz.debugger.socket

import com.tlz.debugger.gson
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD


/**
 * Created by Tomlezen.
 * Data: 2018/9/7.
 * Time: 11:25.
 */
class DebuggerWSD : NanoWSD() {

	private val active = mutableListOf<DebuggerWebSocket>()
	private val toAdd = mutableListOf<DebuggerWebSocket>()
	private val toRemove = mutableListOf<DebuggerWebSocket>()

	private var wsPinger: Thread? = null

	/**
	 * 启动.
	 * @param nanoHTTPD NanoHTTPD
	 */
	fun start(nanoHTTPD: NanoHTTPD) {
		toAdd.clear()
		toRemove.clear()
		active.clear()
		wsPinger = Thread {
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
		wsPinger?.isDaemon = true
		wsPinger?.start()
	}

	/**
	 * 请求.
	 * @param session NanoHTTPD.IHTTPSession
	 * @return NanoHTTPD.Response?
	 */
	fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			if (session.uri == "/api/log") serve(session) else null

	override fun openWebSocket(handshake: NanoHTTPD.IHTTPSession): WebSocket {
		val socket = DebuggerWebSocket(handshake)
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
	fun closeSocket(socket: DebuggerWebSocket) {
		synchronized(toRemove) {
			if (!toRemove.contains(socket))
				toRemove.add(socket)
		}
	}

	/**
	 * 发送消息.
	 * @param message String
	 */
	fun send(log: com.tlz.debugger.models.Log) {
		active.filter { it.isOpen }
				.forEach {
					try {
						it.send(gson.toJson(log))
					} catch (e: Exception) {
					}
				}
	}

	companion object {
		private val pingPayload = "1337DEADBEEFC001".toByteArray()
		private const val TAG = "DebuggerWSD"
	}

}