package com.tlz.debugger.model

/**
 * Created by tomlezen.
 * Data: 2018/2/1.
 * Time: 10:58.
 */
data class TableFieldInfo(val name: String, val type: String, val isPrimaryKey: Boolean = false, val nullable: Boolean, val defValue: String?)