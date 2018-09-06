package com.tlz.debugger.models

import android.support.annotation.Keep

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 17:00.
 * @param verison 数据库版本.
 * @param tables 表.
 */
@Keep
class TableWrapper(val verison: Int, val tables: List<TableInfo>)