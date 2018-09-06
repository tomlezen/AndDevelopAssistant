package com.tlz.debugger.models

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
		val path: String,
		size: Long,
		val targetSdk: Int,
		val firstInstallTime: Long,
		val lastUpdateTime: Long,
		val permissions: List<Pair<String, String>>,
		val activities: List<ActivityInfo>,
		val services: List<ServiceInfo>,
		val receivers: List<ActivityInfo>,
		val providers: List<ProviderInfo>,
		val applicationInfo: ApplicationInfo
) : Application(icon, name, pkg, verName, verCode, isSystemApp, size)