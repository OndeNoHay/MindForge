package com.mindforge.game.taskprioritizer.model

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GameEvent
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.engine.GameResult
import com.mindforge.game.core.engine.GameState

/**
 * Eisenhower Matrix Quadrants for task prioritization
 */
enum class EisenhowerQuadrant(
    val displayName: String,
    val description: String,
    val color: Long
) {
    DO_FIRST(
        displayName = "Hacer Primero",
        description = "Urgente e Importante",
        color = 0xFFE53935  // Red
    ),
    SCHEDULE(
        displayName = "Programar",
        description = "No Urgente pero Importante",
        color = 0xFF43A047  // Green
    ),
    DELEGATE(
        displayName = "Delegar",
        description = "Urgente pero No Importante",
        color = 0xFFFFB300  // Amber
    ),
    ELIMINATE(
        displayName = "Eliminar",
        description = "Ni Urgente ni Importante",
        color = 0xFF9E9E9E  // Grey
    );

    companion object {
        /**
         * Determine the correct quadrant based on urgency and importance levels
         */
        fun fromLevels(urgency: Int, importance: Int): EisenhowerQuadrant {
            val isUrgent = urgency >= 3
            val isImportant = importance >= 3

            return when {
                isUrgent && isImportant -> DO_FIRST
                !isUrgent && isImportant -> SCHEDULE
                isUrgent && !isImportant -> DELEGATE
                else -> ELIMINATE
            }
        }
    }
}

/**
 * Represents a task card in the game
 */
data class TaskCard(
    val id: String,
    val title: String,
    val description: String,
    val urgencyLevel: Int,          // 1-5
    val importanceLevel: Int,        // 1-5
    val deadline: String? = null,
    val estimatedTime: String? = null,
    val category: TaskCategory = TaskCategory.GENERAL
) {
    val correctQuadrant: EisenhowerQuadrant
        get() = EisenhowerQuadrant.fromLevels(urgencyLevel, importanceLevel)
}

/**
 * Task categories for variety
 */
enum class TaskCategory {
    MEETING,
    REPORT,
    EMAIL,
    PLANNING,
    REVIEW,
    GENERAL
}

/**
 * State of the Task Prioritizer game
 */
data class TaskPrioritizerState(
    override val phase: GamePhase = GamePhase.READY,
    override val score: Int = 0,
    override val timeRemaining: Long? = null,
    override val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    // Task Prioritizer specific state
    val currentRound: Int = 0,
    val totalRounds: Int = 5,
    val availableTasks: List<TaskCard> = emptyList(),
    val placedTasks: Map<String, EisenhowerQuadrant> = emptyMap(),  // taskId -> quadrant
    val correctPlacements: Int = 0,
    val incorrectPlacements: Int = 0,
    val roundStartTime: Long = 0,
    val tasksPerRound: Int = 4,
    val timeLimit: Long? = null,  // null for untimed mode
    val showFeedback: Boolean = false,
    val feedbackTask: TaskCard? = null,
    val isCorrectPlacement: Boolean = false
) : GameState {
    val accuracy: Float
        get() {
            val total = correctPlacements + incorrectPlacements
            return if (total > 0) correctPlacements.toFloat() / total.toFloat() else 0f
        }

    val allTasksPlaced: Boolean
        get() = placedTasks.size == availableTasks.size
}

/**
 * Events that can occur in the Task Prioritizer game
 */
sealed class TaskPrioritizerEvent : GameEvent {
    object StartGame : TaskPrioritizerEvent()
    object StartRound : TaskPrioritizerEvent()
    data class PlaceTask(val taskId: String, val quadrant: EisenhowerQuadrant) : TaskPrioritizerEvent()
    object SubmitPlacements : TaskPrioritizerEvent()
    object NextRound : TaskPrioritizerEvent()
    object DismissFeedback : TaskPrioritizerEvent()
    object PauseGame : TaskPrioritizerEvent()
    object ResumeGame : TaskPrioritizerEvent()
    object FinishGame : TaskPrioritizerEvent()
}

/**
 * Result of a Task Prioritizer game session
 */
data class TaskPrioritizerResult(
    override val score: Int,
    override val accuracy: Float,
    override val timeTaken: Long,
    override val difficulty: DifficultyLevel,
    override val xpEarned: Int,
    override val passed: Boolean,

    // Task Prioritizer specific results
    val totalRounds: Int,
    val correctPlacements: Int,
    val incorrectPlacements: Int,
    val averageResponseTime: Long,
    val perfectRounds: Int,
    val fastestRound: Long
) : GameResult

/**
 * Parameters for generating tasks at different difficulty levels
 */
data class TaskPrioritizerParameters(
    val tasksPerRound: Int,
    val roundsCount: Int,
    val timeLimit: Long?,  // milliseconds, null for untimed
    val showHints: Boolean
) {
    companion object {
        fun forDifficulty(level: DifficultyLevel): TaskPrioritizerParameters {
            return when (level) {
                DifficultyLevel.BEGINNER -> TaskPrioritizerParameters(
                    tasksPerRound = 3,
                    roundsCount = 3,
                    timeLimit = null,  // Untimed
                    showHints = true
                )
                DifficultyLevel.EASY -> TaskPrioritizerParameters(
                    tasksPerRound = 4,
                    roundsCount = 4,
                    timeLimit = 90000,  // 90 seconds
                    showHints = true
                )
                DifficultyLevel.MEDIUM -> TaskPrioritizerParameters(
                    tasksPerRound = 5,
                    roundsCount = 5,
                    timeLimit = 75000,  // 75 seconds
                    showHints = false
                )
                DifficultyLevel.HARD -> TaskPrioritizerParameters(
                    tasksPerRound = 6,
                    roundsCount = 5,
                    timeLimit = 60000,  // 60 seconds
                    showHints = false
                )
                DifficultyLevel.EXPERT -> TaskPrioritizerParameters(
                    tasksPerRound = 7,
                    roundsCount = 6,
                    timeLimit = 50000,  // 50 seconds
                    showHints = false
                )
                DifficultyLevel.MASTER -> TaskPrioritizerParameters(
                    tasksPerRound = 8,
                    roundsCount = 6,
                    timeLimit = 45000,
                    showHints = false
                )
                DifficultyLevel.GRANDMASTER -> TaskPrioritizerParameters(
                    tasksPerRound = 9,
                    roundsCount = 7,
                    timeLimit = 40000,
                    showHints = false
                )
                DifficultyLevel.LEGENDARY -> TaskPrioritizerParameters(
                    tasksPerRound = 10,
                    roundsCount = 7,
                    timeLimit = 35000,
                    showHints = false
                )
                DifficultyLevel.MYTHIC -> TaskPrioritizerParameters(
                    tasksPerRound = 11,
                    roundsCount = 8,
                    timeLimit = 30000,
                    showHints = false
                )
                DifficultyLevel.DIVINE -> TaskPrioritizerParameters(
                    tasksPerRound = 12,
                    roundsCount = 8,
                    timeLimit = 25000,
                    showHints = false
                )
            }
        }
    }
}
