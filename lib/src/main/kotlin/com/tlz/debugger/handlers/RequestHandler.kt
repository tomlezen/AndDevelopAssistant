package com.tlz.debugger.handlers

import fi.iki.elonen.NanoHTTPD

/**
 * 网络请求处理器.
 *
 * Created by Tomlezen.
 * Data: 2018/9/5.
 * Time: 13:55.
 */
interface RequestHandler {

	fun onRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response?

}