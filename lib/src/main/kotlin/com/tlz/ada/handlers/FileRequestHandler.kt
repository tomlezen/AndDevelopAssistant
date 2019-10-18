package com.tlz.ada.handlers

import android.os.Environment
import com.tlz.ada.*
import com.tlz.ada.AdaConstUtils.FILE_NAME
import com.tlz.ada.AdaConstUtils.FOLDER_NAME
import com.tlz.ada.AdaConstUtils.PATH
import org.nanohttpd2.protocols.http.IHTTPSession
import org.nanohttpd2.protocols.http.response.Response
import org.nanohttpd2.protocols.http.response.Response.newChunkedResponse
import org.nanohttpd2.protocols.http.response.Status
import java.io.File

/**
 * 文件请求处理.
 *
 * Created by Tomlezen.≥
 * Data: 2018/9/5.
 * Time: 17:54.
 */
class FileRequestHandler(private val webServer: AdaWebServer) : RequestHandler {

	override fun onRequest(session: IHTTPSession): Response? =
			when (session.uri) {
				"/api/file/list" -> checkPermission { session.verifyParams(::handleFileListRequest, PATH) }
				"/api/file/new_file" -> checkPermission { session.verifyParams(::handleNewFileRequest, PATH, FILE_NAME) }
				"/api/file/new_folder" -> checkPermission { session.verifyParams(::handleNewFolderRequest, PATH, FOLDER_NAME) }
				"/api/file/upload" -> checkPermission { session.verifyParams(::handleUploadRequest, PATH) }
				"/api/file/download" -> checkPermission { session.verifyParams(::handleDownloadRequest, PATH) }
				"/api/file/delete" -> checkPermission { session.verifyParams(::handleFileDeleteRequest, PATH) }
				else -> null
			}

	/**
	 * 检查权限.
	 * @param doOnPermissionGranted () -> AdaResponse
	 * @return AdaResponse
	 */
	private fun checkPermission(doOnPermissionGranted: () -> Response): Response =
			if (webServer.filePermissionGranted) {
				doOnPermissionGranted.invoke()
			} else {
				responseError(errorMsg = "没有文件读写权限")
			}

	/**
	 * 处理文件列表请求.
	 * @param session IHTTPSession
	 */
	private fun handleFileListRequest(session: IHTTPSession): Response =
			handleRequestSafely {
				responseData(session.filePath().listFiles().toResponse())
			}

	/**
	 * 处理新建文件请求.
	 * @param session IHTTPSession
	 * @return AdaResponse
	 */
	private fun handleNewFileRequest(session: IHTTPSession): Response =
			handleRequestSafely {
				val path = session.parms[PATH] ?: ""
				val fileName = session.parms[FILE_NAME] ?: ""
				if (fileName.isEmpty()) {
					responseError(errorMsg = "非法的文件名")
				} else {
					try {
						val realPath = if (path == "root") Environment.getExternalStorageDirectory().absolutePath else path
						val newFile = File(realPath, fileName)
						if (newFile.createNewFile()) {
							responseData(realPath.listFiles().toResponse())
						} else {

							responseError(errorMsg = "文件创建失败")
						}
					} catch (e: Exception) {
						responseError(errorMsg = "文件创建失败：${e.message}")
					}
				}
			}

	/**
	 * 处理新建文件夹请求.
	 * @param session IHTTPSession
	 * @return AdaResponse
	 */
	private fun handleNewFolderRequest(session: IHTTPSession): Response =
			handleRequestSafely {
				val folderName = session.parms[FOLDER_NAME] ?: ""
				if (folderName.isEmpty()) {
					responseError(errorMsg = "非法的文件夹名")
				} else {
					try {
						val path = session.filePath()
						val newFile = File(path, folderName)
						if (newFile.mkdir()) {
							responseData(path.listFiles().toResponse())
						} else {
							responseError(errorMsg = "文件夹创建失败")
						}
					} catch (e: Exception) {
						responseError(errorMsg = "文件夹创建失败：${e.message}")
					}
				}
			}

	/**
	 * 处理文件上传请求.
	 * @param session IHTTPSession
	 * @return AdaResponse
	 */
	private fun handleUploadRequest(session: IHTTPSession): Response =
			handleRequestSafely {
				val path = session.filePath()
				val parseBody = hashMapOf<String, String>()
				session.parseBody(parseBody)
				if (parseBody.isNotEmpty()) {
					val filePaths = mutableListOf<String>()
					parseBody.forEach {
						val cacheFile = File(it.value)
						val filePath = "$path/${session.parms[it.key]}"
						cacheFile.renameTo(File(filePath))
						filePaths.add(filePath)
					}
					responseData(path.listFiles().toResponse())
				} else {
					responseError(errorMsg = "空文件")
				}
			}

	/**
	 * 处理文件下载请求.
	 * @param session IHTTPSession
	 * @return AdaResponse
	 */
	private fun handleDownloadRequest(session: IHTTPSession): Response =
			handleRequestSafely {
				val path = session.parms[PATH]
				val file = File(path)
				if (!file.exists()) {
					responseError(errorMsg = "文件不存在")
				} else if (!file.canRead()) {
					responseError(errorMsg = "文件不可读取")
				} else {
					newChunkedResponse(Status.OK, "*/*", file.inputStream()).apply {
						addHeader("Content-Disposition", "attachment; filename=${file.name}")
					}
				}
			}

	/**
	 * 处理文件删除请求.
	 * @param session IHTTPSession
	 * @return AdaResponse
	 */
	private fun handleFileDeleteRequest(session: IHTTPSession): Response =
			handleRequestSafely {
				val path = session.filePath()
				val file = File(path)
				if (file.exists()) {
					val parent = file.parentFile.absolutePath
					if (file.deleteRecursively()) {
						responseData(parent.listFiles().toResponse())
					} else {
						responseError(errorMsg = "删除失败")
					}
				} else {
					responseError(errorMsg = "文件不存在")
				}
			}

	private fun IHTTPSession.filePath() =
			with(parms[PATH] ?: "") {
				if (this == "root") {
					Environment.getExternalStorageDirectory().absolutePath
				} else {
					this
				}
			}
}