package com.tlz.debugger

import android.content.Context
import com.tlz.debugger.model.TableFieldInfo
import com.tlz.debugger.model.TableInfo
import com.tlz.debugger.model.TableWrapper
import net.sqlcipher.Cursor
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import java.util.*

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:01.
 */
class IDataProvider(private val ctx: Context) : DataProvider {

  private var database: SQLiteDatabase? = null
  private var databaseName: String = ""
  private var databaseOpen = false

  private var tableWrapperMap = mutableMapOf<String, TableWrapper>()

  private val databaseFiles: Map<String, Pair<File, String>> by lazy { initDatabaseFiles() }

  override fun getDatabaseList(): List<String> {
    val data = mutableListOf<String>()
    databaseFiles.keys.mapTo(data) { it }
    return data
  }

  override fun getAllTable(databaseName: String): TableWrapper {
    val wrapper = if (databaseName == ConstUtils.PREFS) getSpTags() else getDatabaseTables(databaseName)
    tableWrapperMap.put(databaseName, wrapper)
    return wrapper
  }

  override fun executeQuery(dName: String, sql: String): List<List<String>> {
    openDatabase(dName)
    val data = mutableListOf<List<String>>()
    database?.let {
      var cursor: Cursor? = null
      try {
        cursor = it.rawQuery(sql, null)
        cursor?.let { cur ->
          val columnCount = cur.columnCount
          while (cur.moveToNext()) {
            val columnData = mutableListOf<String>()
            (0 until columnCount).forEach {
              columnData.add(cur.getString(it) ?: "null")
            }
            data.add(columnData)
          }
          cur.close()
        }
      } finally {
        executeSafely { cursor?.close() }
        closeDatabase()
      }
    }
    return data
  }

  override fun executeSql(dName: String, sql: String): Boolean {
    openDatabase(dName)
    return database?.let {
      try {
        it.execSQL(sql)
      } finally {
        closeDatabase()
      }
      true
    } ?: false
  }

  override fun getTableInfo(dName: String, tName: String): TableInfo? {
    if (tableWrapperMap.isEmpty()) {
      databaseFiles.forEach { getAllTable(it.key) }
    }
    return tableWrapperMap[dName]?.tables?.find { it.name == tName }
  }

  override fun getTableDataCount(dName: String, tName: String, where: String): Int {
    openDatabase(dName)
    return database?.let {
      var cursor: Cursor? = null
      try {
        var sql = "select count(*) from $tName"
        if (where.isNotBlank()) {
          sql += " where $where"
        }
        cursor = it.rawQuery(sql, null)
        cursor?.let {
          while (cursor.moveToNext()) {
            return cursor.getInt(0)
          }
          0
        } ?: 0
      } finally {
        executeSafely { cursor?.close() }
        closeDatabase()
      }
    } ?: 0
  }

  /**
   * 初始化数据库文件.
   */
  private fun initDatabaseFiles(): Map<String, Pair<File, String>> {
    val databaseFiles = HashMap<String, Pair<File, String>>()
    try {
      for (databaseName in ctx.databaseList()) {
        if (!databaseName.contains("-journal")) {
          val password = getDatabasePassword(databaseName)
          databaseFiles.put(databaseName, Pair(ctx.getDatabasePath(databaseName), password))
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    //最后加载SharePreferences
    databaseFiles.put(ConstUtils.PREFS, Pair(File(""), ""))

    return databaseFiles
  }

  /**
   * 获取数据库密码.
   * @param database 数据名.
   */
  private fun getDatabasePassword(database: String): String {
    var name = database
    if (name.endsWith(".db")) {
      name = name.substring(0, name.lastIndexOf("."))
    }
    return ctx.metaData(DB_PAS_META_ID_PREFIX + name.toUpperCase())
  }

  /**
   * 获取数据库所有的表.
   */
  private fun getDatabaseTables(databaseName: String): TableWrapper {
    openDatabase(databaseName)
    val data = mutableListOf<TableInfo>()
    var version = 0
    if (databaseOpen) {
      database?.let {
        var cursor: Cursor? = null
        try {
          cursor = it.rawQuery("SELECT name FROM sqlite_master WHERE type='table' OR type='view' ORDER BY name COLLATE NOCASE", null)
          if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
              val tName = cursor.getString(0)
              if (tName != "android_metadata") {
                data.add(TableInfo(tName, getTableInfo(it, tName)))
              }
              cursor.moveToNext()
            }
          }
          executeSafely { version = it.version }
        } finally {
          executeSafely { cursor?.close() }
          closeDatabase()
        }
      }
    }
    return TableWrapper(version, data)
  }

  /**
   * 获取表的数据信息.
   */
  private fun getTableInfo(db: SQLiteDatabase, tName: String): List<TableFieldInfo> {
    val infos = mutableListOf<TableFieldInfo>()
    var cursor: Cursor? = null
    try {
      cursor = db.rawQuery("PRAGMA table_info('$tName')", null)
      cursor?.let {
        it.moveToFirst()
        if (it.count > 0) {
          do {
            var isPrimary = false
            var name = "Null"
            for (i in 0 until it.columnCount) {
              val columnName = it.getColumnName(i)
              when (columnName) {
                ConstUtils.PK -> isPrimary = it.getInt(i) == 1
                ConstUtils.NAME -> name = it.getString(i)
              }
            }
            infos.add(TableFieldInfo(name, isPrimary))
          } while (it.moveToNext())
        }
        it.close()
      }
    } catch (e: Exception) {

    } finally {
      executeSafely { cursor?.close() }
    }
    return infos
  }

  /**
   * 获取所有SharePreferences.
   */
  private fun getSpTags(): TableWrapper {
    val tags = ArrayList<TableInfo>()
    val rootPath = ctx.applicationInfo.dataDir + "/shared_prefs"
    val root = File(rootPath)
    if (root.exists()) {
      root.listFiles()
          .map { it.name }
          .filter { it.endsWith(".xml") }
          .map { it.substring(0, it.length - 4) }
          .map { TableInfo(it, listOf()) }
          .mapTo(tags) { it }
    }
    return TableWrapper(0, tags)
  }

  /**
   * 打开数据库.
   */
  private fun openDatabase(databaseName: String) {
    closeDatabase()
    SQLiteDatabase.loadLibs(ctx)
    databaseFiles[databaseName]?.let {
      database = SQLiteDatabase.openOrCreateDatabase(it.first, it.second, null)
      databaseOpen = true
    }
  }

  /**
   * 关闭数据库.
   */
  private fun closeDatabase() {
    executeSafely { database?.close() }
    databaseOpen = false
  }

  companion object {
    private const val DB_PAS_META_ID_PREFIX = "DB_PAS_"
  }

}