package tomlezen.androiddebuglib.database

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

/**
 * Created by tomlezen.
 * Data: 2018/2/24.
 * Time: 11:01.
 */
class CustomDB(ctx: Context): SQLiteOpenHelper(CustomDatabasePathContext(ctx), "Custom.db", null, 10) {

  private val tableName = "test"

  override fun onCreate(db: SQLiteDatabase?) {
    db?.execSQL("create table $tableName (id integer primary key, key text, value integer)")
  }

  override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
    db?.execSQL("DROP TABLE IF EXISTS $tableName")
    onCreate(db)
  }

  fun init(){
    (1..50).forEach {
      val contentValues = ContentValues()
      contentValues.put("key", "key$it")
      contentValues.put("value", "$it")
      writableDatabase.insert(tableName, null, contentValues)
    }

    close()
  }

  private class CustomDatabasePathContext(base: Context) : ContextWrapper(base) {

    override fun getDatabasePath(name: String): File {
      val databaseDir = File(String.format("%s/%s", filesDir, "custom_dir"))
      databaseDir.mkdirs()
      return File(String.format("%s/%s/%s", filesDir, "custom_dir", name))
    }

    override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase {
      return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null)
    }

    override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?): SQLiteDatabase {
      return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null)
    }
  }

  companion object {
    fun create(ctx: Context){
      CustomDB(ctx).init()
    }
  }

}