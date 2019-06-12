package com.tlz.ada.db

import android.content.Context
import android.content.SharedPreferences
import android.util.Pair
import com.tlz.ada.AdaConstUtils
import com.tlz.ada.gson
import com.tlz.ada.models.KeyValue
import com.tlz.ada.models.TableInfo
import com.tlz.ada.models.TableWrapper
import java.io.File

/**
 * SharePreferences数据处理.
 * Created by Tomlezen.
 * Date: 2019-06-09.
 * Time: 22:03.
 */
internal class SpDataProviderImpl(private val ctx: Context) : AdaDataProvider {

  private var spTables = mutableListOf<TableInfo>()

  override fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {}

  override fun getAllDatabase(): List<String> = listOf(AdaConstUtils.PREFS)

  override fun getDatabaseFile(dName: String): File? = null

  override fun getAllTable(dName: String): TableWrapper {
    val tables = mutableListOf<TableInfo>()
    val spDir = File(ctx.applicationInfo.dataDir + "/shared_prefs")
    if (spDir.exists()) {
      spDir.listFiles()
          .map { it.name }
          .filter { it.endsWith(".xml") }
          .map { it.substring(0, it.length - 4) }
          .mapTo(tables) { TableInfo(it, listOf()) }
    }
    spTables = tables
    return TableWrapper(0, tables)
  }

  override fun getTableInfo(dName: String, tName: String): TableInfo? =
      spTables.find { it.name == dName }

  override fun getTableDataCount(dName: String, tName: String, where: String): Int = -1

  override fun query(dName: String, tName: String, sql: String): List<Any> {
    val data = mutableListOf<Any>()
    val sharePreferences = ctx.getSharedPreferences(tName, Context.MODE_PRIVATE)
    for (entry in sharePreferences.all.entries) {
      data.add(
          KeyValue(
              entry.key,
              entry.value?.toString() ?: "null",
              when {
                entry.value is String -> AdaConstUtils.TYPE_TEXT
                entry.value is Int -> AdaConstUtils.TYPE_INTEGER
                entry.value is Long -> AdaConstUtils.TYPE_LONG
                entry.value is Float -> AdaConstUtils.TYPE_FLOAT
                entry.value is Boolean -> AdaConstUtils.TYPE_BOOLEAN
                entry.value is Set<*> -> AdaConstUtils.TYPE_STRING_SET
                else -> AdaConstUtils.TYPE_TEXT
              })
      )
    }
    if (sql.isNotBlank()) {
      if (sql == "asc") {
        data.sortWith(Comparator { o1, o2 -> if ((o1 as KeyValue).key > (o2 as KeyValue).key) 1 else -1 })
      } else {
        data.sortWith(Comparator { o1, o2 -> if ((o1 as KeyValue).key > (o2 as KeyValue).key) -1 else 1 })
      }
    }
    return data
  }

  override fun rawQuery(dName: String, sql: String): Any = false

  override fun add(dName: String, tName: String, content: Array<KeyValue>): Boolean =
      update(dName, tName, content, "")

  override fun delete(dName: String, tName: String, where: String): Boolean {
    ctx.getSharedPreferences(tName, Context.MODE_PRIVATE).edit().remove(where).apply()
    return true
  }

  override fun update(dName: String, tName: String, content: Array<KeyValue>, where: String): Boolean {
    dName.edit {
      val keyValue = content.first()
      when (keyValue.type) {
        AdaConstUtils.TYPE_INTEGER -> putInt(keyValue.key, keyValue.value?.toInt() ?: 0)
        AdaConstUtils.TYPE_FLOAT -> putFloat(keyValue.key, keyValue.value?.toFloat() ?: 0f)
        AdaConstUtils.TYPE_LONG -> putLong(keyValue.key, keyValue.value?.toLong() ?: 0L)
        AdaConstUtils.TYPE_BOOLEAN -> putBoolean(keyValue.key, keyValue.value?.toBoolean() ?: false)
        AdaConstUtils.TYPE_STRING_SET ->
          putStringSet(
              keyValue.key,
              gson.fromJson<Array<String>>(keyValue.value, Array<String>::class.java).mapTo(mutableSetOf()) { it }
          )
        else -> putString(keyValue.key, keyValue.value)
      }
    }
    return true
  }

  fun String.edit(block: SharedPreferences.Editor.() -> Unit) {
    ctx.getSharedPreferences(this, Context.MODE_PRIVATE).edit().apply(block).apply()
  }
}