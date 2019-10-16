package com.tlz.ada.handlers

import org.nanohttpd.protocols.http.IHTTPSession
import org.nanohttpd.protocols.http.response.Response

/**
 * 网络请求处理器.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 13:55.
 */
interface RequestHandler {

	fun onRequest(session: IHTTPSession): Response?

}