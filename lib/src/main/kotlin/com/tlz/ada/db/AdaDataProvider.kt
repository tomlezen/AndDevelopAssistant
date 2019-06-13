package com.tlz.ada.db

import com.tlz.ada.models.KeyValue
import com.tlz.ada.models.TableInfo
import com.tlz.ada.models.Table
import java.io.File

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:00.
 */
interface AdaDataProvider {
  fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>)

  fun getAllDatabase(): List<String>

  fun getDatabaseFile(dName: String): File?

  fun getAllTable(dName: String): Table

  fun getTableInfo(dName: String, tName: String): TableInfo

  fun getTableDataCount(dName: String, tName: String, where: String): Int

  fun query(dName: String, tName: String, sql: String): List<Any>

  fun rawQuery(dName: String, sql: String): Any

  fun add(dName: String, tName: String, content: Array<KeyValue>): Boolean

  fun delete(dName: String, tName: String, where: String): Boolean

  fun update(dName: String, tName: String, content: Array<KeyValue>, where: String): Boolean
}