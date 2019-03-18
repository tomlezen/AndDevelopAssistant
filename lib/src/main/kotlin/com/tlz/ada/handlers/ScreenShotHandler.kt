package com.tlz.ada.handlers

import android.graphics.Bitmap
import com.tlz.ada.ActivityLifeCycleHooker
import com.tlz.ada.handleRequestSafely
import com.tlz.ada.responseError
import com.tlz.ada.toInputStream
import com.tlz.ada.verifyParams
import fi.iki.elonen.NanoHTTPD


/**
 * Created by tomlezen.
 * Data: 2019/3/18.
 * Time: 16:26.
 */
class ScreenShotHandler(private val activityLifeCycleHooker: ActivityLifeCycleHooker) : RequestHandler {

    override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
        when (session.uri) {
            "/api/app/screenshot" -> session.verifyParams(::handleScreenShotRequest)
            else -> null
        }

    /**
     * 处理截图请求.
     * @param session NanoHTTPD.IHTTPSession
     * @return NanoHTTPD.Response
     */
    private fun handleScreenShotRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
        handleRequestSafely {
            activityLifeCycleHooker.currentActivityInstance?.window?.decorView?.let { dView ->
                dView.isDrawingCacheEnabled = true
                dView.buildDrawingCache()
                val bitmap = Bitmap.createBitmap(dView.drawingCache)
                NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/png", bitmap.toInputStream())
            } ?: responseError(errorMsg = "app未在前台")
        }

}