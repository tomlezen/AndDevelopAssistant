package com.tlz.ada.models

import androidx.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:26.
 * @param status 状态码（200标识成功）.
 * @param data 发送的具体内容.
 * @param errMsg 错误信息.
 * @param total 数据总数.
 */
@Keep
class Response(
		val status: Int = 200,
		val data: Any? = null,
		val errMsg: String = "",
		val total: Int = 0
)