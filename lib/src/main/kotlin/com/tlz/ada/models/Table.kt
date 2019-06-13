package com.tlz.ada.models

import androidx.annotation.Keep

/**
 * 数据库表信息.
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 17:00.
 * @param version 数据库版本.
 * @param tableInfos 表信息.
 */
@Keep
class Table(val version: Int, val tableInfos: List<TableInfo>)