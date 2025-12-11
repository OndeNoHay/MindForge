package com.mindforge.game.spacedreview.model

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GameEvent
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.engine.GameResult
import com.mindforge.game.core.engine.GameState
import java.time.LocalDate

/**
 * A review item using SM-2 algorithm
 */
data class ReviewItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: ReviewCategory,
    val easinessFactor: Float = 2.5f,  // Initial: 2.5
    val interval: Int = 0,  // Days until next review
    val repetitions: Int = 0,
    val nextReviewDate: LocalDate = LocalDate.now(),
    val lastReviewDate: LocalDate? = null,
    val createdDate: LocalDate = LocalDate.now()
)

/**
 * Category of review item
 */
enum class ReviewCategory(val displayName: String, val color: Long) {
    MEMORY("Memoria", 0xFF2196F3),
    TASKS("Tareas", 0xFF4CAF50),
    PEOPLE("Personas", 0xFFFF9800),
    MEETINGS("Reuniones", 0xFF9C27B0),
    CONCEPTS("Conceptos", 0xFF00BCD4),
    GENERAL("General", 0xFF9E9E9E)
}

/**
 * Quality of recall (0-5 for SM-2 algorithm)
 */
enum class RecallQuality(val value: Int, val displayName: String) {
    COMPLETE_BLACKOUT(0, "No recordé nada"),
    INCORRECT_HARD(1, "Incorrecto pero reconocible"),
    INCORRECT_EASY(2, "Incorrecto pero casi correcto"),
    CORRECT_HARD(3, "Correcto con esfuerzo"),
    CORRECT_HESITANT(4, "Correcto con ligera duda"),
    PERFECT(5, "Respuesta perfecta")
}

/**
 * Phase of spaced review session
 */
enum class SpacedReviewPhase {
    REVIEW,     // Reviewing items
    RESULT      // Session results
}

/**
 * State of the Spaced Review game
 */
data class SpacedReviewState(
    override val phase: GamePhase = GamePhase.READY,
    override val score: Int = 0,
    override val timeRemaining: Long? = null,
    override val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    // Spaced Review specific state
    val spacedReviewPhase: SpacedReviewPhase = SpacedReviewPhase.REVIEW,
    val reviewItems: List<ReviewItem> = emptyList(),
    val currentItemIndex: Int = 0,
    val reviewedItems: Map<String, RecallQuality> = emptyMap(),  // itemId -> quality
    val showAnswer: Boolean = false,

    // Stats
    val perfectRecalls: Int = 0,
    val goodRecalls: Int = 0,
    val okayRecalls: Int = 0,
    val poorRecalls: Int = 0,

    // Timing
    val sessionStartTime: Long = 0
) : GameState {
    val currentItem: ReviewItem?
        get() = reviewItems.getOrNull(currentItemIndex)

    val isSessionComplete: Boolean
        get() = reviewedItems.size == reviewItems.size

    val accuracy: Float
        get() {
            val total = reviewedItems.size
            if (total == 0) return 0f
            val goodAnswers = perfectRecalls + goodRecalls
            return goodAnswers.toFloat() / total.toFloat()
        }

    val totalItemsReviewed: Int
        get() = reviewedItems.size
}

/**
 * Events in the Spaced Review game
 */
sealed class SpacedReviewEvent : GameEvent {
    object StartSession : SpacedReviewEvent()
    object ShowAnswer : SpacedReviewEvent()
    data class RateRecall(val itemId: String, val quality: RecallQuality) : SpacedReviewEvent()
    object NextItem : SpacedReviewEvent()
    object FinishSession : SpacedReviewEvent()
    object PauseGame : SpacedReviewEvent()
    object ResumeGame : SpacedReviewEvent()
}

/**
 * Result of a Spaced Review session
 */
data class SpacedReviewResult(
    override val score: Int,
    override val accuracy: Float,
    override val timeTaken: Long,
    override val difficulty: DifficultyLevel,
    override val xpEarned: Int,
    override val passed: Boolean,

    // Spaced Review specific results
    val itemsReviewed: Int,
    val perfectRecalls: Int,
    val goodRecalls: Int,
    val okayRecalls: Int,
    val poorRecalls: Int,
    val itemsDueToday: Int,
    val itemsDueSoon: Int
) : GameResult

/**
 * SM-2 Algorithm Implementation
 */
object SM2Algorithm {

    /**
     * Calculate next review parameters based on recall quality
     *
     * SM-2 Algorithm:
     * - EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
     * - If q >= 3: interval increases
     * - If q < 3: reset to beginning
     */
    fun calculateNextReview(item: ReviewItem, quality: RecallQuality): ReviewItem {
        val q = quality.value
        val oldEF = item.easinessFactor

        // Calculate new easiness factor
        val newEF = (oldEF + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)))
            .coerceAtLeast(1.3f)  // Minimum EF is 1.3

        val newRepetitions: Int
        val newInterval: Int

        if (q < 3) {
            // Failed recall - reset
            newRepetitions = 0
            newInterval = 1
        } else {
            // Successful recall
            newRepetitions = item.repetitions + 1
            newInterval = when (newRepetitions) {
                1 -> 1
                2 -> 6
                else -> (item.interval * newEF).toInt()
            }
        }

        val today = LocalDate.now()

        return item.copy(
            easinessFactor = newEF,
            repetitions = newRepetitions,
            interval = newInterval,
            lastReviewDate = today,
            nextReviewDate = today.plusDays(newInterval.toLong())
        )
    }

    /**
     * Check if an item is due for review
     */
    fun isDue(item: ReviewItem, date: LocalDate = LocalDate.now()): Boolean {
        return !item.nextReviewDate.isAfter(date)
    }

    /**
     * Get items due for review
     */
    fun getDueItems(items: List<ReviewItem>, date: LocalDate = LocalDate.now()): List<ReviewItem> {
        return items.filter { isDue(it, date) }
            .sortedBy { it.nextReviewDate }  // Oldest due first
    }
}

/**
 * Template for generating review items
 */
data class ReviewItemTemplate(
    val question: String,
    val answer: String,
    val category: ReviewCategory
)
