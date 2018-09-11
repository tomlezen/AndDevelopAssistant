package com.tlz.ada.models

import android.support.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/2/1.
 * Time: 10:58.
 * @param name 字段名.
 * @param type 字段数据类型.
 * @param isPrimaryKey 是否是主键.
 * @param nullable 是否可空.
 * @param defValue 默认值.
 */
@Keep
data class TableFieldInfo(val name: String, val type: String, val isPrimaryKey: Boolean = false, val nullable: Boolean, val defValue: String?)