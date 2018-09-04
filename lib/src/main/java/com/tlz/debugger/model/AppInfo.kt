package com.tlz.debugger.model

import android.support.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/1/30.
 * Time: 11:57.
 * @param name 应用名.
 * @param pkg 包名.
 * @param verName 版本名.
 * @param verCode 版本号.
 * @param dbs 应用数据库列表.
 */
@Keep
class AppInfo(
		val name: String,
		val pkg: String,
		val verName: String,
		val verCode: Int,
		val dbs: List<Db>
)