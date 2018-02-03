package com.tlz.debugger.model

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 17:00.
 * @param verison 数据库版本
 * @param tables 表.
 */
class TableWrapper(val verison: Int, val tables: List<TableInfo>)