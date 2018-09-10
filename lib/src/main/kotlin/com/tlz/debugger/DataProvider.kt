package com.tlz.debugger

import com.tlz.debugger.models.KeyValue
import com.tlz.debugger.models.TableInfo
import com.tlz.debugger.models.TableWrapper
import java.io.File
import android.util.Pair

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:00.
 */
interface DataProvider {

  fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>)

  /**
   * 获取数据库列表.
   */
  fun getDatabaseList(): List<String>

  /**
   * 获取数据库文件.
   */
  fun getDatabaseFile(dName: String): File?

  /**
   * 获取数据库中所有表.
   */
  fun getAllTable(databaseName: String): TableWrapper

  /**
   * 执行sql数据查询.
   */
  fun executeQuery(dName: String, tName: String, sql: String): List<Any>

  /**
   * 执行sql命令.
   */
  fun executeSql(dName: String, sql: String): Any

  /**
   * 添加行数据.
   */
  fun addRow(dName: String, tName: String, content: Array<KeyValue>): Boolean

  /**
   * 删除行数据.
   */
  fun deleteRow(dName: String, tName: String, where: String): Boolean

  /**
   * 更新行数据.
   */
  fun updateRow(dName: String, tName: String, content: Array<KeyValue>, where: String): Boolean

  /**
   * 获取表的基本信息.
   */
  fun getTableInfo(dName: String, tName: String): TableInfo?

  /**
   * 获取表中数据条数.
   */
  fun getTableDataCount(dName: String, tName: String, where: String): Int

}