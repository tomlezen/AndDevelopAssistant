package com.tlz.ada


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

	/**
	 * 收集异常日志.
	 * @param e Throwable
	 */
//	private fun collectExceptionLog(e: Throwable){
//		val sb = StringBuffer("\n")
//		val writer = StringWriter()
//		val printWriter = PrintWriter(writer)
//		e.printStackTrace(printWriter)
//		var cause = e.cause
//		while (cause != null) {
//			cause.printStackTrace(printWriter)
//			cause = cause.cause
//		}
//		printWriter.close()
//		val result = writer.toString()
//		sb.append(result)
//		onLogcat(sb.toString())
//	}

	@Synchronized
	fun start() {
		// 避免重复开启.
		if (!isRunning) {
//			val defUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
//			Thread.setDefaultUncaughtExceptionHandler { t, e ->
//				collectExceptionLog(e)
//				defUncaughtExceptionHandler.uncaughtException(t, e)
//			}
			Thread {
				executeSafely {
					p = cmd("logcat -v time") { onLogcat(it) }
				}
			}.start()
		}
	}

//	fun stop() {
//		p?.destroy()
//	}

}