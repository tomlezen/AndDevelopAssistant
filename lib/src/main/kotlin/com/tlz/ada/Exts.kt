package com.tlz.ada

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tlz.ada.models.FileInfo
import com.tlz.ada.models.Response
import fi.iki.elonen.NanoHTTPD
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


val gson: Gson by lazy { GsonBuilder().create() }

fun rMin(v1: Int, v2: Int): Int {
	return if (v2 == -1 || v2 > v1) v1 else v2
}


/**
 * 获取matedata数据.
 * @receiver Context
 * @param key String
 * @return String
 */
internal fun Context.metaData(key: String): String = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)?.metaData?.getString(key)
		?: ""

internal fun Context.metaDataInt(key: String, default: Int = 0): Int = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)?.metaData?.getInt(key, default)
		?: default

/**
 * 读取html文件.
 * @receiver String
 * @param ctx Context
 * @return String
 */
internal fun String.readHtml(ctx: Context): String = ctx.assets.open("web$this").bufferedReader().readText()

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
 * 执行cmd命令.
 * @param cmd String
 * @return List<String>
 */
internal fun cmd(cmd: String): List<String> {
	val p = Runtime.getRuntime().exec(cmd)
	p.waitFor()
	val read = BufferedReader(InputStreamReader(p.inputStream))
	val lines = mutableListOf<String>()
	var line: String? = read.readLine()
	while (line != null) {
		lines.add(line)
		line = read.readLine()
	}
	read.close()
	return lines
}

/**
 * 执行cmd命令.
 * @param cmd String
 * @param onResult (List<String>) -> Unit
 * @return Process
 */
internal fun cmd(cmd: String, onResult: (String) -> Unit): Process {
	val p = Runtime.getRuntime().exec(cmd)
	val read = BufferedReader(InputStreamReader(p.inputStream))
	var line: String? = read.readLine()
	while (line != null) {
		onResult.invoke(line)
		line = read.readLine()
	}
	read.close()
	return p
}

/**
 * 安装应用.
 * @receiver Context
 * @param apkFile File
 */
internal fun Context.installApk(apkFile: File) {
	val install = Intent(Intent.ACTION_VIEW)
	install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		val apkUri = FileProvider.getUriForFile(this, "$packageName.AndDevelopAssistantFileProvider", apkFile)
		install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		install.setDataAndType(apkUri, "application/vnd.android.package-archive");
	} else {
		install.setDataAndType(Uri.parse("file://" + apkFile.absolutePath), "application/vnd.android.package-archive");
	}
	startActivity(install)
}

/**
 * 获取目录下的所有文件.
 * @receiver String
 * @return List<FileInfo>
 */
fun String.listFiles(): List<FileInfo> {
	val dirContent = mutableListOf<FileInfo>()
	val dirFile = File(this)
	if (dirFile.exists() && dirFile.canRead()) {
		dirFile.listFiles()?.mapTo(dirContent) {
			FileInfo(
					it.name,
					it.isDirectory,
					it.absolutePath,
					it.length(),
					it.canRead(),
					it.canWrite(),
					it.isHidden,
					it.lastModified()
			)
		}
	}
	return dirContent
}

fun response(type: String, html: String, cacheTime: String): NanoHTTPD.Response =
		NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, type, html).apply {
			addHeader("Cache-Control", "public")
			addHeader("Cache-Control", "max-age=$cacheTime")
		}

fun response(type: String, html: String): NanoHTTPD.Response =
		NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, type, html)

fun responseData(data: Any): NanoHTTPD.Response =
		NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, gson.toJson(data))

fun responseHtml(html: String): NanoHTTPD.Response =
		NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, html)

fun responseError(status: Int = 401, errorMsg: String): NanoHTTPD.Response =
		responseData(com.tlz.ada.models.Response(status, null, errorMsg))

internal fun Any.toResponse(): Response = Response(data = this)

/**
 * 安全处理请求.
 * @param errorMsg String?
 * @param action () -> NanoHTTPD.Response
 * @return NanoHTTPD.Response
 */
internal fun handleRequestSafely(errorMsg: String? = null, action: () -> NanoHTTPD.Response): NanoHTTPD.Response =
		try {
			action.invoke()
		} catch (t: Throwable) {
			t.printStackTrace()
			responseError(errorMsg = errorMsg ?: "数据处理出错${t.message}")
		}

/**
 * 校验参数.
 * @receiver Map<String, String>
 * @param params Array<out String>
 */
fun NanoHTTPD.IHTTPSession.verifyParams(doOnPass: (NanoHTTPD.IHTTPSession) -> NanoHTTPD.Response, vararg params: String):
		NanoHTTPD.Response {
	return if (params.any { !this.parms.containsKey(it) }) {
		NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "请求参数错误")
	} else {
		doOnPass(this)
	}
}

/**
 * 检查权限是否通过.
 * @receiver Context
 * @param permissions Array<out String>
 * @return Boolean
 */
fun Context.isPermissionsGranted(vararg permissions: String): Boolean =
		permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }