package com.tlz.ada.db

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException


/**
 * Created by Tomlezen.
 * Date: 2019-10-28.
 * Time: 21:53.
 */
interface SQLiteDb {

    val isOpen: Boolean

    val version: Int

    fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int

    fun close()

    fun rawQuery(sql: String, selectionArgs: Array<String>? = null): Cursor

    @Throws(SQLException::class)
    fun execSQL(sql: String)

    fun insert(table: String, nullColumnHack: String?, values: ContentValues): Long

    fun update(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>?): Int
}