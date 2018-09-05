package com.tlz.debugger

import android.content.Context
import android.content.pm.ActivityInfo.*
import android.content.pm.PackageManager
import com.tlz.debugger.models.*
import java.io.File

/**
 * Created by Tomlezen.
 * Data: 2018/9/4.
 * Time: 13:31.
 */
interface ApplicationManager {

	/** 应用列表. */
	val applicationList: List<Application>

	/**
	 * 读取应用列表.
	 */
	fun readApplicationList()

	/**
	 * 获取应用信息.
	 * @param pkg String
	 * @return ApplicationInfo
	 */
	fun getApplicationInfoByPkg(pkg: String): ApplicationInfo?

	companion object {
		operator fun invoke(ctx: Context): ApplicationManager =
				ApplicationManagerImpl(ctx)
	}

}

private class ApplicationManagerImpl(private val ctx: Context) : ApplicationManager {

	private val pkgManager = ctx.packageManager

	private var _applicationList = listOf<Application>()
	private val _applicationInfoList = mutableListOf<ApplicationInfo>()

	override val applicationList: List<Application>
		get() = _applicationList

	override fun readApplicationList() {
		pkgManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
				or PackageManager.GET_ACTIVITIES
				or PackageManager.GET_SERVICES
				or PackageManager.GET_PERMISSIONS
				or PackageManager.GET_PERMISSIONS
				or PackageManager.GET_RECEIVERS
		)
				.mapTo(_applicationInfoList) {
					val path = readApkPath(it.packageName)
					val appInfo = it.applicationInfo
					ApplicationInfo(
							"/api/app/icon?pkg=${it.packageName}",
							appInfo.loadLabel(pkgManager).toString(),
							it.packageName,
							(it.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
							it.versionName,
							it.versionCode,
							path,
							File(path).run {
								if (exists()) {
									length()
								} else {
									0L
								}
							},
							appInfo.targetSdkVersion,
							it.firstInstallTime,
							it.lastUpdateTime,
							(it.requestedPermissions ?: arrayOf()).mapTo(mutableListOf()) { p ->
								Pair("permission", p)
							},
							it.activities?.mapTo(mutableListOf()) { actInfo ->
								ActivityInfo(
										actInfo.name,
										when (actInfo.launchMode) {
											LAUNCH_SINGLE_INSTANCE -> "singleInstance"
											LAUNCH_SINGLE_TOP -> "singleTop"
											LAUNCH_MULTIPLE -> "singleMultiple"
											else -> "singleTask"
										},
										actInfo.flags,
										actInfo.configChanges,
										actInfo.softInputMode,
										actInfo.permission,
										actInfo.exported
								)
							} ?: listOf(),
							it.services?.mapTo(mutableListOf()) { srvInfo ->
								ServiceInfo(
										srvInfo.name,
										srvInfo.permission,
										srvInfo.flags,
										srvInfo.exported
								)
							} ?: listOf(),
							it.receivers?.mapTo(mutableListOf()) { actInfo ->
								ActivityInfo(
										actInfo.name,
										when (actInfo.launchMode) {
											LAUNCH_SINGLE_INSTANCE -> "singleInstance"
											LAUNCH_SINGLE_TOP -> "singleTop"
											LAUNCH_MULTIPLE -> "singleMultiple"
											else -> "singleTask"
										},
										actInfo.flags,
										actInfo.configChanges,
										actInfo.softInputMode,
										actInfo.permission,
										actInfo.exported
								)
							} ?: listOf(),
							it.providers?.mapTo(mutableListOf()) { providerInfo ->
								ProviderInfo(
										providerInfo.name,
										providerInfo.authority,
										providerInfo.readPermission,
										providerInfo.writePermission,
										providerInfo.grantUriPermissions,
										providerInfo.multiprocess
								)
							} ?: listOf(),
							it.applicationInfo
					)
				}
		_applicationList = _applicationInfoList.mapTo(mutableListOf()) { Application(it.icon, it.name, it.pkg, it.verName, it.verCode, it.isSystemApp, it.size) }
	}

	/**
	 * 读取apk路径.
	 * @param pkg String
	 * @return String
	 */
	private fun readApkPath(pkg: String): String {
		val path = cmd("pm path $pkg").firstOrNull()
		if (!path.isNullOrEmpty() && path?.startsWith("package:") == true) {
			return path.split(":")[1]
		}
		return ""
	}

	override fun getApplicationInfoByPkg(pkg: String): ApplicationInfo? =
			_applicationInfoList.find { it.pkg == pkg }

}