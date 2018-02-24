package tomlezen.androiddebuglib.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

/**
 * Created by tomlezen.
 * Data: 2018/2/24.
 * Time: 11:15.
 */
class TestOneDb(ctx: Context): SQLiteOpenHelper(ctx, "TestOne.db", null, 1) {

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL("create table table1 (id integer primary key, field1 text, field2 text, field3 real, field4 blob)")
    db.execSQL("create table table2 (id integer primary key, field1 text)")
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS table1")
    db.execSQL("DROP TABLE IF EXISTS table2")
    onCreate(db)
  }

  fun init() {
    readableDatabase.version
    (0..55).forEach {
      writableDatabase.execSQL("insert into table1 (field1, field2, field3) values ('value$it', 'content$it', ${Random().nextFloat()});")
    }
    (0..9).forEach {
      writableDatabase.execSQL("insert into table2 (field1) values ('hello$it');")
    }
  }

  companion object {
    fun create(ctx: Context){
      TestOneDb(ctx).init()
    }
  }

}