package com.mindforge.game.conceptlinker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.GameType
import com.mindforge.game.core.engine.ScoringSystem
import com.mindforge.game.conceptlinker.engine.ConceptLinkerEngine
import com.mindforge.game.conceptlinker.model.ConceptLinkerEvent
import com.mindforge.game.conceptlinker.model.ConceptLinkerResult
import com.mindforge.game.conceptlinker.model.ConceptLinkerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Concept Linker game
 */
@HiltViewModel
class ConceptLinkerViewModel @Inject constructor(
    private val gameSessionRepository: com.mindforge.core.domain.repository.GameSessionRepository,
    private val userRepository: com.mindforge.core.domain.repository.UserRepository,
    private val initializeUserUseCase: com.mindforge.core.domain.usecase.InitializeUserUseCase
) : ViewModel() {

    private var engine: ConceptLinkerEngine? = null

    private val _uiState = MutableStateFlow<ConceptLinkerState?>(null)
    val uiState: StateFlow<ConceptLinkerState?> = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<ConceptLinkerResult?>(null)
    val gameResult: StateFlow<ConceptLinkerResult?> = _gameResult.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            // Ensure user exists
            initializeUserUseCase()

            // Create engine
            engine = ConceptLinkerEngine(
                initialDifficulty = difficulty,
                coroutineScope = viewModelScope,
                scoringSystem = ScoringSystem()
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

    fun handleEvent(event: ConceptLinkerEvent) {
        viewModelScope.launch {
            engine?.handleEvent(event)
        }
    }

    private suspend fun saveGameSession(result: ConceptLinkerResult) {
        try {
            val session = com.mindforge.core.domain.model.GameSession(
                id = 0, // Auto-generated
                gameType = GameType.CONCEPT_LINKER,
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine?.reset()
    }
}
