package com.mindforge.game.conceptlinker.engine

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.BaseGameEngine
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.engine.ScoringSystem
import com.mindforge.game.conceptlinker.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Game engine for the Concept Linker game
 */
class ConceptLinkerEngine(
    private val initialDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    private val coroutineScope: CoroutineScope,
    private val scoringSystem: ScoringSystem = ScoringSystem()
) : BaseGameEngine<ConceptLinkerState, ConceptLinkerEvent, ConceptLinkerResult>() {

    private val _state = MutableStateFlow(ConceptLinkerState(difficulty = initialDifficulty))
    override val state: StateFlow<ConceptLinkerState> = _state.asStateFlow()

    private val conceptGenerator = ConceptGenerator()
    private var studyTimerJob: Job? = null
    private var gameStartTime: Long = 0
    private val perfectRounds = mutableListOf<Boolean>()
    private var connectionIdCounter = 0

    override suspend fun handleEvent(event: ConceptLinkerEvent) {
        when (event) {
            is ConceptLinkerEvent.StartGame -> startGame()
            is ConceptLinkerEvent.StartRound -> startRound()
            is ConceptLinkerEvent.FinishStudy -> finishStudy()
            is ConceptLinkerEvent.StartConnecting -> startConnecting()
            is ConceptLinkerEvent.SelectNode -> selectNode(event.nodeId)
            is ConceptLinkerEvent.CancelSelection -> cancelSelection()
            is ConceptLinkerEvent.CreateConnection -> createConnection(event.sourceNodeId, event.targetNodeId, event.relationType)
            is ConceptLinkerEvent.DeleteConnection -> deleteConnection(event.connectionId)
            is ConceptLinkerEvent.CompleteRound -> completeRound()
            is ConceptLinkerEvent.NextRound -> nextRound()
            is ConceptLinkerEvent.PauseGame -> pauseGame()
            is ConceptLinkerEvent.ResumeGame -> resumeGame()
            is ConceptLinkerEvent.FinishGame -> finishGame()
        }
    }

    private fun startGame() {
        conceptGenerator.reset()
        gameStartTime = System.currentTimeMillis()
        perfectRounds.clear()
        connectionIdCounter = 0

        val params = ConceptLinkerParameters.forDifficulty(initialDifficulty)

        _state.value = ConceptLinkerState(
            phase = GamePhase.PLAYING,
            difficulty = initialDifficulty,
            conceptLinkerPhase = ConceptLinkerPhase.STUDY,
            currentRound = 1,
            totalRounds = params.roundsCount,
            studyTimePerRound = params.studyTimePerRound
        )

        startRound()
    }

    private fun startRound() {
        val params = ConceptLinkerParameters.forDifficulty(initialDifficulty)

        val (nodes, correctConnections) = conceptGenerator.generateScenario(
            nodesCount = params.nodesPerRound,
            connectionsCount = params.requiredConnectionsPerRound,
            includeStakeholders = params.includeStakeholders,
            includeDeadlines = params.includeDeadlines,
            includeResources = params.includeResources
        )

        _state.value = _state.value.copy(
            conceptLinkerPhase = ConceptLinkerPhase.STUDY,
            nodes = nodes,
            correctConnections = correctConnections,
            userConnections = emptyList(),
            selectedNodeId = null,
            showRelationTypeSelector = false,
            pendingConnection = null,
            correctConnectionsCount = 0,
            incorrectConnectionsCount = 0,
            missedConnectionsCount = 0,
            studyStartTime = System.currentTimeMillis(),
            timeRemaining = params.studyTimePerRound
        )

        startStudyTimer()
    }

    private fun startStudyTimer() {
        studyTimerJob?.cancel()
        studyTimerJob = coroutineScope.launch {
            val totalTime = _state.value.studyTimePerRound
            val startTime = System.currentTimeMillis()

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (totalTime - elapsed).coerceAtLeast(0)

                _state.value = _state.value.copy(timeRemaining = remaining)

                if (remaining <= 0) {
                    finishStudy()
                    break
                }

                delay(100)
            }
        }
    }

    private fun finishStudy() {
        studyTimerJob?.cancel()
        startConnecting()
    }

    private fun startConnecting() {
        _state.value = _state.value.copy(
            conceptLinkerPhase = ConceptLinkerPhase.CONNECTING,
            connectingStartTime = System.currentTimeMillis(),
            timeRemaining = null
        )
    }

    private fun selectNode(nodeId: String) {
        val currentlySelected = _state.value.selectedNodeId

        if (currentlySelected == null) {
            // First node selection
            _state.value = _state.value.copy(selectedNodeId = nodeId)
        } else if (currentlySelected == nodeId) {
            // Clicking same node - deselect
            cancelSelection()
        } else {
            // Second node selection - prepare to create connection
            _state.value = _state.value.copy(
                pendingConnection = Pair(currentlySelected, nodeId),
                showRelationTypeSelector = true
            )
        }
    }

    private fun cancelSelection() {
        _state.value = _state.value.copy(
            selectedNodeId = null,
            showRelationTypeSelector = false,
            pendingConnection = null
        )
    }

    private fun createConnection(sourceNodeId: String, targetNodeId: String, relationType: RelationType) {
        // Check if connection already exists
        val existingConnection = _state.value.userConnections.find {
            (it.sourceNodeId == sourceNodeId && it.targetNodeId == targetNodeId) ||
            (it.sourceNodeId == targetNodeId && it.targetNodeId == sourceNodeId)
        }

        if (existingConnection != null) {
            // Connection already exists
            cancelSelection()
            return
        }

        // Create new connection
        val connection = Connection(
            id = "connection_${connectionIdCounter++}",
            sourceNodeId = sourceNodeId,
            targetNodeId = targetNodeId,
            relationType = relationType
        )

        val newUserConnections = _state.value.userConnections + connection

        // Clear selection state
        _state.value = _state.value.copy(
            userConnections = newUserConnections,
            selectedNodeId = null,
            showRelationTypeSelector = false,
            pendingConnection = null
        )
    }

    private fun deleteConnection(connectionId: String) {
        _state.value = _state.value.copy(
            userConnections = _state.value.userConnections.filter { it.id != connectionId }
        )
    }

    private fun completeRound() {
        // Evaluate connections
        val userConnections = _state.value.userConnections
        val correctConnections = _state.value.correctConnections

        var correctCount = 0
        var incorrectCount = 0

        // Check each user connection
        userConnections.forEach { userConn ->
            val isCorrect = correctConnections.any { correctConn ->
                // Check if nodes match (bidirectional)
                val nodesMatch = (
                    (correctConn.sourceNodeId == userConn.sourceNodeId && correctConn.targetNodeId == userConn.targetNodeId) ||
                    (correctConn.sourceNodeId == userConn.targetNodeId && correctConn.targetNodeId == userConn.sourceNodeId)
                )

                // Check if relation type is valid
                val relationValid = correctConn.validRelationTypes.contains(userConn.relationType)

                nodesMatch && relationValid
            }

            if (isCorrect) {
                correctCount++
            } else {
                incorrectCount++
            }
        }

        // Check for missed connections
        val foundCorrectConnections = mutableSetOf<String>()
        userConnections.forEach { userConn ->
            correctConnections.forEach { correctConn ->
                if ((correctConn.sourceNodeId == userConn.sourceNodeId && correctConn.targetNodeId == userConn.targetNodeId) ||
                    (correctConn.sourceNodeId == userConn.targetNodeId && correctConn.targetNodeId == userConn.sourceNodeId)) {
                    if (correctConn.validRelationTypes.contains(userConn.relationType)) {
                        foundCorrectConnections.add("${correctConn.sourceNodeId}-${correctConn.targetNodeId}")
                    }
                }
            }
        }

        val missedCount = correctConnections.size - foundCorrectConnections.size

        // Calculate score for round
        val pointsPerCorrectConnection = 100
        val penaltyPerIncorrect = 20
        val roundScore = (correctCount * pointsPerCorrectConnection - incorrectCount * penaltyPerIncorrect).coerceAtLeast(0)

        val scoredPoints = scoringSystem.calculateScore(
            baseScore = roundScore,
            difficulty = _state.value.difficulty,
            accuracy = if (correctCount + incorrectCount > 0) correctCount.toFloat() / (correctCount + incorrectCount).toFloat() else 0f,
            timeBonus = 0
        )

        val isPerfectRound = incorrectCount == 0 && missedCount == 0
        perfectRounds.add(isPerfectRound)

        _state.value = _state.value.copy(
            conceptLinkerPhase = ConceptLinkerPhase.REVIEW,
            correctConnectionsCount = _state.value.correctConnectionsCount + correctCount,
            incorrectConnectionsCount = _state.value.incorrectConnectionsCount + incorrectCount,
            missedConnectionsCount = _state.value.missedConnectionsCount + missedCount,
            score = _state.value.score + scoredPoints
        )
    }

    private fun nextRound() {
        if (_state.value.currentRound < _state.value.totalRounds) {
            _state.value = _state.value.copy(
                currentRound = _state.value.currentRound + 1
            )
            startRound()
        } else {
            finishGame()
        }
    }

    private fun pauseGame() {
        studyTimerJob?.cancel()
        _state.value = _state.value.copy(phase = GamePhase.PAUSED)
    }

    private fun resumeGame() {
        _state.value = _state.value.copy(phase = GamePhase.PLAYING)

        if (_state.value.conceptLinkerPhase == ConceptLinkerPhase.STUDY) {
            startStudyTimer()
        }
    }

    private fun finishGame() {
        studyTimerJob?.cancel()

        val totalTime = System.currentTimeMillis() - gameStartTime
        val totalConnections = _state.value.correctConnectionsCount + _state.value.incorrectConnectionsCount
        val accuracy = if (totalConnections > 0) {
            _state.value.correctConnectionsCount.toFloat() / totalConnections.toFloat()
        } else {
            0f
        }

        val totalPossibleConnections = _state.value.correctConnectionsCount + _state.value.missedConnectionsCount
        val completionRate = if (totalPossibleConnections > 0) {
            _state.value.correctConnectionsCount.toFloat() / totalPossibleConnections.toFloat()
        } else {
            0f
        }

        val xpEarned = scoringSystem.calculateXP(
            score = _state.value.score,
            difficulty = _state.value.difficulty,
            accuracy = accuracy
        )

        val result = ConceptLinkerResult(
            score = _state.value.score,
            accuracy = accuracy,
            timeTaken = totalTime,
            difficulty = _state.value.difficulty,
            xpEarned = xpEarned,
            passed = accuracy >= 0.6f && completionRate >= 0.5f,  // 60% accuracy and 50% completion
            totalRounds = _state.value.totalRounds,
            totalPossibleConnections = totalPossibleConnections,
            correctConnectionsCount = _state.value.correctConnectionsCount,
            incorrectConnectionsCount = _state.value.incorrectConnectionsCount,
            missedConnectionsCount = _state.value.missedConnectionsCount,
            completionRate = completionRate,
            perfectRounds = perfectRounds.count { it }
        )

        _state.value = _state.value.copy(
            phase = GamePhase.FINISHED
        )

        emitResult(result)
    }

    override fun reset() {
        studyTimerJob?.cancel()
        conceptGenerator.reset()
        perfectRounds.clear()
        connectionIdCounter = 0
        _state.value = ConceptLinkerState(difficulty = initialDifficulty)
    }
}
