package com.mindforge.core.domain.repository

import com.mindforge.core.domain.model.GameSession
import com.mindforge.core.domain.model.GameType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for GameSession-related operations
 */
interface GameSessionRepository {
    /**
     * Save a game session
     */
    suspend fun saveSession(session: GameSession)

    /**
     * Get all sessions for a specific game type
     */
    fun getSessionsByGameType(gameType: GameType): Flow<List<GameSession>>

    /**
     * Get recent sessions (last N sessions)
     */
    fun getRecentSessions(limit: Int = 10): Flow<List<GameSession>>

    /**
     * Get session by ID
     */
    suspend fun getSessionById(sessionId: String): GameSession?

    /**
     * Get total XP earned across all sessions
     */
    suspend fun getTotalXPEarned(): Int

    /**
     * Get today's session count for a specific user
     */
    suspend fun getTodaySessionCount(userId: String): Int
}
