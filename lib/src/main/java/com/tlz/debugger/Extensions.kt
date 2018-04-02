package com.tlz.debugger

import android.content.Context
import android.content.pm.PackageManager
import com.tlz.debugger.model.Response

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 15:56.
 */

/**
 * 获取matedata数据.
 */
internal fun Context.metaData(key: String): String = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)?.metaData?.getString(key) ?: ""

internal fun Context.metaDataInt(key: String, default: Int = 0): Int = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)?.metaData?.getInt(key, default) ?: default

/**
 * 读取html文件.
 */
internal fun String.readHtml(ctx: Context): String = ctx.assets.open("web2018_3_15$this").bufferedReader().readText()

/**
 * 执行代码并捕捉异常
 */
internal fun executeSafely(action: () -> Unit): Boolean {
  return try {
    action.invoke()
    true
  } catch (t: Throwable) {
    t.printStackTrace()
    false
  }
}

/**
 * 执行代码并捕捉异常
 */
internal fun executeSafely(action: () -> Unit, exceptionWithAction: ((Throwable) -> Boolean)? = null): Boolean {
  return try {
    action.invoke()
    true
  } catch (t: Throwable) {
    t.printStackTrace()
    exceptionWithAction?.invoke(t) ?: false
  }
}

internal fun Any.toResponse(): Response = Response(data = this)