package com.mindforge.game.meetingrecall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.GameType
import com.mindforge.game.core.engine.ScoringSystem
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

    private var engine: MeetingRecallEngine? = null

    private val _uiState = MutableStateFlow<MeetingRecallState?>(null)
    val uiState: StateFlow<MeetingRecallState?> = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<MeetingRecallResult?>(null)
    val gameResult: StateFlow<MeetingRecallResult?> = _gameResult.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            // Ensure user exists
            initializeUserUseCase()

            // Create engine
            engine = MeetingRecallEngine(
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

    fun handleEvent(event: MeetingRecallEvent) {
        viewModelScope.launch {
            engine?.handleEvent(event)
        }
    }

    private suspend fun saveGameSession(result: MeetingRecallResult) {
        try {
            val session = com.mindforge.core.domain.model.GameSession(
                id = 0, // Auto-generated
                gameType = GameType.MEETING_RECALL,
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
