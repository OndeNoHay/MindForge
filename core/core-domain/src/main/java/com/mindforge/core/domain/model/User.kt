package com.mindforge.core.domain.model

/**
 * Domain model for User
 */
data class User(
    val id: String,
    val name: String,
    val totalXP: Int,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long,
    val dailyGoal: Int,
    val preferredDifficulty: DifficultyLevel
)
