package tomlezen.androiddebuglib.room

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase


/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-31 14:17
 */
class RoomDBTestHelper(ctx: Context) {

    private val db = Room.databaseBuilder(ctx, TestRoomDatabase::class.java, "RoomTest.db")
            .allowMainThreadQueries()
            .build()

    private val _inMemoryAppDatabase = Room.inMemoryDatabaseBuilder(ctx, TestRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    val inMemoryAppDatabase: SupportSQLiteDatabase
        get() = _inMemoryAppDatabase.openHelper.writableDatabase

    val name: String
        get() = _inMemoryAppDatabase.openHelper.databaseName

    fun init() {
        (0 until 100).forEach {
            db.roomTestDao().insert(RoomTest(it, "test$it", 1000 + it))
        }
    }
}