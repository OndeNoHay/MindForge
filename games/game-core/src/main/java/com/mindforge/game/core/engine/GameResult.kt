package com.mindforge.game.core.engine

import com.mindforge.core.domain.model.DifficultyLevel

/**
 * Base interface for game results
 * All game results must implement this interface
 */
interface GameResult {
    /**
     * Final score achieved
     */
    val score: Int

    /**
     * Accuracy percentage (0.0 to 1.0)
     */
    val accuracy: Float

    /**
     * Time taken to complete (in milliseconds)
     */
    val timeTaken: Long

    /**
     * Difficulty level played
     */
    val difficulty: DifficultyLevel

    /**
     * XP earned from this session
     */
    val xpEarned: Int

    /**
     * Whether the player passed/succeeded
     */
    val passed: Boolean
}

/**
 * Default implementation of GameResult
 */
data class DefaultGameResult(
    override val score: Int,
    override val accuracy: Float,
    override val timeTaken: Long,
    override val difficulty: DifficultyLevel,
    override val xpEarned: Int,
    override val passed: Boolean = accuracy >= 0.6f
) : GameResult
