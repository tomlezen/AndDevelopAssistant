package tomlezen.androiddebuglib.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

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