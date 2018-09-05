package com.tlz.debugger.models

import android.support.annotation.Keep

/**
 * 应用.
 *
 * Created by Tomlezen.
 * Data: 2018/9/4.
 * Time: 13:33.
 */
@Keep
open class Application(
		val icon: String,
		val name: String,
		val pkg: String,
		val verName: String,
		val verCode: Int,
		val isSystemApp: Boolean,
		val size: Long
)