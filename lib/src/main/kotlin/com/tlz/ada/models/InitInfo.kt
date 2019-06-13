package com.tlz.ada.models

import androidx.annotation.Keep

/**
 * @property dbs List<Db> 数据库列表.
 * @constructor
 */
@Keep
class InitInfo(
		icon: String,
		name: String,
		pkg: String,
		verName: String?,
		verCode: Int?,
		isSystemApp: Boolean,
		size: Long,
		val logServer: String,
		val dbs: List<Db>
) : Application(icon, name, pkg, verName, verCode, isSystemApp, size)