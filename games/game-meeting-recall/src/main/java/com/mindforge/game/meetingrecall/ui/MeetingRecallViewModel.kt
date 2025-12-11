package com.mindforge.game.meetingrecall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.GameType
import com.mindforge.game.core.scoring.ScoringSystem
import com.mindforge.game.meetingrecall.engine.MeetingRecallEngine
import com.mindforge.game.meetingrecall.model.MeetingRecallEvent
import com.mindforge.game.meetingrecall.model.MeetingRecallResult
import com.mindforge.game.meetingrecall.model.MeetingRecallState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Meeting Recall game
 */
@HiltViewModel
class MeetingRecallViewModel @Inject constructor(
    private val gameSessionRepository: com.mindforge.core.domain.repository.GameSessionRepository,
    private val userRepository: com.mindforge.core.domain.repository.UserRepository,
    private val initializeUserUseCase: com.mindforge.core.domain.usecase.InitializeUserUseCase
) : ViewModel() {

    private lateinit var engine: MeetingRecallEngine
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow<MeetingRecallUiState>(MeetingRecallUiState.Loading)
    val uiState: StateFlow<MeetingRecallUiState> = _uiState.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel) {
        engine = MeetingRecallEngine(
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
                _uiState.value = MeetingRecallUiState.Playing(gameState)
            }
        }

        // Start the game
        engine.start()
    }

    fun handleEvent(event: MeetingRecallEvent) {
        if (::engine.isInitialized) {
            engine.processEvent(event)
        }
    }

    fun onFinishGame() {
        if (!::engine.isInitialized) return
        val result = engine.finish()
        _uiState.value = MeetingRecallUiState.Finished(result)

        // Save the session and update user XP
        viewModelScope.launch {
            currentUserId?.let { userId ->
                // Save game session
                val endTime = System.currentTimeMillis()
                val session = com.mindforge.core.domain.model.GameSession(
                    id = java.util.UUID.randomUUID().toString(),
                    gameType = GameType.MEETING_RECALL,
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
 * UI state for the Meeting Recall screen
 */
sealed class MeetingRecallUiState {
    object Loading : MeetingRecallUiState()
    data class Playing(val gameState: MeetingRecallState) : MeetingRecallUiState()
    data class Finished(val result: MeetingRecallResult) : MeetingRecallUiState()
}
