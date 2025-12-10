package com.mindforge.game.core.difficulty

import com.mindforge.core.common.Constants
import com.mindforge.core.domain.model.DifficultyLevel

/**
 * Manages adaptive difficulty based on player performance
 * Adjusts difficulty automatically to maintain optimal challenge
 */
class AdaptiveDifficultyManager {

    companion object {
        // Accuracy thresholds for difficulty adjustment
        const val ACCURACY_THRESHOLD_UP = Constants.ACCURACY_THRESHOLD_UP // 0.8f
        const val ACCURACY_THRESHOLD_DOWN = Constants.ACCURACY_THRESHOLD_DOWN // 0.6f

        // Response time thresholds (in milliseconds)
        const val FAST_RESPONSE_THRESHOLD = 3000L
        const val SLOW_RESPONSE_THRESHOLD = 10000L

        // Streak thresholds
        const val STREAK_THRESHOLD_UP = 5
        const val STREAK_THRESHOLD_DOWN = 3
    }

    /**
     * Calculate the next difficulty level based on performance metrics
     *
     * @param currentLevel Current difficulty level
     * @param accuracy Accuracy percentage (0.0 to 1.0)
     * @param responseTime Average response time in milliseconds
     * @param streak Current streak of correct/incorrect answers
     * @return Recommended next difficulty level
     */
    fun calculateNextDifficulty(
        currentLevel: DifficultyLevel,
        accuracy: Float,
        responseTime: Long,
        streak: Int = 0
    ): DifficultyLevel {
        val shouldIncrease = shouldIncreaseDifficulty(accuracy, responseTime, streak)
        val shouldDecrease = shouldDecreaseDifficulty(accuracy, responseTime, streak)

        return when {
            shouldIncrease -> increaseDifficulty(currentLevel)
            shouldDecrease -> decreaseDifficulty(currentLevel)
            else -> currentLevel
        }
    }

    /**
     * Determine if difficulty should be increased
     */
    private fun shouldIncreaseDifficulty(
        accuracy: Float,
        responseTime: Long,
        streak: Int
    ): Boolean {
        // High accuracy + fast response + good streak = increase difficulty
        val highAccuracy = accuracy >= ACCURACY_THRESHOLD_UP
        val fastResponse = responseTime < FAST_RESPONSE_THRESHOLD
        val goodStreak = streak >= STREAK_THRESHOLD_UP

        return highAccuracy && (fastResponse || goodStreak)
    }

    /**
     * Determine if difficulty should be decreased
     */
    private fun shouldDecreaseDifficulty(
        accuracy: Float,
        responseTime: Long,
        streak: Int
    ): Boolean {
        // Low accuracy or very slow response = decrease difficulty
        val lowAccuracy = accuracy < ACCURACY_THRESHOLD_DOWN
        val slowResponse = responseTime > SLOW_RESPONSE_THRESHOLD
        val poorStreak = streak <= -STREAK_THRESHOLD_DOWN

        return lowAccuracy || slowResponse || poorStreak
    }

    /**
     * Increase difficulty level by one step
     */
    fun increaseDifficulty(currentLevel: DifficultyLevel): DifficultyLevel {
        val currentValue = currentLevel.value
        if (currentValue >= Constants.MAX_DIFFICULTY_LEVEL) {
            return currentLevel
        }
        return DifficultyLevel.fromValue(currentValue + 1)
    }

    /**
     * Decrease difficulty level by one step
     */
    fun decreaseDifficulty(currentLevel: DifficultyLevel): DifficultyLevel {
        val currentValue = currentLevel.value
        if (currentValue <= Constants.MIN_DIFFICULTY_LEVEL) {
            return currentLevel
        }
        return DifficultyLevel.fromValue(currentValue - 1)
    }

    /**
     * Calculate difficulty parameters for a specific game
     * This is a template that games can override
     *
     * @param difficulty The difficulty level
     * @return Map of parameter names to values
     */
    fun getDifficultyParameters(difficulty: DifficultyLevel): DifficultyParameters {
        return when (difficulty) {
            DifficultyLevel.BEGINNER -> DifficultyParameters(
                itemCount = 3,
                timeLimit = 5000L,
                complexity = 1
            )
            DifficultyLevel.EASY -> DifficultyParameters(
                itemCount = 4,
                timeLimit = 4500L,
                complexity = 2
            )
            DifficultyLevel.MEDIUM -> DifficultyParameters(
                itemCount = 5,
                timeLimit = 4000L,
                complexity = 3
            )
            DifficultyLevel.HARD -> DifficultyParameters(
                itemCount = 6,
                timeLimit = 3500L,
                complexity = 4
            )
            DifficultyLevel.EXPERT -> DifficultyParameters(
                itemCount = 7,
                timeLimit = 3000L,
                complexity = 5
            )
            DifficultyLevel.MASTER -> DifficultyParameters(
                itemCount = 8,
                timeLimit = 2500L,
                complexity = 6
            )
            DifficultyLevel.GRANDMASTER -> DifficultyParameters(
                itemCount = 9,
                timeLimit = 2000L,
                complexity = 7
            )
            DifficultyLevel.LEGENDARY -> DifficultyParameters(
                itemCount = 10,
                timeLimit = 1800L,
                complexity = 8
            )
            DifficultyLevel.MYTHIC -> DifficultyParameters(
                itemCount = 12,
                timeLimit = 1500L,
                complexity = 9
            )
            DifficultyLevel.DIVINE -> DifficultyParameters(
                itemCount = 15,
                timeLimit = 1200L,
                complexity = 10
            )
        }
    }
}

/**
 * Data class holding difficulty parameters
 * Games can extend this to add specific parameters
 */
data class DifficultyParameters(
    val itemCount: Int,
    val timeLimit: Long,
    val complexity: Int
)
