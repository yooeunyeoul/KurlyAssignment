package com.brady.kurly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brady.kurly.data.local.dao.WishDao
import com.brady.kurly.data.local.entity.WishEntity

@Database(
    entities = [WishEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KurlyDatabase : RoomDatabase() {
    abstract fun wishDao(): WishDao
}
