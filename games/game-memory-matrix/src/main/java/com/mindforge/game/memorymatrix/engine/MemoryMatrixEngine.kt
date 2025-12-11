package com.mindforge.game.memorymatrix.engine

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.BaseGameEngine
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.scoring.ScoringSystem
import com.mindforge.game.memorymatrix.model.CellPosition
import com.mindforge.game.memorymatrix.model.MemoryMatrixEvent
import com.mindforge.game.memorymatrix.model.MemoryMatrixParameters
import com.mindforge.game.memorymatrix.model.MemoryMatrixResult
import com.mindforge.game.memorymatrix.model.MemoryMatrixState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Game engine for Memory Matrix
 * Handles game logic, pattern generation, and scoring
 */
class MemoryMatrixEngine(
    private val initialDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    private val coroutineScope: CoroutineScope,
    private val scoringSystem: ScoringSystem = ScoringSystem()
) : BaseGameEngine<MemoryMatrixState, MemoryMatrixEvent, MemoryMatrixResult>() {

    private val _state = MutableStateFlow(
        MemoryMatrixState(
            difficulty = initialDifficulty
        ).apply {
            val params = MemoryMatrixParameters.forDifficulty(initialDifficulty)
            copy(
                gridSize = params.gridSize,
                displayTimeMs = params.displayTimeMs,
                totalRounds = params.roundsCount
            )
        }
    )
    override val currentState: StateFlow<MemoryMatrixState> = _state.asStateFlow()

    private var gameStartTime: Long = 0
    private val roundResponseTimes = mutableListOf<Long>()

    override fun onProcessEvent(event: MemoryMatrixEvent) {
        when (event) {
            is MemoryMatrixEvent.StartGame -> handleStartGame()
            is MemoryMatrixEvent.StartRound -> handleStartRound()
            is MemoryMatrixEvent.PatternDisplayComplete -> handlePatternDisplayComplete()
            is MemoryMatrixEvent.CellSelected -> handleCellSelected(event.position)
            is MemoryMatrixEvent.SubmitSelections -> handleSubmitSelections()
            is MemoryMatrixEvent.NextRound -> handleNextRound()
            is MemoryMatrixEvent.PauseGame -> handlePauseInternal()
            is MemoryMatrixEvent.ResumeGame -> handleResumeInternal()
            is MemoryMatrixEvent.FinishGame -> handleFinishInternal()
        }
    }

    override fun onStart() {
        handleStartGame()
    }

    override fun onPause() {
        handlePauseInternal()
    }

    override fun onResume() {
        handleResumeInternal()
    }

    override fun onFinish(): MemoryMatrixResult {
        val state = _state.value
        val totalTime = System.currentTimeMillis() - gameStartTime
        val totalAttempts = state.correctSelections + state.incorrectSelections
        val accuracy = if (totalAttempts > 0) {
            state.correctSelections.toFloat() / totalAttempts.toFloat()
        } else 0f

        val xp = scoringSystem.calculateSessionXP(
            totalScore = state.score,
            accuracy = accuracy,
            difficulty = state.difficulty,
            timeTaken = totalTime
        )

        val avgResponseTime = if (roundResponseTimes.isNotEmpty()) {
            roundResponseTimes.average().toLong()
        } else 0L

        return MemoryMatrixResult(
            finalScore = state.score,
            xpEarned = xp,
            accuracy = accuracy,
            timeSpentMs = totalTime,
            difficulty = state.difficulty,
            totalRounds = state.totalRounds,
            correctSelections = state.correctSelections,
            incorrectSelections = state.incorrectSelections,
            averageResponseTime = avgResponseTime,
            perfectRounds = calculatePerfectRounds()
        )
    }

    override fun onRestart() {
        gameStartTime = 0
        roundResponseTimes.clear()
        val params = MemoryMatrixParameters.forDifficulty(initialDifficulty)
        _state.value = MemoryMatrixState(
            difficulty = initialDifficulty,
            gridSize = params.gridSize,
            displayTimeMs = params.displayTimeMs,
            totalRounds = params.roundsCount
        )
    }

    private fun handleStartGame() {
        gameStartTime = System.currentTimeMillis()
        roundResponseTimes.clear()

        val params = MemoryMatrixParameters.forDifficulty(initialDifficulty)
        _state.value = _state.value.copy(
            phase = GamePhase.PLAYING,
            gridSize = params.gridSize,
            displayTimeMs = params.displayTimeMs,
            totalRounds = params.roundsCount,
            currentRound = 0,
            score = 0,
            correctSelections = 0,
            incorrectSelections = 0
        )

        // Start first round
        processEvent(MemoryMatrixEvent.StartRound)
    }

    private fun handleStartRound() {
        val state = _state.value
        if (state.currentRound >= state.totalRounds) {
            processEvent(MemoryMatrixEvent.FinishGame)
            return
        }

        // Generate new pattern
        val params = MemoryMatrixParameters.forDifficulty(state.difficulty)
        val targetCells = generateRandomPattern(params.gridSize, params.targetCount)

        _state.value = state.copy(
            currentRound = state.currentRound + 1,
            targetCells = targetCells,
            selectedCells = emptySet(),
            revealedCells = emptySet(),
            isShowingPattern = true,
            isAcceptingInput = false,
            roundStartTime = System.currentTimeMillis()
        )

        // Auto-hide pattern after display time
        coroutineScope.launch {
            delay(state.displayTimeMs)
            processEvent(MemoryMatrixEvent.PatternDisplayComplete)
        }
    }

    private fun handlePatternDisplayComplete() {
        _state.value = _state.value.copy(
            isShowingPattern = false,
            isAcceptingInput = true
        )
    }

    private fun handleCellSelected(position: CellPosition) {
        val state = _state.value
        if (!state.isAcceptingInput || state.phase != GamePhase.PLAYING) return

        val newSelectedCells = if (position in state.selectedCells) {
            // Deselect cell
            state.selectedCells - position
        } else {
            // Select cell
            state.selectedCells + position
        }

        _state.value = state.copy(selectedCells = newSelectedCells)
    }

    private fun handleSubmitSelections() {
        val state = _state.value
        if (!state.isAcceptingInput) return

        // Calculate response time for this round
        val responseTime = System.currentTimeMillis() - state.roundStartTime
        roundResponseTimes.add(responseTime)

        // Calculate correct and incorrect selections
        val correctInThisRound = state.selectedCells.intersect(state.targetCells).size
        val incorrectInThisRound = state.selectedCells.subtract(state.targetCells).size
        val missedCells = state.targetCells.subtract(state.selectedCells).size

        // Calculate score for this round
        val roundScore = scoringSystem.calculateAnswerScore(
            isCorrect = missedCells == 0 && incorrectInThisRound == 0,
            responseTime = responseTime,
            difficulty = state.difficulty,
            currentStreak = if (missedCells == 0 && incorrectInThisRound == 0) 1 else 0
        )

        _state.value = state.copy(
            score = state.score + roundScore,
            correctSelections = state.correctSelections + correctInThisRound,
            incorrectSelections = state.incorrectSelections + incorrectInThisRound,
            revealedCells = state.targetCells,
            isAcceptingInput = false,
            phase = GamePhase.REVIEW
        )
    }

    private fun handleNextRound() {
        _state.value = _state.value.copy(phase = GamePhase.PLAYING)
        processEvent(MemoryMatrixEvent.StartRound)
    }

    private fun handlePauseInternal() {
        _state.value = _state.value.copy(phase = GamePhase.PAUSED)
    }

    private fun handleResumeInternal() {
        _state.value = _state.value.copy(phase = GamePhase.PLAYING)
    }

    private fun handleFinishInternal() {
        _state.value = _state.value.copy(phase = GamePhase.COMPLETED)
    }

    private fun generateRandomPattern(gridSize: Int, targetCount: Int): Set<CellPosition> {
        val allPositions = mutableListOf<CellPosition>()
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                allPositions.add(CellPosition(row, col))
            }
        }

        // Shuffle and take first N positions
        return allPositions.shuffled(Random).take(targetCount).toSet()
    }

    private fun calculatePerfectRounds(): Int {
        // This would need to track perfect rounds during gameplay
        // For now, estimate based on score
        val state = _state.value
        val maxPossibleScore = ScoringSystem.BASE_SCORE_CORRECT * state.totalRounds
        return if (maxPossibleScore > 0) {
            ((state.score.toFloat() / maxPossibleScore.toFloat()) * state.totalRounds.toFloat()).toInt()
        } else 0
    }
}
