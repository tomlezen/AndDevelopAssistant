package tomlezen.androiddebuglib.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by tomlezen.
 * Data: 2018/2/24.
 * Time: 11:15.
 */
class TestTwoDb(ctx: Context): SQLiteOpenHelper(ctx, "TestTwo.db", null, 1) {

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL("create table table1 (id integer primary key, name text, phone text, email text, street text, place text, createdAt integer)")
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS table1")
    onCreate(db)
  }

  fun init() {
    (0..97).forEach {
      writableDatabase.execSQL("insert into table1 (name, phone, email, street, place, createdAt) values ('name$it', 'phone$it', 'email$it', 'street$it', 'place$it', ${System.currentTimeMillis()});")
    }
  }

  companion object {
    fun create(ctx: Context){
      TestTwoDb(ctx).init()
    }
  }
}