package com.tlz.debugger

/**
 * 日志读取.
 *
 * Created by Tomlezen.
 * Data: 2018/9/7.
 * Time: 12:46.
 */
class LogcatReader(private val onLogcat: (String) -> Unit) {

	private var p: Process? = null
	private val isRunning: Boolean
		get() {
			return try {
				p?.exitValue()
				false
			} catch (e: IllegalThreadStateException) {
				true
			}
		}

	fun start() {
		// 避免重复开启.
		if (!isRunning) {
			Thread {
				executeSafely {
					p = cmd("logcat -v time") { onLogcat(it) }
//					p = cmd("logcat -v time *:w") { onLogcat(it) }
				}
			}.start()
		}
	}

	fun stop() {
		p?.destroy()
	}

}