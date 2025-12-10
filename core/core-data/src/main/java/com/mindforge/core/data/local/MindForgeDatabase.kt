package com.mindforge.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mindforge.core.data.local.dao.UserDao
import com.mindforge.core.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MindForgeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
