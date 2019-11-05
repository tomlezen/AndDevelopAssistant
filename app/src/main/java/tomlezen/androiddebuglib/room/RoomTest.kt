package tomlezen.androiddebuglib.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-31 14:13
 */
@Entity
data class RoomTest(
    @PrimaryKey
    val id: Int,
    val test1: String,
    val test2: Int
)