package com.mindforge.core.data.repository

import com.mindforge.core.data.local.dao.GameSessionDao
import com.mindforge.core.data.local.entity.GameSessionEntity
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.GameSession
import com.mindforge.core.domain.model.GameType
import com.mindforge.core.domain.repository.GameSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameSessionRepositoryImpl @Inject constructor(
    private val gameSessionDao: GameSessionDao
) : GameSessionRepository {

    private val defaultUserId = "default_user" // TODO: Get from auth or user service

    override suspend fun saveSession(session: GameSession) {
        val entity = session.toEntity(defaultUserId)
        gameSessionDao.insertSession(entity)
    }

    override fun getSessionsByGameType(gameType: GameType): Flow<List<GameSession>> {
        return gameSessionDao.getUserSessions(defaultUserId)
            .map { entities ->
                entities
                    .filter { it.gameType == gameType.name }
                    .map { it.toDomain() }
            }
    }

    override fun getRecentSessions(limit: Int): Flow<List<GameSession>> {
        return gameSessionDao.getUserSessions(defaultUserId)
            .map { entities ->
                entities.take(limit).map { it.toDomain() }
            }
    }

    override suspend fun getSessionById(sessionId: String): GameSession? {
        return gameSessionDao.getSessionById(sessionId)?.toDomain()
    }

    override suspend fun getTotalXPEarned(): Int {
        // This would need a new DAO query
        return 0 // TODO: Implement
    }
}

// Extension functions for mapping
private fun GameSessionEntity.toDomain(): GameSession {
    return GameSession(
        id = id,
        gameType = GameType.valueOf(gameType),
        startTime = startTime,
        endTime = endTime,
        score = score,
        accuracy = accuracy,
        difficultyLevel = DifficultyLevel.valueOf(difficulty),
        xpEarned = xpEarned
    )
}

private fun GameSession.toEntity(userId: String): GameSessionEntity {
    return GameSessionEntity(
        id = id,
        userId = userId,
        gameType = gameType.name,
        startTime = startTime,
        endTime = endTime,
        score = score,
        xpEarned = xpEarned,
        accuracy = accuracy,
        difficulty = difficultyLevel.name,
        passed = accuracy >= 0.6f,
        completedAt = endTime
    )
}
