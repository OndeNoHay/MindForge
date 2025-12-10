package com.mindforge.core.common

/**
 * Application-wide constants
 */
object Constants {
    // Database
    const val DATABASE_NAME = "mindforge_db"
    const val DATABASE_VERSION = 1

    // Shared Preferences
    const val PREFS_NAME = "mindforge_prefs"

    // Game Constants
    const val DEFAULT_DAILY_GOAL_MINUTES = 15
    const val XP_PER_LEVEL = 1000
    const val STREAK_BONUS_MULTIPLIER = 1.5f

    // Difficulty Levels
    const val MAX_DIFFICULTY_LEVEL = 10
    const val MIN_DIFFICULTY_LEVEL = 1

    // Performance Thresholds
    const val ACCURACY_THRESHOLD_UP = 0.8f
    const val ACCURACY_THRESHOLD_DOWN = 0.6f

    // Spaced Repetition
    const val INITIAL_EASINESS_FACTOR = 2.5f
    const val MIN_EASINESS_FACTOR = 1.3f
}
