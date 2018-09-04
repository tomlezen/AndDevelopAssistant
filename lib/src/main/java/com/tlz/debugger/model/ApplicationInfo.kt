package com.tlz.debugger.model

import android.content.pm.ApplicationInfo
import android.support.annotation.Keep

/**
 * Created by Tomlezen.
 * Data: 2018/9/4.
 * Time: 13:33.
 */
@Keep
class ApplicationInfo(
		icon: String,
		name: String,
		pkg: String,
		isSystemApp: Boolean,
		verName: String,
		verCode: Int,
		size: Long,
		targetSdk: Int,
		firstInstallTime: Long,
		lastUpdateTime: Long,
		permissions: Array<String>?,
		activities: List<ActivityInfo>,
		services: List<ServiceInfo>,
		receivers: List<ActivityInfo>,
		providers: List<ProviderInfo>,
		val applicationInfo: ApplicationInfo
) : Application(icon, name, pkg, verName, verCode, isSystemApp, size)