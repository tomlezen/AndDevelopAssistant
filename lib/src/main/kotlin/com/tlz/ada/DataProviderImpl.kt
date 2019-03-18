package com.tlz.ada

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.tlz.ada.models.KeyValue
import com.tlz.ada.models.TableFieldInfo
import com.tlz.ada.models.TableInfo
import com.tlz.ada.models.TableWrapper
import net.sqlcipher.Cursor
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import java.util.*
import android.util.Pair

/**
 * Created by tomlezen.
 * Data: 2018/1/27.
 * Time: 16:01.
 */
class DataProviderImpl(private val ctx: Context, private val gson: Gson) : DataProvider {

	private var database: SQLiteDatabase? = null
	private var databaseOpen = false

	private var tableWrapperMap = mutableMapOf<String, TableWrapper>()

	private val databaseFiles: MutableMap<String, Pair<File, String>> by lazy { initDatabaseFiles() }

	override fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
		databaseFiles.putAll(files)
	}

	override fun getDatabaseList(): List<String> {
		val data = mutableListOf<String>()
		databaseFiles.keys.mapTo(data) { it }
		return data
	}

	override fun getDatabaseFile(dName: String): File? {
		return databaseFiles[dName]?.first
	}

	override fun getAllTable(databaseName: String): TableWrapper {
		val wrapper = if (databaseName == ConstUtils.PREFS) getSpTags() else getDatabaseTables(databaseName)
		tableWrapperMap[databaseName] = wrapper
		return wrapper
	}

	override fun executeQuery(dName: String, tName: String, sql: String): List<Any> {
		val data = mutableListOf<Any>()
		if (dName.isPrefs()) {
			val sharePreferences = ctx.getSharedPreferences(tName, Context.MODE_PRIVATE)
			for (entry in sharePreferences.all.entries) {
				val type = when {
					entry.value is String -> ConstUtils.TYPE_TEXT
					entry.value is Int -> ConstUtils.TYPE_INTEGER
					entry.value is Long -> ConstUtils.TYPE_LONG
					entry.value is Float -> ConstUtils.TYPE_FLOAT
					entry.value is Boolean -> ConstUtils.TYPE_BOOLEAN
					entry.value is Set<*> -> ConstUtils.TYPE_STRING_SET
					else -> ConstUtils.TYPE_TEXT
				}
				data.add(KeyValue(entry.key, entry.value?.toString() ?: "null", type))
			}
			if (sql.isNotBlank()) {
				if (sql == "asc") {
					data.sortWith(Comparator { o1, o2 -> if ((o1 as KeyValue).key > (o2 as KeyValue).key) 1 else -1 })
				} else {
					data.sortWith(Comparator { o1, o2 -> if ((o1 as KeyValue).key > (o2 as KeyValue).key) -1 else 1 })
				}
			}
		} else {
			openDatabase(dName)
			database?.let { db ->
				var cursor: Cursor? = null
				try {
					cursor = db.rawQuery(sql, null)
					cursor?.let { cur ->
						val columnCount = cur.columnCount
						while (cur.moveToNext()) {
							val columnData = mutableMapOf<String, String>()
							(0 until columnCount).forEach {
								try {
									columnData[cur.getColumnName(it)] = cur.getString(it) ?: "null"
								} catch (e: Exception) {
									columnData[cur.getColumnName(it)] = (cur.getBlob(it)?.contentToString() ?: "null")
								}
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
		}
		return data
	}

	override fun executeSql(dName: String, sql: String): Any {
		openDatabase(dName)
		return database?.let {
			try {
				if (sql.toLowerCase().startsWith("select ")) {
					val cursor = it.rawQuery(sql, null)
					cursor.moveToNext()
					return cursor.getInt(0)
				} else {
					it.execSQL(sql)
				}
			} finally {
				closeDatabase()
			}
			true
		} ?: false
	}

	override fun deleteRow(dName: String, tName: String, where: String): Boolean {
		return if (dName.isPrefs()) {
			ctx.getSharedPreferences(tName, Context.MODE_PRIVATE).edit().remove(where).apply()
			true
		} else {
			openDatabase(dName)
			database?.let {
				try {
					it.delete(tName, where, null)
				} finally {
					closeDatabase()
				}
				true
			} ?: false
		}
	}

	override fun updateRow(dName: String, tName: String, content: Array<KeyValue>, where: String): Boolean {
		if (dName.isPrefs()) {
			val editor = ctx.getSharedPreferences(tName, Context.MODE_PRIVATE).edit()
			val keyValue = content.first()
			when (keyValue.type) {
				ConstUtils.TYPE_INTEGER -> editor.putInt(keyValue.key, keyValue.value!!.toInt())
				ConstUtils.TYPE_FLOAT -> editor.putFloat(keyValue.key, keyValue.value!!.toFloat())
				ConstUtils.TYPE_LONG -> editor.putLong(keyValue.key, keyValue.value!!.toLong())
				ConstUtils.TYPE_BOOLEAN -> editor.putBoolean(keyValue.key, keyValue.value!!.toBoolean())
				ConstUtils.TYPE_STRING_SET -> editor.putStringSet(keyValue.key, gson.fromJson<Array<String>>(keyValue.value, Array<String>::class.java).let { it ->
					val set = mutableSetOf<String>()
					it.mapTo(set) { it }
					set
				})
				else -> editor.putString(keyValue.key, keyValue.value)
			}
			editor.apply()
			return true
		} else {
			openDatabase(dName)
			return database?.let { db ->
				try {
					val contentValues = ContentValues()
					content.forEach {
						when (it.type) {
							ConstUtils.TYPE_INTEGER -> contentValues.put(it.key, it.value?.toIntOrNull())
							ConstUtils.TYPE_REAL -> contentValues.put(it.key, it.value?.toDoubleOrNull())
							else -> contentValues.put(it.key, it.value)
						}
					}
					db.update(tName, contentValues, where, null) == 1
				} finally {
					closeDatabase()
				}
			} ?: false
		}
	}

	override fun addRow(dName: String, tName: String, content: Array<KeyValue>): Boolean {
		openDatabase(dName)
		database?.let {
			try {
				val contentValues = ContentValues()
				content.forEach { keyValue ->
					when (keyValue.type) {
						ConstUtils.TYPE_INTEGER -> contentValues.put(keyValue.key, keyValue.value?.toIntOrNull())
						ConstUtils.TYPE_REAL -> contentValues.put(keyValue.key, keyValue.value?.toDoubleOrNull())
						else -> contentValues.put(keyValue.key, keyValue.value)
					}
				}
				return it.insertOrThrow(tName, null, contentValues) >= 0
			} finally {
				closeDatabase()
			}
		}
		return false
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
				cursor?.let { cur ->
					while (cur.moveToNext()) {
						return cur.getInt(0)
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
	private fun initDatabaseFiles(): MutableMap<String, Pair<File, String>> {
		val databaseFiles = HashMap<String, Pair<File, String>>()
		executeSafely {
			for (databaseName in ctx.databaseList()) {
				if (!databaseName.contains("-journal")) {
					val password = getDatabasePassword(databaseName)
					databaseFiles[databaseName] = Pair(ctx.getDatabasePath(databaseName), password)
				}
			}
		}

		//最后加载SharePreferences
		databaseFiles[ConstUtils.PREFS] = Pair(File(""), "")

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
					executeSafely { version = it.version }
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
	@SuppressLint("LongLogTag")
	private fun getTableInfo(db: SQLiteDatabase, tName: String): List<TableFieldInfo> {
		val data = mutableListOf<TableFieldInfo>()
		var cursor: Cursor? = null
		try {
			cursor = db.rawQuery("PRAGMA table_info('$tName')", null)
			cursor?.let {
				it.moveToFirst()
				if (it.count > 0) {
					do {
						var isPrimary = false
						var type = "Null"
						var name = "Null"
						var nullable = false
						var defValue: String? = null
						executeSafely {
							for (i in 0 until it.columnCount) {
								val columnName = it.getColumnName(i)
								when (columnName) {
									ConstUtils.PK -> isPrimary = it.getInt(i) == 1
									ConstUtils.TYPE -> type = it.getString(i)
									ConstUtils.NAME -> name = it.getString(i)
									ConstUtils.NULLABLE -> nullable = it.getInt(i) == 1
									ConstUtils.DEF_VALUE -> defValue = it.getString(i)
								}
							}
						}
						data.add(TableFieldInfo(name, type, isPrimary, nullable, defValue))
					} while (it.moveToNext())
				}
				it.close()
			}
		} catch (e: Exception) {
			Log.e(AndDevelopAssistantWebServer.TAG, "Android调试辅助初始化失败")
		} finally {
			executeSafely { cursor?.close() }
			closeDatabase()
		}
		return data
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

	private fun String.isPrefs() = this == "SharePreferences"

	companion object {
		private const val DB_PAS_META_ID_PREFIX = "DB_PAS_"
	}

}