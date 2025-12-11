package com.mindforge.game.spacedreview.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.GameType
import com.mindforge.game.core.engine.ScoringSystem
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

    private var engine: SpacedReviewEngine? = null

    private val _uiState = MutableStateFlow<SpacedReviewState?>(null)
    val uiState: StateFlow<SpacedReviewState?> = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<SpacedReviewResult?>(null)
    val gameResult: StateFlow<SpacedReviewResult?> = _gameResult.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            // Ensure user exists
            initializeUserUseCase()

            // Create engine
            engine = SpacedReviewEngine(
                initialDifficulty = difficulty,
                coroutineScope = viewModelScope,
                scoringSystem = ScoringSystem(),
                existingItems = emptyList()  // TODO: Load from database
            )

            // Observe state
            engine?.state?.collect { state ->
                _uiState.value = state
            }
        }

        // Observe results
        viewModelScope.launch {
            engine?.results?.collect { result ->
                _gameResult.value = result
                saveGameSession(result)
            }
        }
    }

    fun handleEvent(event: SpacedReviewEvent) {
        viewModelScope.launch {
            engine?.handleEvent(event)
        }
    }

    private suspend fun saveGameSession(result: SpacedReviewResult) {
        try {
            val session = com.mindforge.core.domain.model.GameSession(
                id = 0, // Auto-generated
                gameType = GameType.SPACED_REVIEW,
                difficulty = result.difficulty,
                score = result.score,
                accuracy = result.accuracy,
                xpEarned = result.xpEarned,
                timeTaken = result.timeTaken,
                completed = true,
                timestamp = System.currentTimeMillis()
            )

            gameSessionRepository.insertSession(session)

            // Update user XP
            val currentUser = userRepository.getCurrentUser()
            currentUser?.let { user ->
                userRepository.updateUser(
                    user.copy(
                        totalXP = user.totalXP + result.xpEarned,
                        gamesPlayed = user.gamesPlayed + 1
                    )
                )
            }

            // TODO: Save updated review items to database
            // engine?.getUpdatedItems()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine?.reset()
    }
}
