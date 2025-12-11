package com.mindforge.game.memorymatrix.model

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GameEvent
import com.mindforge.game.core.engine.GameResult
import com.mindforge.game.core.engine.GameState
import com.mindforge.game.core.engine.GamePhase

/**
 * State of the Memory Matrix game
 */
data class MemoryMatrixState(
    override val phase: GamePhase = GamePhase.READY,
    override val score: Int = 0,
    override val timeRemaining: Long? = null,
    override val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    // Memory Matrix specific state
    val gridSize: Int = 3,
    val targetCells: Set<CellPosition> = emptySet(),
    val selectedCells: Set<CellPosition> = emptySet(),
    val revealedCells: Set<CellPosition> = emptySet(),
    val displayTimeMs: Long = 3000,
    val currentRound: Int = 0,
    val totalRounds: Int = 5,
    val correctSelections: Int = 0,
    val incorrectSelections: Int = 0,
    val roundStartTime: Long = 0,
    val isShowingPattern: Boolean = false,
    val isAcceptingInput: Boolean = false
) : GameState

/**
 * Events that can occur in the Memory Matrix game
 */
sealed class MemoryMatrixEvent : GameEvent {
    object StartGame : MemoryMatrixEvent()
    object StartRound : MemoryMatrixEvent()
    object PatternDisplayComplete : MemoryMatrixEvent()
    data class CellSelected(val position: CellPosition) : MemoryMatrixEvent()
    object SubmitSelections : MemoryMatrixEvent()
    object NextRound : MemoryMatrixEvent()
    object PauseGame : MemoryMatrixEvent()
    object ResumeGame : MemoryMatrixEvent()
    object FinishGame : MemoryMatrixEvent()
}

/**
 * Result of a Memory Matrix game session
 */
data class MemoryMatrixResult(
    override val finalScore: Int,
    override val xpEarned: Int,
    override val accuracy: Float,
    override val timeSpentMs: Long,
    override val difficulty: DifficultyLevel,

    // Memory Matrix specific results
    val totalRounds: Int,
    val correctSelections: Int,
    val incorrectSelections: Int,
    val averageResponseTime: Long,
    val perfectRounds: Int
) : GameResult

/**
 * Position of a cell in the grid
 */
data class CellPosition(
    val row: Int,
    val col: Int
) {
    override fun toString(): String = "($row, $col)"
}

/**
 * Parameters for generating a Memory Matrix level
 */
data class MemoryMatrixParameters(
    val gridSize: Int,
    val targetCount: Int,
    val displayTimeMs: Long,
    val roundsCount: Int = 5
) {
    companion object {
        /**
         * Get parameters for a specific difficulty level
         */
        fun forDifficulty(level: DifficultyLevel): MemoryMatrixParameters {
            return when (level) {
                DifficultyLevel.BEGINNER -> MemoryMatrixParameters(
                    gridSize = 3,
                    targetCount = 3,
                    displayTimeMs = 3000
                )
                DifficultyLevel.EASY -> MemoryMatrixParameters(
                    gridSize = 3,
                    targetCount = 4,
                    displayTimeMs = 3000
                )
                DifficultyLevel.MEDIUM -> MemoryMatrixParameters(
                    gridSize = 4,
                    targetCount = 5,
                    displayTimeMs = 2500
                )
                DifficultyLevel.HARD -> MemoryMatrixParameters(
                    gridSize = 5,
                    targetCount = 7,
                    displayTimeMs = 2000
                )
                DifficultyLevel.EXPERT -> MemoryMatrixParameters(
                    gridSize = 6,
                    targetCount = 9,
                    displayTimeMs = 1500
                )
                DifficultyLevel.MASTER -> MemoryMatrixParameters(
                    gridSize = 6,
                    targetCount = 10,
                    displayTimeMs = 1500
                )
                DifficultyLevel.GRANDMASTER -> MemoryMatrixParameters(
                    gridSize = 7,
                    targetCount = 12,
                    displayTimeMs = 1200
                )
                DifficultyLevel.LEGENDARY -> MemoryMatrixParameters(
                    gridSize = 7,
                    targetCount = 14,
                    displayTimeMs = 1200
                )
                DifficultyLevel.MYTHIC -> MemoryMatrixParameters(
                    gridSize = 8,
                    targetCount = 16,
                    displayTimeMs = 1000
                )
                DifficultyLevel.DIVINE -> MemoryMatrixParameters(
                    gridSize = 8,
                    targetCount = 18,
                    displayTimeMs = 1000
                )
            }
        }
    }
}
