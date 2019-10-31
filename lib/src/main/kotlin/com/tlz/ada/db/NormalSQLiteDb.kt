package com.tlz.ada.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-31 13:16
 */
class NormalSQLiteDb(private val ctx: Context, private val dbFile: File, private val dName: String, private val password: String?) : SQLiteDb {

    override val isOpen: Boolean
        get() = false

    override val version: Int
        get() = open {
            version
        }

    override fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int =
            open {
                delete(table, whereClause, whereArgs)
            }

    override fun close() {

    }

    override fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor =
            open {
                rawQuery(sql, selectionArgs)
            }

    override fun execSQL(sql: String) {
        open {
            execSQL(sql)
        }
    }

    override fun insert(table: String, nullColumnHack: String?, values: ContentValues): Long =
            open {
                insert(table, nullColumnHack, values)
            }

    override fun update(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>?): Int =
            open {
                update(table, values, whereClause, whereArgs)
            }

    /**
     * 打开数据库.
     * @param block [@kotlin.ExtensionFunctionType] Function1<SQLiteDatabase, T>
     * @return T
     */
    private fun <T> open(block: SQLiteDatabase.() -> T): T {
        SQLiteDatabase.loadLibs(ctx)
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, if (password.isNullOrEmpty()) null else password, null)
        return try {
            db.beginTransaction()
            block.invoke(db).apply {
                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
            db.close()
        }
    }

}