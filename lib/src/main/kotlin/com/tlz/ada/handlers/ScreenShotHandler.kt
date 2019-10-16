package com.tlz.ada.handlers

import android.graphics.Bitmap
import com.tlz.ada.AdaActivityLifeCycleListener
import com.tlz.ada.handleRequestSafely
import com.tlz.ada.responseError
import com.tlz.ada.verifyParams
import org.nanohttpd.protocols.http.IHTTPSession
import org.nanohttpd.protocols.http.response.Response
import org.nanohttpd.protocols.http.response.Response.newChunkedResponse
import org.nanohttpd.protocols.http.response.Status
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


/**
 * Created by tomlezen.
 * Data: 2019/3/18.
 * Time: 16:26.
 */
class ScreenShotHandler(private val activityLifeCycleHooker: AdaActivityLifeCycleListener) : RequestHandler {

  override fun onRequest(session: IHTTPSession): Response? =
      when (session.uri) {
        "/api/app/screenshot" -> session.verifyParams(::handleScreenShotRequest)
        else -> null
      }

  /**
   * 处理截图请求.
   * @param session IHTTPSession
   * @return AdaResponse
   */
  private fun handleScreenShotRequest(session: IHTTPSession): Response =
      handleRequestSafely {
        activityLifeCycleHooker.currentActivityInstance?.window?.decorView?.let { dView ->
          dView.isDrawingCacheEnabled = true
          dView.buildDrawingCache()
          newChunkedResponse(Status.OK, "image/png", Bitmap.createBitmap(dView.drawingCache).toInputStream())
        } ?: responseError(errorMsg = "app未在前台")
      }

  /**
   * Bitmap转输入流.
   * @receiver Bitmap
   * @return InputStream
   */
  private fun Bitmap.toInputStream(): InputStream =
      ByteArrayOutputStream().use {
        compress(Bitmap.CompressFormat.JPEG, 100, it)
        ByteArrayInputStream(it.toByteArray())
      }
}