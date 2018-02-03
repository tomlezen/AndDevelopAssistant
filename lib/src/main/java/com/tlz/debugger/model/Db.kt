package com.tlz.debugger.model

/**
 * Created by tomlezen.
 * Data: 2018/1/30.
 * Time: 12:00.
 */
data class Db(val name: String, val version: Int, val tables: List<TableInfo>)