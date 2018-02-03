package com.tlz.debugger

import com.tlz.debugger.model.TableInfo
import com.tlz.debugger.model.TableWrapper

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:00.
 */
interface DataProvider {

  /**
   * 获取数据库列表.
   */
  fun getDatabaseList(): List<String>

  /**
   * 获取数据库中所有表.
   */
  fun getAllTable(databaseName: String): TableWrapper

  /**
   * 执行sql数据查询.
   */
  fun executeQuery(dName: String, sql: String): List<List<String>>

  /**
   * 执行sql命令.
   */
  fun executeSql(dName: String, sql: String): Boolean

  /**
   * 获取表的基本信息.
   */
  fun getTableInfo(dName: String, tName: String): TableInfo?

  /**
   * 获取表中数据条数.
   */
  fun getTableDataCount(dName: String, tName: String, where: String): Int

}