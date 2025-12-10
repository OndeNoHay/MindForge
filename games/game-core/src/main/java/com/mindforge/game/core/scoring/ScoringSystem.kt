package com.mindforge.game.core.scoring

import com.mindforge.core.domain.model.DifficultyLevel
import kotlin.math.max

/**
 * Core scoring system for games
 * Calculates scores based on various factors
 */
class ScoringSystem {

    companion object {
        // Base score values
        const val BASE_SCORE_CORRECT = 100
        const val BASE_SCORE_INCORRECT = -20

        // Time bonus thresholds (in milliseconds)
        const val FAST_RESPONSE_THRESHOLD = 2000L
        const val MEDIUM_RESPONSE_THRESHOLD = 5000L

        // Bonus multipliers
        const val FAST_RESPONSE_MULTIPLIER = 1.5f
        const val MEDIUM_RESPONSE_MULTIPLIER = 1.2f
        const val STREAK_MULTIPLIER_PER_ITEM = 0.1f
        const val MAX_STREAK_MULTIPLIER = 2.0f

        // Accuracy thresholds
        const val PERFECT_ACCURACY = 1.0f
        const val HIGH_ACCURACY = 0.9f
        const val MEDIUM_ACCURACY = 0.7f
    }

    /**
     * Calculate score for a single answer
     *
     * @param isCorrect Whether the answer is correct
     * @param responseTime Time taken to respond (in milliseconds)
     * @param currentStreak Current streak of correct answers
     * @param difficulty Current difficulty level
     * @return Calculated score
     */
    fun calculateAnswerScore(
        isCorrect: Boolean,
        responseTime: Long,
        currentStreak: Int = 0,
        difficulty: DifficultyLevel = DifficultyLevel.BEGINNER
    ): Int {
        if (!isCorrect) {
            return BASE_SCORE_INCORRECT
        }

        var score = BASE_SCORE_CORRECT

        // Apply time bonus
        val timeMultiplier = calculateTimeBonus(responseTime)
        score = (score * timeMultiplier).toInt()

        // Apply streak bonus
        val streakMultiplier = calculateStreakBonus(currentStreak)
        score = (score * streakMultiplier).toInt()

        // Apply difficulty bonus
        val difficultyMultiplier = calculateDifficultyBonus(difficulty)
        score = (score * difficultyMultiplier).toInt()

        return score
    }

    /**
     * Calculate time bonus multiplier
     */
    fun calculateTimeBonus(responseTime: Long): Float {
        return when {
            responseTime <= FAST_RESPONSE_THRESHOLD -> FAST_RESPONSE_MULTIPLIER
            responseTime <= MEDIUM_RESPONSE_THRESHOLD -> MEDIUM_RESPONSE_MULTIPLIER
            else -> 1.0f
        }
    }

    /**
     * Calculate streak bonus multiplier
     */
    fun calculateStreakBonus(streak: Int): Float {
        val multiplier = 1.0f + (streak * STREAK_MULTIPLIER_PER_ITEM)
        return multiplier.coerceAtMost(MAX_STREAK_MULTIPLIER)
    }

    /**
     * Calculate difficulty bonus multiplier
     */
    fun calculateDifficultyBonus(difficulty: DifficultyLevel): Float {
        return when (difficulty) {
            DifficultyLevel.BEGINNER -> 1.0f
            DifficultyLevel.EASY -> 1.2f
            DifficultyLevel.MEDIUM -> 1.4f
            DifficultyLevel.HARD -> 1.6f
            DifficultyLevel.EXPERT -> 1.8f
            DifficultyLevel.MASTER -> 2.0f
            DifficultyLevel.GRANDMASTER -> 2.3f
            DifficultyLevel.LEGENDARY -> 2.6f
            DifficultyLevel.MYTHIC -> 3.0f
            DifficultyLevel.DIVINE -> 3.5f
        }
    }

    /**
     * Calculate final session XP based on performance
     *
     * @param totalScore Total score earned
     * @param accuracy Accuracy percentage (0.0 to 1.0)
     * @param difficulty Difficulty level
     * @param timeTaken Total time taken (in milliseconds)
     * @return XP earned
     */
    fun calculateSessionXP(
        totalScore: Int,
        accuracy: Float,
        difficulty: DifficultyLevel,
        timeTaken: Long
    ): Int {
        // Base XP from score
        var xp = (totalScore * 0.5f).toInt()

        // Accuracy bonus
        val accuracyBonus = when {
            accuracy >= PERFECT_ACCURACY -> 1.5f
            accuracy >= HIGH_ACCURACY -> 1.3f
            accuracy >= MEDIUM_ACCURACY -> 1.1f
            else -> 1.0f
        }
        xp = (xp * accuracyBonus).toInt()

        // Difficulty bonus
        val difficultyBonus = calculateDifficultyBonus(difficulty)
        xp = (xp * difficultyBonus).toInt()

        // Minimum XP for participation
        return max(xp, 10)
    }

    /**
     * Calculate accuracy percentage
     *
     * @param correctAnswers Number of correct answers
     * @param totalAnswers Total number of answers
     * @return Accuracy as a float between 0.0 and 1.0
     */
    fun calculateAccuracy(correctAnswers: Int, totalAnswers: Int): Float {
        if (totalAnswers == 0) return 0f
        return correctAnswers.toFloat() / totalAnswers.toFloat()
    }

    /**
     * Determine if the player passed based on accuracy
     *
     * @param accuracy Accuracy percentage
     * @param minimumAccuracy Minimum required accuracy (default 0.6)
     * @return True if passed
     */
    fun hasPassed(accuracy: Float, minimumAccuracy: Float = 0.6f): Boolean {
        return accuracy >= minimumAccuracy
    }
}
