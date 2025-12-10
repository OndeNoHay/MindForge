package com.mindforge.core.data.local.dao

import androidx.room.*
import com.mindforge.core.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET totalXP = totalXP + :xpToAdd WHERE id = :userId")
    suspend fun updateXP(userId: String, xpToAdd: Int)

    @Query("UPDATE users SET currentStreak = :newStreak WHERE id = :userId")
    suspend fun updateStreak(userId: String, newStreak: Int)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}
