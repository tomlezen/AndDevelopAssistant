package com.tlz.debugger.model

import android.support.annotation.Keep

/**
 * Created by Tomlezen.
 * Data: 2018/9/4.
 * Time: 14:14.
 */
@Keep
class ProviderInfo(
		val name: String,
		val authority: String?,
		val readPermission: String?,
		val writePermission: String?,
		val grantUriPermissions: Boolean,
		val multiprocess: Boolean
)