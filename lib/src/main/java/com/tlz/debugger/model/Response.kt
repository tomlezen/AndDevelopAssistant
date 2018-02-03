package com.tlz.debugger.model

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:26.
 * status: 状态码（200标识成功）.
 * data: 发送的具体内容.
 * errMsg: 错误信息.
 */
class Response(val status: Int = 200, val data: Any? = null, val errMsg: String = "")