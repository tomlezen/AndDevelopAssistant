package tomlezen.androiddebuglib.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-31 14:05
 */
@Database(entities = [RoomTest::class], version = 1, exportSchema = false)
abstract class TestRoomDatabase: RoomDatabase() {

    abstract fun roomTestDao(): RoomTestDao

}