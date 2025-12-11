package com.mindforge.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mindforge.core.data.local.dao.GameSessionDao
import com.mindforge.core.data.local.dao.UserDao
import com.mindforge.core.data.local.entity.GameSessionEntity
import com.mindforge.core.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        GameSessionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class MindForgeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gameSessionDao(): GameSessionDao
}
