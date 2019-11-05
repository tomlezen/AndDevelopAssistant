package com.tlz.ada.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.content.Context
import com.tlz.ada.models.KeyValue
import com.tlz.ada.models.Table
import com.tlz.ada.models.TableInfo
import java.io.File

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:01.
 */
class AdaDataProviderImpl(ctx: Context) : AdaDataProvider {

  private val dbDataProvider = DbDataProviderImpl(ctx)
  private val spDataProvider = SpDataProviderImpl(ctx)

  override fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
    dbDataProvider.setCustomDatabaseFiles(files)
    spDataProvider.setCustomDatabaseFiles(files)
  }

  override fun setInMemoryRoomDatabases(databases: Map<String, SupportSQLiteDatabase>) {
    dbDataProvider.setInMemoryRoomDatabases(databases)
    spDataProvider.setInMemoryRoomDatabases(databases)
  }

  override fun getAllDatabase(): List<String> =
      spDataProvider.getAllDatabase() + dbDataProvider.getAllDatabase()

  override fun getDatabaseFile(dName: String): File? =
      dName.toDataProvider().getDatabaseFile(dName)

  override
  fun getAllTable(dName: String): Table =
      dName.toDataProvider().getAllTable(dName)

  override fun getTableInfo(dName: String, tName: String): TableInfo =
      dName.toDataProvider().getTableInfo(dName, tName)

  override fun getTableDataCount(dName: String, tName: String, where: String): Int =
      dName.toDataProvider().getTableDataCount(dName, tName, where)

  override fun query(dName: String, tName: String, sql: String): List<Any> =
      dName.toDataProvider().query(dName, tName, sql)

  override fun rawQuery(dName: String, sql: String): Any =
      dName.toDataProvider().rawQuery(dName, sql)

  override fun add(dName: String, tName: String, content: Array<KeyValue>): Boolean =
      dName.toDataProvider().add(dName, tName, content)

  override fun delete(dName: String, tName: String, where: String): Boolean =
      dName.toDataProvider().delete(dName, tName, where)

  override fun update(dName: String, tName: String, content: Array<KeyValue>, where: String): Boolean =
      dName.toDataProvider().update(dName, tName, content, where)

  private fun String.toDataProvider(): AdaDataProvider =
      if (this == "SharePreferences") spDataProvider else dbDataProvider
}