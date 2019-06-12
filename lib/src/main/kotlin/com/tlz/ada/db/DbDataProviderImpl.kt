package com.tlz.ada.db

import android.content.Context
import android.util.Pair
import com.tlz.ada.models.KeyValue
import com.tlz.ada.models.TableInfo
import com.tlz.ada.models.TableWrapper
import java.io.File

/**
 * Created by Tomlezen.
 * Date: 2019-06-09.
 * Time: 22:43.
 */
class DbDataProviderImpl(private val ctx: Context): AdaDataProvider {
  override fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getAllDatabase(): List<String> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDatabaseFile(dName: String): File? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getAllTable(dName: String): TableWrapper {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getTableInfo(dName: String, tName: String): TableInfo? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getTableDataCount(dName: String, tName: String, where: String): Int {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun query(dName: String, tName: String, sql: String): List<Any> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun rawQuery(dName: String, sql: String): Any {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun add(dName: String, tName: String, content: Array<KeyValue>): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun delete(dName: String, tName: String, where: String): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun update(dName: String, tName: String, content: Array<KeyValue>, where: String): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}