package com.mindforge.game.taskprioritizer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.taskprioritizer.engine.TaskPrioritizerEngine
import com.mindforge.game.taskprioritizer.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Task Prioritizer game
 */
@HiltViewModel
class TaskPrioritizerViewModel @Inject constructor(
    private val gameSessionRepository: com.mindforge.core.domain.repository.GameSessionRepository,
    private val userRepository: com.mindforge.core.domain.repository.UserRepository,
    private val initializeUserUseCase: com.mindforge.core.domain.usecase.InitializeUserUseCase
) : ViewModel() {

    private lateinit var engine: TaskPrioritizerEngine
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow<TaskPrioritizerUiState>(TaskPrioritizerUiState.Loading)
    val uiState: StateFlow<TaskPrioritizerUiState> = _uiState.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel = DifficultyLevel.BEGINNER) {
        engine = TaskPrioritizerEngine(
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
                _uiState.value = TaskPrioritizerUiState.Playing(gameState)
            }
        }

        // Start the game
        engine.start()
    }

    fun onPlaceTask(taskId: String, quadrant: EisenhowerQuadrant) {
        if (::engine.isInitialized) {
            engine.processEvent(TaskPrioritizerEvent.PlaceTask(taskId, quadrant))
        }
    }

    fun onDismissFeedback() {
        if (::engine.isInitialized) {
            engine.processEvent(TaskPrioritizerEvent.DismissFeedback)
        }
    }

    fun onSubmitPlacements() {
        if (::engine.isInitialized) {
            engine.processEvent(TaskPrioritizerEvent.SubmitPlacements)
        }
    }

    fun onNextRound() {
        if (::engine.isInitialized) {
            engine.processEvent(TaskPrioritizerEvent.NextRound)
        }
    }

    fun onPause() {
        if (::engine.isInitialized) {
            engine.pause()
        }
    }

    fun onResume() {
        if (::engine.isInitialized) {
            engine.resume()
        }
    }

    fun onFinishGame() {
        if (!::engine.isInitialized) return
        val result = engine.finish()
        _uiState.value = TaskPrioritizerUiState.Finished(result)

        // Save the session and update user XP
        viewModelScope.launch {
            currentUserId?.let { userId ->
                val endTime = System.currentTimeMillis()
                val session = com.mindforge.core.domain.model.GameSession(
                    id = java.util.UUID.randomUUID().toString(),
                    gameType = com.mindforge.core.domain.model.GameType.TASK_PRIORITIZER,
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
 * UI state for the Task Prioritizer screen
 */
sealed class TaskPrioritizerUiState {
    object Loading : TaskPrioritizerUiState()
    data class Playing(val gameState: TaskPrioritizerState) : TaskPrioritizerUiState()
    data class Finished(val result: TaskPrioritizerResult) : TaskPrioritizerUiState()
}
