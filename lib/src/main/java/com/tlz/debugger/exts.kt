package com.tlz.debugger

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Created by Tomlezen.
 * Date: 2018/2/8.
 * Time: 下午9:16.
 */

fun Context.readHtmlFIle(fileName: String): String{
	try {
		return BufferedReader(InputStreamReader(this.assets.open("web$fileName"), "UTF-8")).readText()
	}catch (e: Exception){
		e.printStackTrace()
	}
	return ""
}