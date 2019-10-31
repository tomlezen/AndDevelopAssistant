package tomlezen.androiddebuglib.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Author: tomlezen
 * Email: tomlezen@protonmail.com
 * Date: 2019-10-31 14:13
 */
@Entity
data class RoomTest(
        @PrimaryKey
        private val id: Int,
        private val test1: String,
        private val test2: Int
)