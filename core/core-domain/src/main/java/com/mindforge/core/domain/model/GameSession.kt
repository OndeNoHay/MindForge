package com.mindforge.core.domain.model

/**
 * Domain model for a game session
 */
data class GameSession(
    val id: String,
    val gameType: GameType,
    val startTime: Long,
    val endTime: Long,
    val score: Int,
    val accuracy: Float,
    val difficultyLevel: DifficultyLevel,
    val xpEarned: Int
)

/**
 * Types of games available in the app
 */
enum class GameType {
    MEMORY_MATRIX,
    TASK_PRIORITIZER,
    NAME_FACE,
    MEETING_RECALL,
    CONCEPT_LINKER,
    SPACED_REVIEW
}
