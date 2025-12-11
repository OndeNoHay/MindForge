package com.mindforge.game.meetingrecall.engine

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.BaseGameEngine
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.scoring.ScoringSystem
import com.mindforge.game.meetingrecall.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Game engine for the Meeting Recall game
 */
class MeetingRecallEngine(
    private val initialDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    private val coroutineScope: CoroutineScope,
    private val scoringSystem: ScoringSystem = ScoringSystem()
) : BaseGameEngine<MeetingRecallState, MeetingRecallEvent, MeetingRecallResult>() {

    private val _state = MutableStateFlow(MeetingRecallState(difficulty = initialDifficulty))
    override val currentState: StateFlow<MeetingRecallState> = _state.asStateFlow()

    private val meetingGenerator = MeetingGenerator()
    private var readingTimerJob: Job? = null
    private var gameStartTime: Long = 0
    private val perfectRounds = mutableListOf<Boolean>()
    private val readingTimes = mutableListOf<Long>()

    override fun onProcessEvent(event: MeetingRecallEvent) {
        when (event) {
            is MeetingRecallEvent.StartGame -> startGame()
            is MeetingRecallEvent.StartRound -> startRound()
            is MeetingRecallEvent.StartReading -> startReading()
            is MeetingRecallEvent.FinishReading -> finishReading()
            is MeetingRecallEvent.StartTest -> startTest()
            is MeetingRecallEvent.AnswerQuestion -> answerQuestion(event.questionId, event.selectedAnswer)
            is MeetingRecallEvent.NextQuestion -> nextQuestion()
            is MeetingRecallEvent.CompleteTest -> completeTest()
            is MeetingRecallEvent.NextRound -> nextRound()
            is MeetingRecallEvent.PauseGame -> pauseGame()
            is MeetingRecallEvent.ResumeGame -> resumeGame()
            is MeetingRecallEvent.FinishGame -> finishGame()
        }
    }

    override fun onStart() {
        startGame()
    }

    override fun onPause() {
        pauseGame()
    }

    override fun onResume() {
        resumeGame()
    }

    override fun onFinish(): MeetingRecallResult {
        return finishGameInternal()
    }

    override fun onRestart() {
        readingTimerJob?.cancel()
        meetingGenerator.reset()
        perfectRounds.clear()
        readingTimes.clear()
        _state.value = MeetingRecallState(difficulty = initialDifficulty)
    }

    private fun startGame() {
        meetingGenerator.reset()
        gameStartTime = System.currentTimeMillis()
        perfectRounds.clear()
        readingTimes.clear()

        val params = MeetingRecallParameters.forDifficulty(initialDifficulty)

        _state.value = MeetingRecallState(
            phase = GamePhase.PLAYING,
            difficulty = initialDifficulty,
            meetingRecallPhase = MeetingRecallPhase.READING,
            currentRound = 1,
            totalRounds = params.roundsCount,
            readingTimePerMeeting = params.readingTimePerMeeting
        )

        startRound()
    }

    private fun startRound() {
        val params = MeetingRecallParameters.forDifficulty(initialDifficulty)

        val meeting = meetingGenerator.generateMeeting(
            attendeesCount = params.attendeesCount,
            topicsCount = params.topicsCount,
            actionItemsCount = params.actionItemsCount,
            includeLocation = params.includeLocation,
            includeDuration = params.includeDuration
        )

        _state.value = _state.value.copy(
            meetingRecallPhase = MeetingRecallPhase.READING,
            currentMeeting = meeting,
            readingStartTime = System.currentTimeMillis(),
            timeRemaining = params.readingTimePerMeeting,
            questions = emptyList(),
            currentQuestionIndex = 0,
            answeredQuestions = emptyMap(),
            correctAnswers = 0,
            incorrectAnswers = 0,
            detailQuestionsCorrect = 0
        )

        startReading()
    }

    private fun startReading() {
        readingTimerJob?.cancel()
        readingTimerJob = coroutineScope.launch {
            val totalTime = _state.value.readingTimePerMeeting
            val startTime = System.currentTimeMillis()

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (totalTime - elapsed).coerceAtLeast(0)

                _state.value = _state.value.copy(timeRemaining = remaining)

                if (remaining <= 0) {
                    finishReading()
                    break
                }

                delay(100) // Update every 100ms
            }
        }
    }

    private fun finishReading() {
        readingTimerJob?.cancel()

        val readingTime = System.currentTimeMillis() - _state.value.readingStartTime
        readingTimes.add(readingTime)

        // Automatically start test
        startTest()
    }

    private fun startTest() {
        val params = MeetingRecallParameters.forDifficulty(initialDifficulty)
        val meeting = _state.value.currentMeeting ?: return

        val questions = generateQuestions(meeting, params)

        _state.value = _state.value.copy(
            meetingRecallPhase = MeetingRecallPhase.TEST,
            questions = questions,
            currentQuestionIndex = 0,
            testStartTime = System.currentTimeMillis(),
            timeRemaining = null
        )
    }

    private fun generateQuestions(meeting: Meeting, params: MeetingRecallParameters): List<MeetingQuestion> {
        val questions = mutableListOf<MeetingQuestion>()
        var questionCount = 0

        // Generate attendee questions
        if (meeting.attendees.isNotEmpty() && questionCount < params.questionsPerMeeting) {
            val attendee = meeting.attendees.random()
            val otherNames = meeting.attendees.filter { it.name != attendee.name }.map { it.name }
            val wrongOptions = (otherNames + listOf("Roberto Sánchez", "Marta Vega", "Luis Torres")).shuffled().take(3)
            val options = (wrongOptions + attendee.name).shuffled()

            questions.add(
                MeetingQuestion(
                    id = "q_attendee_$questionCount",
                    type = QuestionType.ATTENDEE,
                    questionText = "¿Quién asistió a la reunión con el rol de ${attendee.role}?",
                    options = options,
                    correctAnswer = attendee.name
                )
            )
            questionCount++
        }

        // Generate topic questions
        meeting.topics.forEach { topic ->
            if (questionCount >= params.questionsPerMeeting) return@forEach

            val keyPoint = topic.keyPoints.random()
            val wrongPoints = listOf(
                "Retraso en el cronograma del proyecto",
                "Aumento del presupuesto en 15%",
                "Cambio en la metodología de trabajo",
                "Contratación de personal adicional"
            ).shuffled().take(3)
            val options = (wrongPoints + keyPoint).shuffled()

            questions.add(
                MeetingQuestion(
                    id = "q_topic_$questionCount",
                    type = QuestionType.TOPIC,
                    questionText = "En el tema '${topic.title}', ¿cuál fue uno de los puntos clave discutidos?",
                    options = options,
                    correctAnswer = keyPoint
                )
            )
            questionCount++
        }

        // Generate decision questions
        meeting.topics.forEach { topic ->
            if (questionCount >= params.questionsPerMeeting) return@forEach

            if (topic.decisions.isNotEmpty()) {
                val decision = topic.decisions.random()
                val wrongDecisions = listOf(
                    "Cancelar el proyecto indefinidamente",
                    "Duplicar el equipo de trabajo",
                    "Cambiar completamente la estrategia",
                    "Posponer la decisión hasta próximo trimestre"
                ).shuffled().take(3)
                val options = (wrongDecisions + decision).shuffled()

                questions.add(
                    MeetingQuestion(
                        id = "q_decision_$questionCount",
                        type = QuestionType.DECISION,
                        questionText = "¿Cuál fue una de las decisiones tomadas en '${topic.title}'?",
                        options = options,
                        correctAnswer = decision,
                        isDetailQuestion = params.includeDetailQuestions
                    )
                )
                questionCount++
            }
        }

        // Generate action item questions
        meeting.actionItems.forEach { actionItem ->
            if (questionCount >= params.questionsPerMeeting) return@forEach

            val otherAssignees = meeting.attendees.map { it.name }.filter { it != actionItem.assignee }
            val wrongAssignees = (otherAssignees + listOf("Alguien externo", "Por definir")).shuffled().take(3)
            val options = (wrongAssignees + actionItem.assignee).shuffled()

            questions.add(
                MeetingQuestion(
                    id = "q_action_$questionCount",
                    type = QuestionType.ACTION_ITEM,
                    questionText = "¿Quién fue asignado para '${actionItem.task}'?",
                    options = options,
                    correctAnswer = actionItem.assignee
                )
            )
            questionCount++
        }

        // Generate deadline questions
        if (meeting.actionItems.isNotEmpty() && questionCount < params.questionsPerMeeting) {
            val actionItem = meeting.actionItems.random()
            val wrongDeadlines = listOf(
                "Esta semana",
                "Próxima semana",
                "Fin de mes",
                "En 2 semanas",
                "Próximo mes"
            ).filter { it != actionItem.deadline }.shuffled().take(3)
            val options = (wrongDeadlines + actionItem.deadline).shuffled()

            questions.add(
                MeetingQuestion(
                    id = "q_deadline_$questionCount",
                    type = QuestionType.DEADLINE,
                    questionText = "¿Cuál es el plazo para '${actionItem.task}'?",
                    options = options,
                    correctAnswer = actionItem.deadline
                )
            )
            questionCount++
        }

        // Generate detail questions if enabled
        if (params.includeDetailQuestions && questionCount < params.questionsPerMeeting) {
            if (meeting.location != null) {
                val wrongLocations = listOf(
                    "Sala de Juntas - Planta 2",
                    "Oficina Central",
                    "Sala Virtual - Zoom",
                    "Centro de Capacitación"
                ).filter { it != meeting.location }.shuffled().take(3)
                val options = (wrongLocations + meeting.location).shuffled()

                questions.add(
                    MeetingQuestion(
                        id = "q_detail_location",
                        type = QuestionType.DETAIL,
                        questionText = "¿Dónde se realizó la reunión?",
                        options = options,
                        correctAnswer = meeting.location,
                        isDetailQuestion = true
                    )
                )
                questionCount++
            }
        }

        return questions.shuffled().take(params.questionsPerMeeting)
    }

    private fun answerQuestion(questionId: String, selectedAnswer: String) {
        val currentQuestion = _state.value.questions.find { it.id == questionId } ?: return
        val isCorrect = currentQuestion.correctAnswer == selectedAnswer

        val newAnsweredQuestions = _state.value.answeredQuestions + (questionId to isCorrect)
        val newCorrectAnswers = if (isCorrect) _state.value.correctAnswers + 1 else _state.value.correctAnswers
        val newIncorrectAnswers = if (!isCorrect) _state.value.incorrectAnswers + 1 else _state.value.incorrectAnswers
        val newDetailQuestionsCorrect = if (isCorrect && currentQuestion.isDetailQuestion) {
            _state.value.detailQuestionsCorrect + 1
        } else {
            _state.value.detailQuestionsCorrect
        }

        // Calculate score with bonus for detail questions
        val baseScore = scoringSystem.calculateAnswerScore(
            isCorrect = isCorrect,
            responseTime = 0L,
            difficulty = _state.value.difficulty,
            currentStreak = if (isCorrect) 1 else 0
        )

        val bonusMultiplier = if (currentQuestion.isDetailQuestion && isCorrect) 1.5f else 1f
        val pointsForAnswer = (baseScore * bonusMultiplier).toInt()

        _state.value = _state.value.copy(
            answeredQuestions = newAnsweredQuestions,
            correctAnswers = newCorrectAnswers,
            incorrectAnswers = newIncorrectAnswers,
            detailQuestionsCorrect = newDetailQuestionsCorrect,
            score = _state.value.score + pointsForAnswer
        )
    }

    private fun nextQuestion() {
        val newIndex = _state.value.currentQuestionIndex + 1

        if (newIndex < _state.value.questions.size) {
            _state.value = _state.value.copy(currentQuestionIndex = newIndex)
        } else {
            completeTest()
        }
    }

    private fun completeTest() {
        val isPerfectRound = _state.value.incorrectAnswers == 0
        perfectRounds.add(isPerfectRound)

        _state.value = _state.value.copy(
            meetingRecallPhase = MeetingRecallPhase.REVIEW
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
        readingTimerJob?.cancel()
        _state.value = _state.value.copy(phase = GamePhase.PAUSED)
    }

    private fun resumeGame() {
        _state.value = _state.value.copy(phase = GamePhase.PLAYING)

        // Resume timer if in reading phase
        if (_state.value.meetingRecallPhase == MeetingRecallPhase.READING) {
            startReading()
        }
    }

    private fun finishGame() {
        _state.value = _state.value.copy(phase = GamePhase.COMPLETED)
    }

    private fun finishGameInternal(): MeetingRecallResult {
        readingTimerJob?.cancel()

        val totalTime = System.currentTimeMillis() - gameStartTime
        val accuracy = _state.value.accuracy

        val xpEarned = scoringSystem.calculateSessionXP(
            totalScore = _state.value.score,
            accuracy = accuracy,
            difficulty = _state.value.difficulty,
            timeTaken = totalTime
        )

        val totalQuestions = _state.value.totalRounds * _state.value.questions.size

        val result = MeetingRecallResult(
            score = _state.value.score,
            accuracy = accuracy,
            timeTaken = totalTime,
            difficulty = _state.value.difficulty,
            xpEarned = xpEarned,
            passed = scoringSystem.hasPassed(accuracy),
            totalRounds = _state.value.totalRounds,
            totalQuestions = totalQuestions,
            correctAnswers = _state.value.correctAnswers,
            incorrectAnswers = _state.value.incorrectAnswers,
            detailQuestionsCorrect = _state.value.detailQuestionsCorrect,
            averageReadingTime = if (readingTimes.isNotEmpty()) readingTimes.average().toLong() else 0L,
            perfectRounds = perfectRounds.count { it }
        )

        _state.value = _state.value.copy(
            phase = GamePhase.COMPLETED
        )

        return result
    }
}
