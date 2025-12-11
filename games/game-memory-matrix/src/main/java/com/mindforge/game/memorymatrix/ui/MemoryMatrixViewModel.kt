package com.mindforge.game.memorymatrix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforge.game.core.difficulty.DifficultyLevel
import com.mindforge.game.memorymatrix.engine.MemoryMatrixEngine
import com.mindforge.game.memorymatrix.model.CellPosition
import com.mindforge.game.memorymatrix.model.MemoryMatrixEvent
import com.mindforge.game.memorymatrix.model.MemoryMatrixResult
import com.mindforge.game.memorymatrix.model.MemoryMatrixState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the Memory Matrix game
 */
@HiltViewModel
class MemoryMatrixViewModel @Inject constructor(
    // Dependencies will be injected later when we set up the DI module
) : ViewModel() {

    private lateinit var engine: MemoryMatrixEngine

    private val _uiState = MutableStateFlow<MemoryMatrixUiState>(MemoryMatrixUiState.Loading)
    val uiState: StateFlow<MemoryMatrixUiState> = _uiState.asStateFlow()

    fun initializeGame(difficulty: DifficultyLevel = DifficultyLevel.EASY) {
        engine = MemoryMatrixEngine(
            initialDifficulty = difficulty,
            coroutineScope = viewModelScope
        )

        // Observe engine state
        kotlinx.coroutines.launch(viewModelScope.coroutineContext) {
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
