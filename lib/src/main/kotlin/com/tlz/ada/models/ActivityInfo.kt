package com.tlz.ada.models

import androidx.annotation.Keep

/**
 * Created by Tomlezen.
 * Data: 2018/9/4.
 * Time: 14:09.
 */
@Keep
class ActivityInfo(
		val name: String,
		val launchMode: String?,
		val flags: Int,
		val configChanges: Int,
		val softInputMode: Int,
		val permission: String?,
		val exported: Boolean
)