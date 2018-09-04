package com.tlz.debugger

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Apk提取器.
 *
 * Created by Tomlezen.
 * Data: 2018/9/4.
 * Time: 11:38.
 */
class ApkExtractor(private val ctx: Context) {

	fun extractorApk() {
//		val process = Runtime.getRuntime().exec("pm path ${ctx.packageName}")
		val process = Runtime.getRuntime().exec("pm list packages")
		process.waitFor()
		val read = BufferedReader(InputStreamReader(process.inputStream))
		var line: String? = read.readLine()
		while (line != null) {
			Log.d("结果", "获取结果: $line")
			line = read.readLine()
		}
		read.close()
	}

}