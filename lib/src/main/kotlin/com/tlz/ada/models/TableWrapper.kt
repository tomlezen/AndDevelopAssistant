package com.tlz.ada.models

import android.support.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 17:00.
 * @param version 数据库版本.
 * @param tables 表.
 */
@Keep
class TableWrapper(val version: Int, val tables: List<TableInfo>)