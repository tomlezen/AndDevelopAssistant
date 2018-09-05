package com.tlz.debugger.handlers

import android.os.Environment
import com.tlz.debugger.ConstUtils.PATH
import com.tlz.debugger.handleRequestSafely
import com.tlz.debugger.models.FileInfo
import com.tlz.debugger.responseData
import com.tlz.debugger.toResponse
import com.tlz.debugger.verifyParams
import fi.iki.elonen.NanoHTTPD
import java.io.File

/**
 * 文件请求处理.
 *
 * Created by Tomlezen.≥
 * Data: 2018/9/5.
 * Time: 17:54.
 */
class FileRequestHandler : RequestHandler {

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
		when(session.uri){
			"/api/file/list" -> session.verifyParams(::handleFileListRequest, PATH)
			else -> null
		}

	/**
	 * 处理文件列表请求.
	 * @param session NanoHTTPD.IHTTPSession
	 */
	private fun handleFileListRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
			handleRequestSafely {
				val path = session.parms[PATH] ?: ""
				responseData(listFiles(if(path == "root") Environment.getExternalStorageDirectory().absolutePath else path).toResponse())
			}

	/**
	 * 获取目录下的所有文件.
	 * @param path String
	 */
	private fun listFiles(path: String): List<FileInfo>{
		val dirContent = mutableListOf<FileInfo>()
		val dirFile = File(path)
		if(dirFile.exists() && dirFile.canRead()){
			dirFile.listFiles()?.mapTo(dirContent){
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
}