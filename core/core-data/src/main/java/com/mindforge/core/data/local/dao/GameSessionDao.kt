package com.mindforge.core.data.local.dao

import androidx.room.*
import com.mindforge.core.data.local.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getUserSessions(userId: String): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_sessions WHERE userId = :userId AND completedAt >= :startOfDay")
    fun getTodaySessions(userId: String, startOfDay: Long): Flow<List<GameSessionEntity>>

    @Query("SELECT COUNT(*) FROM game_sessions WHERE userId = :userId AND completedAt >= :startOfDay")
    suspend fun getTodaySessionCount(userId: String, startOfDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity)

    @Query("DELETE FROM game_sessions WHERE userId = :userId")
    suspend fun deleteUserSessions(userId: String)

    @Query("SELECT * FROM game_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): GameSessionEntity?
}
