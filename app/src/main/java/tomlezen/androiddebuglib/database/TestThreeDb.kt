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
class TestThreeDb(ctx: Context): SQLiteOpenHelper(ctx, "TestThree.db", null, 1) {

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL("create table table1 (id integer primary key, name text, age integer, birthday text, number text, address text, grade text, teacher text)")
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS table1")
    onCreate(db)
  }

  fun init() {
    (0..97).forEach {
      writableDatabase.execSQL("insert into table1 (name, age, birthday, number, address, grade, teacher) values ('name$it', ${Random().nextInt()}, 'birthday$it', 'number$it', 'address$it', ${(Random().nextFloat() * 100).toInt()}, 'teacher$it');")
    }

    close()
  }

  companion object {
    fun create(ctx: Context){
      TestThreeDb(ctx).init()
    }
  }
}