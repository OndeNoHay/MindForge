package com.mindforge.game.spacedreview.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.GameType
import com.mindforge.game.core.scoring.ScoringSystem
import com.mindforge.game.spacedreview.engine.SpacedReviewEngine
import com.mindforge.game.spacedreview.model.SpacedReviewEvent
import com.mindforge.game.spacedreview.model.SpacedReviewResult
import com.mindforge.game.spacedreview.model.SpacedReviewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Spaced Review System
 */
@HiltViewModel
class SpacedReviewViewModel @Inject constructor(
    private val gameSessionRepository: com.mindforge.core.domain.repository.GameSessionRepository,
    private val userRepository: com.mindforge.core.domain.repository.UserRepository,
    private val initializeUserUseCase: com.mindforge.core.domain.usecase.InitializeUserUseCase
) : ViewModel() {

    private lateinit var engine: SpacedReviewEngine
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow<SpacedReviewUiState>(SpacedReviewUiState.Loading)
    val uiState: StateFlow<SpacedReviewUiState> = _uiState.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel) {
        engine = SpacedReviewEngine(
            initialDifficulty = difficulty,
            coroutineScope = viewModelScope,
            scoringSystem = ScoringSystem(),
            existingItems = emptyList()  // TODO: Load from database
        )

        // Initialize user
        viewModelScope.launch {
            val user = initializeUserUseCase()
            currentUserId = user.id
        }

        // Observe engine state
        viewModelScope.launch {
            engine.currentState.collect { gameState ->
                _uiState.value = SpacedReviewUiState.Playing(gameState)
            }
        }

        // Start the game
        engine.start()
    }

    fun handleEvent(event: SpacedReviewEvent) {
        if (::engine.isInitialized) {
            engine.processEvent(event)
        }
    }

    fun onFinishGame() {
        if (!::engine.isInitialized) return
        val result = engine.finish()
        _uiState.value = SpacedReviewUiState.Finished(result)

        // Save the session and update user XP
        viewModelScope.launch {
            currentUserId?.let { userId ->
                // Save game session
                val endTime = System.currentTimeMillis()
                val session = com.mindforge.core.domain.model.GameSession(
                    id = java.util.UUID.randomUUID().toString(),
                    gameType = GameType.SPACED_REVIEW,
                    startTime = endTime - result.timeTaken,
                    endTime = endTime,
                    score = result.score,
                    accuracy = result.accuracy,
                    difficultyLevel = result.difficulty,
                    xpEarned = result.xpEarned
                )
                gameSessionRepository.saveSession(session)

                // Update user XP
                userRepository.updateXP(userId, result.xpEarned)

                // TODO: Save updated review items to database
                // engine.getUpdatedItems()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::engine.isInitialized) {
            engine.finish()
        }
    }
}

/**
 * UI state for the Spaced Review screen
 */
sealed class SpacedReviewUiState {
    object Loading : SpacedReviewUiState()
    data class Playing(val gameState: SpacedReviewState) : SpacedReviewUiState()
    data class Finished(val result: SpacedReviewResult) : SpacedReviewUiState()
}
