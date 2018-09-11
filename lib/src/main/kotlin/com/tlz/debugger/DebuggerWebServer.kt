package com.tlz.debugger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Pair
import com.tlz.debugger.handlers.*
import com.tlz.debugger.socket.DebuggerWSD
import fi.iki.elonen.NanoHTTPD
import java.io.File


/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 15:00.
 */
class DebuggerWebServer private constructor(internal val ctx: Context, private val port: Int) : NanoHTTPD(port) {

	private val tag = DebuggerWebServer::class.java.canonicalName

	private val dataProvider: DataProvider by lazy { DataProviderImpl(ctx, gson) }
	private val appManager by lazy { ApplicationManager(ctx) }
	private val wsd by lazy { DebuggerWSD() }

	/** 所有请求处理器. */
	private val handlers = mutableListOf<RequestHandler>()

	/** web服务器是否运行. */
	private var isRunning = false

	/**
	 * 启动服务器.
	 */
	fun startServer() {
		if (!isRunning) {
			tempFileManagerFactory = AndTempFileManagerFactory(ctx)
			serverAddress = "${Initializer.getPhoneIp()}:$port"
			Thread {
				appManager.readApplicationList()
				// 注册各种处理器
				handlers.add(LogRequestHandler(ctx, wsd))
				handlers.add(InitRequestHandler(ctx, dataProvider, appManager))
				handlers.add(DbRequestHandler(dataProvider))
				handlers.add(AppRequestHandler(ctx, appManager))
				handlers.add(FileRequestHandler())
				handlers.add(DefaultRequestHandler(ctx))

				start(10000)
				wsd.start(this)
				isRunning = true
				Log.e(tag, "address: $serverAddress")
			}.start()
		}
	}

	fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
		dataProvider.setCustomDatabaseFiles(files)
	}

	override fun serve(session: IHTTPSession?): Response {
		session?.run {
			handlers.forEach {
				val resp = it.onRequest(session)
				if (resp != null) {
					return resp
				}
			}
		}
		return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "")
	}

	companion object {
		private const val DEF_PORT = 10000

		@SuppressLint("StaticFieldLeak")
		private var instance: DebuggerWebServer? = null

		/**
		 * 文件读写权限是否通过.
		 */
		var filePermissionGranted = false
			get() {
				if (!field) {
					field = instance?.ctx?.isPermissionsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE) ?: false
				}
				return field
			}

		var serverAddress: String = ""

		fun start(ctx: Context) {
			instance = instance ?: DebuggerWebServer(ctx, readPort(ctx))
			instance?.startServer()
		}

		fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
			instance?.setCustomDatabaseFiles(files)
		}

		/**
		 * 读取设置的端口号.
		 */
		private fun readPort(ctx: Context): Int {
			return try {
				ctx.metaDataInt("DEBUG_PORT", DEF_PORT)
			} catch (e: Exception) {
				e.printStackTrace()
				DEF_PORT
			}
		}
	}

}