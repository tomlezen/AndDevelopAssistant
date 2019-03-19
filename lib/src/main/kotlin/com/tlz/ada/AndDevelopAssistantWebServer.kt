package com.tlz.ada

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Pair
import com.tlz.ada.handlers.AppRequestHandler
import com.tlz.ada.handlers.DbRequestHandler
import com.tlz.ada.handlers.DefaultRequestHandler
import com.tlz.ada.handlers.FileRequestHandler
import com.tlz.ada.handlers.InitRequestHandler
import com.tlz.ada.handlers.LogRequestHandler
import com.tlz.ada.handlers.RequestHandler
import com.tlz.ada.handlers.ScreenShotHandler
import com.tlz.ada.socket.AndDevelopAssistantWSD
import fi.iki.elonen.NanoHTTPD
import java.io.File


/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 15:00.
 */
class AndDevelopAssistantWebServer private constructor(internal val ctx: Context, private val port: Int) : NanoHTTPD(port) {

    private val dataProvider: DataProvider by lazy { DataProviderImpl(ctx, gson) }
    private val appManager by lazy { ApplicationManager(ctx) }
    private val wsd by lazy { AndDevelopAssistantWSD() }
    private val activityLifeCycleHooker by lazy { ActivityLifeCycleHooker(ctx) }

    /** 所有请求处理器. */
    private val handlers = mutableListOf<RequestHandler>()

    /** web服务器是否运行. */
    private var isRunning = false

    /** 文件读写权限是否通过. */
    var filePermissionGranted = false
        get() {
            if (!field) {
                field = ctx.isPermissionsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            return field
        }

    /**
     * 启动服务器.
     */
    @SuppressLint("LongLogTag")
    fun startServer() {
        if (!isRunning) {
            activityLifeCycleHooker.install()
            tempFileManagerFactory = AndTempFileManagerFactory(ctx)
            serverAddress = "${Initializer.getPhoneIp()}:$port"
            Thread {
                val isSuccessful = executeSafely {
                    // 为了加快应用列表api的访问速度，先加载所有的应用再启动服务器
                    appManager.readApplicationList()
                    // 注册各种处理器
                    handlers.add(LogRequestHandler(ctx, wsd))
                    handlers.add(InitRequestHandler(ctx, dataProvider, appManager))
                    handlers.add(DbRequestHandler(dataProvider))
                    handlers.add(AppRequestHandler(ctx, appManager))
                    handlers.add(FileRequestHandler(this))
                    handlers.add(ScreenShotHandler(activityLifeCycleHooker))
                    handlers.add(DefaultRequestHandler(ctx))

                    start(10000)
                    wsd.start(this)
                    isRunning = true
                    Log.e(TAG, "address: $serverAddress")
                }
                if (!isSuccessful) {
                    Log.e(TAG, "Android调试辅助初始化失败")
                }
            }.start()
        }
    }

    /**
     * 设置自定义数据库.
     * @param files Map<String, Pair<File, String>>
     */
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
        const val TAG = "AndDevelopAssistantWebServer"
        private const val DEF_PORT = 10000

        @SuppressLint("StaticFieldLeak")
        private var instance: AndDevelopAssistantWebServer? = null

        /** 服务器地址. */
        var serverAddress: String = ""

        fun start(ctx: Context) {
            instance = instance ?: AndDevelopAssistantWebServer(ctx, readPort(ctx))
            instance?.startServer()
        }

        /**
         * 设置自定义数据库文件.
         * @param files Map<String, Pair<File, String>>
         */
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
                DEF_PORT
            }
        }
    }

}