package tomlezen.androiddebuglib.room

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.content.Context


/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-31 14:17
 */
class RoomDBTestHelper(ctx: Context) {

    private val roomAppDatabase = Room.databaseBuilder(ctx, TestRoomDatabase::class.java, "RoomTest")
            .allowMainThreadQueries()
            .build()

    private val _inMemoryAppDatabase = Room.inMemoryDatabaseBuilder(ctx, TestRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    val inMemoryAppDatabase: SupportSQLiteDatabase
        get() = _inMemoryAppDatabase.openHelper.writableDatabase

    val name: String
        get() = _inMemoryAppDatabase.openHelper.databaseName ?: "MemoryTest.db"

    fun init() {
        (0 until 100).forEach {
            _inMemoryAppDatabase.roomTestDao().insert(RoomTest(it, "test$it", 1000 + it))
        }
        (0 until 100).forEach {
            roomAppDatabase.roomTestDao().insert(RoomTest(it, "test$it", 1000 + it))
        }
    }
}