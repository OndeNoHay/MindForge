package com.mindforge.core.domain.model

/**
 * Domain model representing user's overall progress
 */
data class UserProgress(
    val totalXP: Int,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val achievements: List<Achievement>,
    val dailyGoalProgress: Float
) {
    val xpToNextLevel: Int
        get() = ((level + 1) * 1000) - (totalXP % 1000)

    val levelProgress: Float
        get() = (totalXP % 1000) / 1000f
}
