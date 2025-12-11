package com.mindforge.game.nameface.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.GameType
import com.mindforge.game.core.scoring.ScoringSystem
import com.mindforge.game.nameface.engine.NameFaceEngine
import com.mindforge.game.nameface.model.NameFaceEvent
import com.mindforge.game.nameface.model.NameFaceResult
import com.mindforge.game.nameface.model.NameFaceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Name-Face Associator game
 */
@HiltViewModel
class NameFaceViewModel @Inject constructor(
    private val gameSessionRepository: com.mindforge.core.domain.repository.GameSessionRepository,
    private val userRepository: com.mindforge.core.domain.repository.UserRepository,
    private val initializeUserUseCase: com.mindforge.core.domain.usecase.InitializeUserUseCase
) : ViewModel() {

    private lateinit var engine: NameFaceEngine
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow<NameFaceUiState>(NameFaceUiState.Loading)
    val uiState: StateFlow<NameFaceUiState> = _uiState.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel) {
        engine = NameFaceEngine(
            initialDifficulty = difficulty,
            coroutineScope = viewModelScope,
            scoringSystem = ScoringSystem()
        )

        // Initialize user
        viewModelScope.launch {
            val user = initializeUserUseCase()
            currentUserId = user.id
        }

        // Observe engine state
        viewModelScope.launch {
            engine.currentState.collect { gameState ->
                _uiState.value = NameFaceUiState.Playing(gameState)
            }
        }

        // Start the game
        engine.start()
    }

    fun handleEvent(event: NameFaceEvent) {
        if (::engine.isInitialized) {
            engine.processEvent(event)
        }
    }

    fun onFinishGame() {
        if (!::engine.isInitialized) return
        val result = engine.finish()
        _uiState.value = NameFaceUiState.Finished(result)

        // Save the session and update user XP
        viewModelScope.launch {
            currentUserId?.let { userId ->
                // Save game session
                val endTime = System.currentTimeMillis()
                val session = com.mindforge.core.domain.model.GameSession(
                    id = java.util.UUID.randomUUID().toString(),
                    gameType = GameType.NAME_FACE,
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
 * UI state for the Name-Face Associator screen
 */
sealed class NameFaceUiState {
    object Loading : NameFaceUiState()
    data class Playing(val gameState: NameFaceState) : NameFaceUiState()
    data class Finished(val result: NameFaceResult) : NameFaceUiState()
}
