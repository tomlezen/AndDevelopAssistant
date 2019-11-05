package tomlezen.androiddebuglib.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy

/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-31 14:15
 */
@Dao
interface RoomTestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: RoomTest)

}