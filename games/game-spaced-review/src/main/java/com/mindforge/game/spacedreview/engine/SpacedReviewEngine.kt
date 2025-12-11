package com.mindforge.game.spacedreview.engine

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.BaseGameEngine
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.engine.ScoringSystem
import com.mindforge.game.spacedreview.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * Game engine for the Spaced Review System
 */
class SpacedReviewEngine(
    private val initialDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    private val coroutineScope: CoroutineScope,
    private val scoringSystem: ScoringSystem = ScoringSystem(),
    private val existingItems: List<ReviewItem> = emptyList()
) : BaseGameEngine<SpacedReviewState, SpacedReviewEvent, SpacedReviewResult>() {

    private val _state = MutableStateFlow(SpacedReviewState(difficulty = initialDifficulty))
    override val state: StateFlow<SpacedReviewState> = _state.asStateFlow()

    private val reviewItemGenerator = ReviewItemGenerator()
    private var gameStartTime: Long = 0
    private val updatedItems = mutableMapOf<String, ReviewItem>()  // Store updated items

    override suspend fun handleEvent(event: SpacedReviewEvent) {
        when (event) {
            is SpacedReviewEvent.StartSession -> startSession()
            is SpacedReviewEvent.ShowAnswer -> showAnswer()
            is SpacedReviewEvent.RateRecall -> rateRecall(event.itemId, event.quality)
            is SpacedReviewEvent.NextItem -> nextItem()
            is SpacedReviewEvent.FinishSession -> finishSession()
            is SpacedReviewEvent.PauseGame -> pauseGame()
            is SpacedReviewEvent.ResumeGame -> resumeGame()
        }
    }

    private fun startSession() {
        reviewItemGenerator.reset()
        gameStartTime = System.currentTimeMillis()
        updatedItems.clear()

        // Get items to review
        val itemsToReview = if (existingItems.isNotEmpty()) {
            // Use existing items and filter due ones
            SM2Algorithm.getDueItems(existingItems).take(20)
        } else {
            // Generate new items for demo
            reviewItemGenerator.generateVariedItems(15)
                .let { SM2Algorithm.getDueItems(it) }
        }

        if (itemsToReview.isEmpty()) {
            // No items due, generate some new ones
            val newItems = reviewItemGenerator.generateInitialItems(10)
            _state.value = SpacedReviewState(
                phase = GamePhase.PLAYING,
                difficulty = initialDifficulty,
                spacedReviewPhase = SpacedReviewPhase.REVIEW,
                reviewItems = newItems,
                currentItemIndex = 0,
                sessionStartTime = System.currentTimeMillis()
            )
        } else {
            _state.value = SpacedReviewState(
                phase = GamePhase.PLAYING,
                difficulty = initialDifficulty,
                spacedReviewPhase = SpacedReviewPhase.REVIEW,
                reviewItems = itemsToReview,
                currentItemIndex = 0,
                sessionStartTime = System.currentTimeMillis()
            )
        }
    }

    private fun showAnswer() {
        _state.value = _state.value.copy(showAnswer = true)
    }

    private fun rateRecall(itemId: String, quality: RecallQuality) {
        val currentItem = _state.value.currentItem ?: return

        if (currentItem.id != itemId) return

        // Update item using SM-2 algorithm
        val updatedItem = SM2Algorithm.calculateNextReview(currentItem, quality)
        updatedItems[itemId] = updatedItem

        // Update stats
        val newPerfect = if (quality == RecallQuality.PERFECT) _state.value.perfectRecalls + 1 else _state.value.perfectRecalls
        val newGood = if (quality == RecallQuality.CORRECT_HESITANT) _state.value.goodRecalls + 1 else _state.value.goodRecalls
        val newOkay = if (quality == RecallQuality.CORRECT_HARD) _state.value.okayRecalls + 1 else _state.value.okayRecalls
        val newPoor = if (quality.value < 3) _state.value.poorRecalls + 1 else _state.value.poorRecalls

        // Calculate score
        val pointsForQuality = when (quality) {
            RecallQuality.PERFECT -> 100
            RecallQuality.CORRECT_HESITANT -> 80
            RecallQuality.CORRECT_HARD -> 60
            RecallQuality.INCORRECT_EASY -> 30
            RecallQuality.INCORRECT_HARD -> 10
            RecallQuality.COMPLETE_BLACKOUT -> 0
        }

        val scoredPoints = scoringSystem.calculateScore(
            baseScore = pointsForQuality,
            difficulty = _state.value.difficulty,
            accuracy = 1.0f,
            timeBonus = 0
        )

        _state.value = _state.value.copy(
            reviewedItems = _state.value.reviewedItems + (itemId to quality),
            perfectRecalls = newPerfect,
            goodRecalls = newGood,
            okayRecalls = newOkay,
            poorRecalls = newPoor,
            score = _state.value.score + scoredPoints
        )
    }

    private fun nextItem() {
        if (_state.value.isSessionComplete) {
            finishSession()
            return
        }

        val newIndex = _state.value.currentItemIndex + 1

        _state.value = _state.value.copy(
            currentItemIndex = newIndex,
            showAnswer = false
        )

        if (newIndex >= _state.value.reviewItems.size) {
            finishSession()
        }
    }

    private fun finishSession() {
        val totalTime = System.currentTimeMillis() - gameStartTime
        val accuracy = _state.value.accuracy

        val xpEarned = scoringSystem.calculateXP(
            score = _state.value.score,
            difficulty = _state.value.difficulty,
            accuracy = accuracy
        )

        // Calculate items due soon
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val allItems = _state.value.reviewItems.map { item ->
            updatedItems[item.id] ?: item
        }

        val itemsDueToday = allItems.count { SM2Algorithm.isDue(it, today) }
        val itemsDueSoon = allItems.count {
            it.nextReviewDate.isAfter(today) && !it.nextReviewDate.isAfter(tomorrow.plusDays(2))
        }

        val result = SpacedReviewResult(
            score = _state.value.score,
            accuracy = accuracy,
            timeTaken = totalTime,
            difficulty = _state.value.difficulty,
            xpEarned = xpEarned,
            passed = accuracy >= 0.6f,  // 60% good recalls to pass
            itemsReviewed = _state.value.totalItemsReviewed,
            perfectRecalls = _state.value.perfectRecalls,
            goodRecalls = _state.value.goodRecalls,
            okayRecalls = _state.value.okayRecalls,
            poorRecalls = _state.value.poorRecalls,
            itemsDueToday = itemsDueToday,
            itemsDueSoon = itemsDueSoon
        )

        _state.value = _state.value.copy(
            phase = GamePhase.FINISHED,
            spacedReviewPhase = SpacedReviewPhase.RESULT
        )

        emitResult(result)
    }

    private fun pauseGame() {
        _state.value = _state.value.copy(phase = GamePhase.PAUSED)
    }

    private fun resumeGame() {
        _state.value = _state.value.copy(phase = GamePhase.PLAYING)
    }

    /**
     * Get all updated items (with new review dates)
     */
    fun getUpdatedItems(): List<ReviewItem> {
        return _state.value.reviewItems.map { item ->
            updatedItems[item.id] ?: item
        }
    }

    override fun reset() {
        reviewItemGenerator.reset()
        updatedItems.clear()
        _state.value = SpacedReviewState(difficulty = initialDifficulty)
    }
}
