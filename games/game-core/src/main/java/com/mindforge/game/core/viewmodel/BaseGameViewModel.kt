package com.mindforge.game.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.game.core.engine.GameEngine
import com.mindforge.game.core.engine.GameEvent
import com.mindforge.game.core.engine.GameResult
import com.mindforge.game.core.engine.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for all games
 * Handles common game logic and state management
 *
 * @param State The type of game state
 * @param Event The type of game events
 * @param Result The type of game result
 */
abstract class BaseGameViewModel<State : GameState, Event : GameEvent, Result : GameResult> :
    ViewModel() {

    /**
     * Game engine instance
     */
    protected abstract val gameEngine: GameEngine<State, Event, Result>

    /**
     * UI state flow
     */
    private val _uiState = MutableStateFlow<GameUiState<State, Result>>(GameUiState.Loading())
    val uiState: StateFlow<GameUiState<State, Result>> = _uiState.asStateFlow()

    init {
        observeGameState()
    }

    /**
     * Observe game state changes and update UI state
     */
    private fun observeGameState() {
        viewModelScope.launch {
            gameEngine.currentState.collect { state ->
                _uiState.value = GameUiState.Playing(state)
            }
        }
    }

    /**
     * Start the game
     */
    fun startGame() {
        viewModelScope.launch {
            gameEngine.start()
        }
    }

    /**
     * Pause the game
     */
    fun pauseGame() {
        viewModelScope.launch {
            gameEngine.pause()
        }
    }

    /**
     * Resume the game
     */
    fun resumeGame() {
        viewModelScope.launch {
            gameEngine.resume()
        }
    }

    /**
     * Finish the game
     */
    fun finishGame() {
        viewModelScope.launch {
            val result = gameEngine.finish()
            _uiState.value = GameUiState.Finished(result)
        }
    }

    /**
     * Restart the game
     */
    fun restartGame() {
        viewModelScope.launch {
            gameEngine.restart()
        }
    }

    /**
     * Process a game event
     */
    fun processEvent(event: Event) {
        viewModelScope.launch {
            gameEngine.processEvent(event)
        }
    }

    /**
     * Handle errors
     */
    protected fun handleError(error: Throwable) {
        _uiState.value = GameUiState.Error(error.message ?: "Unknown error")
    }
}

/**
 * Sealed class representing the UI state of a game
 */
sealed class GameUiState<out State : GameState, out Result : GameResult> {
    /**
     * Loading state
     */
    data class Loading<State : GameState, Result : GameResult>(
        val message: String = "Loading game..."
    ) : GameUiState<State, Result>()

    /**
     * Playing state
     */
    data class Playing<State : GameState, Result : GameResult>(
        val gameState: State
    ) : GameUiState<State, Result>()

    /**
     * Finished state
     */
    data class Finished<State : GameState, Result : GameResult>(
        val result: Result
    ) : GameUiState<State, Result>()

    /**
     * Error state
     */
    data class Error<State : GameState, Result : GameResult>(
        val message: String
    ) : GameUiState<State, Result>()
}
