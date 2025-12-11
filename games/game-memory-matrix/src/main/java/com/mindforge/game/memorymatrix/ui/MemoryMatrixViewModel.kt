package com.mindforge.game.memorymatrix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.memorymatrix.engine.MemoryMatrixEngine
import com.mindforge.game.memorymatrix.model.CellPosition
import com.mindforge.game.memorymatrix.model.MemoryMatrixEvent
import com.mindforge.game.memorymatrix.model.MemoryMatrixResult
import com.mindforge.game.memorymatrix.model.MemoryMatrixState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Memory Matrix game
 */
@HiltViewModel
class MemoryMatrixViewModel @Inject constructor(
    private val gameSessionRepository: com.mindforge.core.domain.repository.GameSessionRepository,
    private val userRepository: com.mindforge.core.domain.repository.UserRepository,
    private val initializeUserUseCase: com.mindforge.core.domain.usecase.InitializeUserUseCase
) : ViewModel() {

    private lateinit var engine: MemoryMatrixEngine
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow<MemoryMatrixUiState>(MemoryMatrixUiState.Loading)
    val uiState: StateFlow<MemoryMatrixUiState> = _uiState.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel = DifficultyLevel.BEGINNER) {
        engine = MemoryMatrixEngine(
            initialDifficulty = difficulty,
            coroutineScope = viewModelScope
        )

        // Initialize user
        viewModelScope.launch {
            val user = initializeUserUseCase()
            currentUserId = user.id
        }

        // Observe engine state
        viewModelScope.launch {
            engine.currentState.collect { gameState ->
                _uiState.value = MemoryMatrixUiState.Playing(gameState)
            }
        }

        // Start the game
        engine.start()
    }

    fun onCellTapped(position: CellPosition) {
        engine.processEvent(MemoryMatrixEvent.CellSelected(position))
    }

    fun onSubmitSelections() {
        engine.processEvent(MemoryMatrixEvent.SubmitSelections)
    }

    fun onNextRound() {
        engine.processEvent(MemoryMatrixEvent.NextRound)
    }

    fun onPause() {
        engine.pause()
    }

    fun onResume() {
        engine.resume()
    }

    fun onFinishGame() {
        val result = engine.finish()
        _uiState.value = MemoryMatrixUiState.Finished(result)

        // Save the session and update user XP
        viewModelScope.launch {
            currentUserId?.let { userId ->
                // Save game session
                val endTime = System.currentTimeMillis()
                val session = com.mindforge.core.domain.model.GameSession(
                    id = java.util.UUID.randomUUID().toString(),
                    gameType = com.mindforge.core.domain.model.GameType.MEMORY_MATRIX,
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
 * UI state for the Memory Matrix screen
 */
sealed class MemoryMatrixUiState {
    object Loading : MemoryMatrixUiState()
    data class Playing(val gameState: MemoryMatrixState) : MemoryMatrixUiState()
    data class Finished(val result: MemoryMatrixResult) : MemoryMatrixUiState()
}
