package com.mindforge.game.core.engine

import com.mindforge.core.domain.model.DifficultyLevel

/**
 * Base interface for game state
 * All game states must implement this interface
 */
interface GameState {
    /**
     * Current phase of the game
     */
    val phase: GamePhase

    /**
     * Current score
     */
    val score: Int

    /**
     * Time remaining in milliseconds (null if no time limit)
     */
    val timeRemaining: Long?

    /**
     * Current difficulty level
     */
    val difficulty: DifficultyLevel
}

/**
 * Default implementation of GameState
 */
data class DefaultGameState(
    override val phase: GamePhase = GamePhase.READY,
    override val score: Int = 0,
    override val timeRemaining: Long? = null,
    override val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER
) : GameState
