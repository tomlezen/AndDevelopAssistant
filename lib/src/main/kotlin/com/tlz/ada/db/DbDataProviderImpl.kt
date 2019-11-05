package com.tlz.ada.db

import android.content.ContentValues
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tlz.ada.AdaConstUtils
import com.tlz.ada.exceptions.AdaException
import com.tlz.ada.metaData
import com.tlz.ada.models.KeyValue
import com.tlz.ada.models.Table
import com.tlz.ada.models.TableFieldInfo
import com.tlz.ada.models.TableInfo
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 数据库数据处理.
 * Created by Tomlezen.
 * Date: 2019-06-09.
 * Time: 22:43.
 */
class DbDataProviderImpl(private val ctx: Context) : AdaDataProvider {

    /** 所有数据库文件. */
    private val databaseFiles = ConcurrentHashMap<String, Pair<File, String>>()
    /** 内存数据库. */
    private val inMemoryDatabases = ConcurrentHashMap<String, SupportSQLiteDatabase>()
    /** 所有的表信息. */
    private val tables = ConcurrentHashMap<String, Table>()

    private val sqliteDbs = ConcurrentHashMap<String, SQLiteDb>()

    init {
        loadDatabaseFile()
    }

    override fun setCustomDatabaseFiles(files: Map<String, Pair<File, String>>) {
        files.forEach {
            databaseFiles[it.key] = it.value
        }
    }

    override fun setInMemoryRoomDatabases(databases: Map<String, SupportSQLiteDatabase>) {
        databases.forEach {
            inMemoryDatabases[it.key] = it.value
            databaseFiles[it.key] = File(it.value.path) to ""
        }
    }

    override fun getAllDatabase(): List<String> = databaseFiles.keys().toList()

    override fun getDatabaseFile(dName: String): File? = databaseFiles[dName]?.first

    override fun getAllTable(dName: String): Table =
            dName.open {
                val data = mutableListOf<TableInfo>()
                rawQuery("SELECT name FROM sqlite_master WHERE type='table' OR type='view' ORDER BY name COLLATE NOCASE", null)
                        .use { cursor ->
                            if (cursor.moveToFirst()) {
                                while (!cursor.isAfterLast) {
                                    val tName = cursor.getString(0)
                                    if (tName != "android_metadata") {
                                        data += TableInfo(tName, getTableFieldInfo(this, tName))
                                    }
                                    cursor.moveToNext()
                                }
                            }
                        }
                Table(runCatching { version }.getOrNull() ?: 0, data)
            }.also {
                tables[dName] = it
            }

    override fun getTableInfo(dName: String, tName: String): TableInfo {
        if (tables.isEmpty()) {
            databaseFiles.forEach { getAllTable(it.key) }
        }
        return tables[dName]?.tableInfos?.find { it.name == tName }
                ?: throw AdaException("不存在该表信息: dName=$dName;tName=$tName")
    }

    override fun getTableDataCount(dName: String, tName: String, where: String): Int =
            dName.open {
                var sql = "select count(*) from $tName"
                if (where.isNotBlank()) {
                    sql += " where $where"
                }
                rawQuery(sql, null)
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                return@open cursor.getInt(0)
                            }
                        }
                0
            }

    override fun query(dName: String, tName: String, sql: String): List<Any> =
            dName.open {
                rawQuery(sql, null)
                        .use { cursor ->
                            val data = mutableListOf<Any>()
                            val columnCount = cursor.columnCount
                            while (cursor.moveToNext()) {
                                val columnData = mutableMapOf<String, String>()
                                (0 until columnCount).forEach { columnIndex ->
                                    runCatching {
                                        columnData[cursor.getColumnName(columnIndex)] = cursor.getString(columnIndex)
                                                ?: "null"
                                    }.onFailure {
                                        columnData[cursor.getColumnName(columnIndex)] = (cursor.getBlob(columnIndex)?.contentToString()
                                                ?: "null")
                                    }
                                }
                                data += columnData
                            }
                            data
                        }
            }

    override fun rawQuery(dName: String, sql: String): Any =
            dName.open {
                if (sql.toLowerCase(Locale.getDefault()).startsWith("select ")) {
                    rawQuery(sql, null)
                            .use { cursor ->
                                cursor.moveToNext()
                                cursor.getInt(0)
                            }
                } else {
                    execSQL(sql)
                }
            }

    override fun add(dName: String, tName: String, content: Array<KeyValue>): Boolean =
            dName.open {
                val contentValues = ContentValues()
                content.forEach { kv ->
                    when (kv.type) {
                        AdaConstUtils.TYPE_INTEGER -> contentValues.put(kv.key, kv.value?.toIntOrNull())
                        AdaConstUtils.TYPE_REAL -> contentValues.put(kv.key, kv.value?.toDoubleOrNull())
                        else -> contentValues.put(kv.key, kv.value)
                    }
                }
                insert(tName, null, contentValues) >= 0
            }

    override fun delete(dName: String, tName: String, where: String): Boolean =
            dName.open {
                delete(tName, where, null) > 0
            }

    override fun update(dName: String, tName: String, content: Array<KeyValue>, where: String): Boolean =
            dName.open {
                val contentValues = ContentValues()
                content.forEach { kv ->
                    when (kv.type) {
                        AdaConstUtils.TYPE_INTEGER -> contentValues.put(kv.key, kv.value?.toIntOrNull())
                        AdaConstUtils.TYPE_REAL -> contentValues.put(kv.key, kv.value?.toDoubleOrNull())
                        else -> contentValues.put(kv.key, kv.value)
                    }
                }
                update(tName, contentValues, where, null) == 1
            }

    /**
     * 加载数据库文件.
     */
    private fun loadDatabaseFile() {
        runCatching {
            ctx.databaseList()
                    .filter { !it.contains("-journal") }
                    .map { Triple(it, ctx.getDatabasePath(it), getDatabasePassword(it)) }
                    .forEach {
                        databaseFiles[it.first] = it.second to it.third
                    }
        }
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
        return ctx.metaData(DB_PAS_META_ID_PREFIX + name.toUpperCase(Locale.getDefault()))
    }

    /**
     * 获取表字段信息.
     * @param db SQLiteDb
     * @param tName String
     * @return List<TableFieldInfo>
     */
    private fun getTableFieldInfo(db: SQLiteDb, tName: String): List<TableFieldInfo> {
        val data = mutableListOf<TableFieldInfo>()
        db.rawQuery("PRAGMA table_info('$tName')", null)
                .use {
                    it.moveToFirst()
                    if (it.count > 0) {
                        do {
                            var isPrimary = false
                            var type = "Null"
                            var name = "Null"
                            var nullable = false
                            var defValue: String? = null
                            runCatching {
                                for (i in 0 until it.columnCount) {
                                    when (it.getColumnName(i)) {
                                        AdaConstUtils.PK -> isPrimary = it.getInt(i) == 1
                                        AdaConstUtils.TYPE -> type = it.getString(i)
                                        AdaConstUtils.NAME -> name = it.getString(i)
                                        AdaConstUtils.NULLABLE -> nullable = it.getInt(i) == 1
                                        AdaConstUtils.DEF_VALUE -> defValue = it.getString(i)
                                    }
                                }
                            }
                            data += TableFieldInfo(name, type, isPrimary, nullable, defValue)
                        } while (it.moveToNext())
                    }
                }
        return data
    }

    private fun <T> String.open(block: SQLiteDb.() -> T): T =
            (sqliteDbs[this] ?: create().also {
                sqliteDbs[this] = it
            }).let {
                try {
                    block.invoke(it)
                } finally {
                    it.close()
                }
            }

    private fun String.create(): SQLiteDb {
        if (inMemoryDatabases.containsKey(this)) return InMemorySQLiteDb(inMemoryDatabases[this]!!)
        return NormalSQLiteDb(ctx, databaseFiles[this]?.first
                ?: throw AdaException("不存在该数据库"), this, getDatabasePassword(this))
    }

    companion object {
        private const val DB_PAS_META_ID_PREFIX = "DB_PAS_"
    }
}