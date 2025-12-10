package com.mindforge.core.domain.repository

import com.mindforge.core.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Achievement-related operations
 */
interface AchievementRepository {
    /**
     * Get all achievements
     */
    fun getAllAchievements(): Flow<List<Achievement>>

    /**
     * Get unlocked achievements
     */
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    /**
     * Unlock an achievement
     */
    suspend fun unlockAchievement(achievementId: String)

    /**
     * Update achievement progress
     */
    suspend fun updateAchievementProgress(achievementId: String, progress: Float)
}
