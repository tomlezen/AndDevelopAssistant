package com.tlz.debugger.handlers

import android.content.Context
import com.tlz.debugger.handleRequestSafely
import com.tlz.debugger.readHtml
import com.tlz.debugger.response
import com.tlz.debugger.responseHtml
import fi.iki.elonen.NanoHTTPD

/**
 * 基础请求处理.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 16:21.
 */
class DefaultRequestHandler(private val ctx: Context) : RequestHandler {

	override fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? =
			handleRequestSafely {
				val uri = session.uri
				when {
					uri.endsWith(".png") ->
						NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/png", ctx.assets.open("web" + uri))
					uri.endsWith(".ico") ->
						NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/vnd.microsoft.icon", ctx.assets.open("web" + uri))
					else -> {
						try {
							val file = uri.readHtml(ctx)
							when {
								uri.endsWith(".css") -> response("text/css", file, "86400")
								uri.endsWith(".js") -> response("text/javascript", file, "86400")
								uri.endsWith(".eot") -> response("application/vnd.ms-fontobject", file)
								uri.endsWith(".svg") -> response("image/svg+xml", file)
								uri.endsWith(".ttf") -> response("application/x-font-ttf", file)
								uri.endsWith(".woff") -> response("application/font-woff", file)
								uri.endsWith(".woff2") -> response("font/woff2", file)
								else -> responseHtml("/index.html".readHtml(ctx))
							}
						} catch (e: Exception) {
							e.printStackTrace()
							NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "")
						}
					}
				}
			}
}