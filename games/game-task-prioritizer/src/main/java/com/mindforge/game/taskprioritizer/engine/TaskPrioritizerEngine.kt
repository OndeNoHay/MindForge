package com.mindforge.game.taskprioritizer.engine

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.BaseGameEngine
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.scoring.ScoringSystem
import com.mindforge.game.taskprioritizer.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Game engine for Task Prioritizer
 * Handles task generation, placement validation, and scoring
 */
class TaskPrioritizerEngine(
    private val initialDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    private val coroutineScope: CoroutineScope,
    private val scoringSystem: ScoringSystem = ScoringSystem()
) : BaseGameEngine<TaskPrioritizerState, TaskPrioritizerEvent, TaskPrioritizerResult>() {

    private val taskGenerator = TaskGenerator()
    private var timerJob: Job? = null
    private var gameStartTime: Long = 0
    private val roundResponseTimes = mutableListOf<Long>()
    private val perfectRoundsList = mutableListOf<Boolean>()
    private var fastestRoundTime: Long = Long.MAX_VALUE

    private val _state = MutableStateFlow(
        TaskPrioritizerParameters.forDifficulty(initialDifficulty).let { params ->
            TaskPrioritizerState(
                phase = GamePhase.READY,
                difficulty = initialDifficulty,
                totalRounds = params.roundsCount,
                tasksPerRound = params.tasksPerRound,
                timeLimit = params.timeLimit
            )
        }
    )

    override val currentState: StateFlow<TaskPrioritizerState> = _state.asStateFlow()

    override fun onProcessEvent(event: TaskPrioritizerEvent) {
        when (event) {
            is TaskPrioritizerEvent.StartGame -> handleStartGame()
            is TaskPrioritizerEvent.StartRound -> handleStartRound()
            is TaskPrioritizerEvent.PlaceTask -> handlePlaceTask(event.taskId, event.quadrant)
            is TaskPrioritizerEvent.SubmitPlacements -> handleSubmitPlacements()
            is TaskPrioritizerEvent.NextRound -> handleNextRound()
            is TaskPrioritizerEvent.DismissFeedback -> handleDismissFeedback()
            is TaskPrioritizerEvent.PauseGame -> handlePause()
            is TaskPrioritizerEvent.ResumeGame -> handleResume()
            is TaskPrioritizerEvent.FinishGame -> handleFinish()
        }
    }

    override fun onStart() {
        handleStartGame()
    }

    override fun onPause() {
        handlePause()
    }

    override fun onResume() {
        handleResume()
    }

    override fun onFinish(): TaskPrioritizerResult {
        val state = _state.value
        val totalTime = System.currentTimeMillis() - gameStartTime
        val totalAttempts = state.correctPlacements + state.incorrectPlacements

        val accuracy = if (totalAttempts > 0) {
            state.correctPlacements.toFloat() / totalAttempts.toFloat()
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

        val passed = scoringSystem.hasPassed(accuracy)

        return TaskPrioritizerResult(
            score = state.score,
            accuracy = accuracy,
            timeTaken = totalTime,
            difficulty = state.difficulty,
            xpEarned = xp,
            passed = passed,
            totalRounds = state.totalRounds,
            correctPlacements = state.correctPlacements,
            incorrectPlacements = state.incorrectPlacements,
            averageResponseTime = avgResponseTime,
            perfectRounds = perfectRoundsList.count { it },
            fastestRound = if (fastestRoundTime != Long.MAX_VALUE) fastestRoundTime else 0L
        )
    }

    override fun onRestart() {
        timerJob?.cancel()
        gameStartTime = 0
        roundResponseTimes.clear()
        perfectRoundsList.clear()
        fastestRoundTime = Long.MAX_VALUE
        taskGenerator.reset()

        val params = TaskPrioritizerParameters.forDifficulty(initialDifficulty)
        _state.value = TaskPrioritizerState(
            phase = GamePhase.READY,
            difficulty = initialDifficulty,
            totalRounds = params.roundsCount,
            tasksPerRound = params.tasksPerRound,
            timeLimit = params.timeLimit
        )
    }

    private fun handleStartGame() {
        gameStartTime = System.currentTimeMillis()
        roundResponseTimes.clear()
        perfectRoundsList.clear()
        taskGenerator.reset()

        _state.value = _state.value.copy(
            phase = GamePhase.PLAYING,
            currentRound = 0,
            score = 0,
            correctPlacements = 0,
            incorrectPlacements = 0
        )

        // Start first round
        processEvent(TaskPrioritizerEvent.StartRound)
    }

    private fun handleStartRound() {
        val state = _state.value

        if (state.currentRound >= state.totalRounds) {
            processEvent(TaskPrioritizerEvent.FinishGame)
            return
        }

        // Generate tasks for this round
        val tasks = taskGenerator.generateTasks(state.tasksPerRound)

        _state.value = state.copy(
            currentRound = state.currentRound + 1,
            availableTasks = tasks,
            placedTasks = emptyMap(),
            roundStartTime = System.currentTimeMillis(),
            showFeedback = false,
            feedbackTask = null
        )

        // Start timer if time limit is set
        state.timeLimit?.let { timeLimit ->
            startTimer(timeLimit)
        }
    }

    private fun handlePlaceTask(taskId: String, quadrant: EisenhowerQuadrant) {
        val state = _state.value
        if (state.phase != GamePhase.PLAYING || state.showFeedback) return

        val task = state.availableTasks.find { it.id == taskId } ?: return
        val isCorrect = task.correctQuadrant == quadrant

        // Update placements
        val newPlacements = state.placedTasks.toMutableMap()
        newPlacements[taskId] = quadrant

        // Calculate score for this placement
        val placementScore = if (isCorrect) {
            val responseTime = System.currentTimeMillis() - state.roundStartTime
            scoringSystem.calculateAnswerScore(
                isCorrect = true,
                responseTime = responseTime,
                currentStreak = 1,
                difficulty = state.difficulty
            )
        } else {
            ScoringSystem.BASE_SCORE_INCORRECT
        }

        _state.value = state.copy(
            placedTasks = newPlacements,
            score = state.score + placementScore,
            correctPlacements = if (isCorrect) state.correctPlacements + 1 else state.correctPlacements,
            incorrectPlacements = if (!isCorrect) state.incorrectPlacements + 1 else state.incorrectPlacements,
            showFeedback = true,
            feedbackTask = task,
            isCorrectPlacement = isCorrect
        )
    }

    private fun handleDismissFeedback() {
        val state = _state.value

        _state.value = state.copy(
            showFeedback = false,
            feedbackTask = null
        )

        // Check if all tasks are placed
        if (state.allTasksPlaced) {
            handleSubmitPlacements()
        }
    }

    private fun handleSubmitPlacements() {
        val state = _state.value
        if (state.phase != GamePhase.PLAYING) return

        timerJob?.cancel()

        // Calculate round completion time
        val roundTime = System.currentTimeMillis() - state.roundStartTime
        roundResponseTimes.add(roundTime)

        // Track fastest round
        if (roundTime < fastestRoundTime) {
            fastestRoundTime = roundTime
        }

        // Check if round was perfect
        val correctInRound = state.placedTasks.count { (taskId, quadrant) ->
            val task = state.availableTasks.find { it.id == taskId }
            task?.correctQuadrant == quadrant
        }
        perfectRoundsList.add(correctInRound == state.availableTasks.size)

        _state.value = state.copy(phase = GamePhase.REVIEW)
    }

    private fun handleNextRound() {
        _state.value = _state.value.copy(phase = GamePhase.PLAYING)
        processEvent(TaskPrioritizerEvent.StartRound)
    }

    private fun handlePause() {
        timerJob?.cancel()
        _state.value = _state.value.copy(phase = GamePhase.PAUSED)
    }

    private fun handleResume() {
        val state = _state.value
        _state.value = state.copy(phase = GamePhase.PLAYING)

        // Restart timer if needed
        state.timeLimit?.let { timeLimit ->
            val elapsed = System.currentTimeMillis() - state.roundStartTime
            val remaining = timeLimit - elapsed
            if (remaining > 0) {
                startTimer(remaining)
            } else {
                handleSubmitPlacements()
            }
        }
    }

    private fun handleFinish() {
        timerJob?.cancel()
        _state.value = _state.value.copy(phase = GamePhase.COMPLETED)
    }

    private fun startTimer(duration: Long) {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            var remaining = duration
            while (remaining > 0) {
                _state.value = _state.value.copy(timeRemaining = remaining)
                delay(100)
                remaining -= 100
            }
            // Time's up - auto submit
            if (_state.value.phase == GamePhase.PLAYING) {
                handleSubmitPlacements()
            }
        }
    }
}
