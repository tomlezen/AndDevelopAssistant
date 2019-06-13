package com.tlz.ada.models

import androidx.annotation.Keep

/**
 * Created by Tomlezen.
 * Data: 2018/9/4.
 * Time: 14:14.
 */
@Keep
class ServiceInfo(
		val name: String,
		val permission: String?,
		val flags: Int,
		val exported: Boolean
)