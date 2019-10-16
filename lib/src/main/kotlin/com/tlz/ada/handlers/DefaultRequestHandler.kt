package com.tlz.ada.handlers

import android.content.Context
import com.tlz.ada.handleRequestSafely
import com.tlz.ada.readHtml
import com.tlz.ada.response
import com.tlz.ada.responseHtml
import org.nanohttpd.protocols.http.IHTTPSession
import org.nanohttpd.protocols.http.response.Response
import org.nanohttpd.protocols.http.response.Response.newChunkedResponse
import org.nanohttpd.protocols.http.response.Status

/**
 * 基础请求处理.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 16:21.
 */
class DefaultRequestHandler(private val ctx: Context) : RequestHandler {

	override fun onRequest(session: IHTTPSession): Response? =
			handleRequestSafely {
				val uri = session.uri
				when {
					uri.endsWith(".png") ->
						newChunkedResponse(Status.OK, "image/png", ctx.assets.open("web$uri"))
					uri.endsWith(".ico") ->
						newChunkedResponse(Status.OK, "image/vnd.microsoft.icon", ctx.assets.open("web$uri"))
					uri.endsWith(".svg") ->
						newChunkedResponse(Status.OK, "image/svg+xml", ctx.assets.open("web$uri"))
					else -> {
						try {
							val file = uri.readHtml(ctx)
							when {
								uri.contains(".css") -> response("text/css", file, "86400")
								uri.contains(".js") -> response("text/javascript", file, "86400")
								uri.contains(".eot") -> response("application/vnd.ms-fontobject", file)
//								uri.endsWith(".svg") -> response("image/svg+xml", file)
								uri.contains(".ttf") -> response("application/x-font-ttf", file)
								uri.contains(".woff2") -> response("font/woff2", file)
								uri.contains(".woff") -> response("application/font-woff", file)
								else -> responseHtml("/index.html".readHtml(ctx))
							}
						} catch (e: Exception) {
							responseHtml("/index.html".readHtml(ctx))
//							newFixedLengthResponse(AdaStatus.FORBIDDEN,MIME_PLAINTEXT, "")
						}
					}
				}
			}
}