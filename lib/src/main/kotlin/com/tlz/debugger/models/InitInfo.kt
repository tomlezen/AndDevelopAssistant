package com.tlz.debugger.models

import android.support.annotation.Keep

/**
 * @property dbs List<Db> 数据库列表.
 * @constructor
 */
@Keep
class InitInfo(
		icon: String,
		name: String,
		pkg: String,
		verName: String,
		verCode: Int,
		isSystemApp: Boolean,
		size: Long,
		val dbs: List<Db>
) : Application(icon, name, pkg, verName, verCode, isSystemApp, size)