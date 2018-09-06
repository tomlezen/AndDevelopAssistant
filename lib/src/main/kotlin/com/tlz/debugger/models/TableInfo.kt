package com.tlz.debugger.models

import android.support.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/2/1.
 * Time: 11:42.
 * @param name 表名.
 * @param fieldInfos 表中所有字段信息.
 */
@Keep
class TableInfo(val name: String, val fieldInfos: List<TableFieldInfo>)