package com.tlz.ada.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.content.ContentValues
import android.database.Cursor

/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-30 13:31
 */
class InMemorySQLiteDb(private val db: SupportSQLiteDatabase) : SQLiteDb {

    override val isOpen: Boolean
        get() = db.isOpen

    override val version: Int
        get() = db.version

    override fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int =
            db.delete(table, whereClause, whereArgs)

    override fun close() {
//        db.close()
    }

    override fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor =
            db.query(sql, selectionArgs)

    override fun execSQL(sql: String) {
        db.execSQL(sql)
    }

    override fun insert(table: String, nullColumnHack: String?, values: ContentValues): Long =
            db.insert(table, 0, values)

    override fun update(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>?): Int =
            db.update(table, 0, values, whereClause, whereArgs)
}