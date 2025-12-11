package com.mindforge.game.nameface.engine

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.BaseGameEngine
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.scoring.ScoringSystem
import com.mindforge.game.nameface.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Game engine for the Name-Face Associator game
 */
class NameFaceEngine(
    private val initialDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    private val coroutineScope: CoroutineScope,
    private val scoringSystem: ScoringSystem = ScoringSystem()
) : BaseGameEngine<NameFaceState, NameFaceEvent, NameFaceResult>() {

    private val _state = MutableStateFlow(NameFaceState(difficulty = initialDifficulty))
    override val currentState: StateFlow<NameFaceState> = _state.asStateFlow()

    private val personGenerator = PersonGenerator()
    private var studyTimerJob: Job? = null
    private var gameStartTime: Long = 0
    private val perfectRounds = mutableListOf<Boolean>()
    private val studyTimes = mutableListOf<Long>()

    override fun onProcessEvent(event: NameFaceEvent) {
        when (event) {
            is NameFaceEvent.StartGame -> startGame()
            is NameFaceEvent.StartRound -> startRound()
            is NameFaceEvent.NextPerson -> nextPerson()
            is NameFaceEvent.StartTest -> startTest()
            is NameFaceEvent.AnswerQuestion -> answerQuestion(event.questionId, event.selectedPersonId)
            is NameFaceEvent.NextQuestion -> nextQuestion()
            is NameFaceEvent.CompleteTest -> completeTest()
            is NameFaceEvent.NextRound -> nextRound()
            is NameFaceEvent.PauseGame -> pauseGame()
            is NameFaceEvent.ResumeGame -> resumeGame()
            is NameFaceEvent.FinishGame -> finishGame()
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

    override fun onFinish(): NameFaceResult {
        return finishGameInternal()
    }

    override fun onRestart() {
        studyTimerJob?.cancel()
        personGenerator.reset()
        perfectRounds.clear()
        studyTimes.clear()
        _state.value = NameFaceState(difficulty = initialDifficulty)
    }

    private fun startGame() {
        personGenerator.reset()
        gameStartTime = System.currentTimeMillis()
        perfectRounds.clear()
        studyTimes.clear()

        val params = NameFaceParameters.forDifficulty(initialDifficulty)

        _state.value = NameFaceState(
            phase = GamePhase.PLAYING,
            difficulty = initialDifficulty,
            nameFacePhase = NameFacePhase.STUDY,
            currentRound = 1,
            totalRounds = params.roundsCount,
            studyTimePerPerson = params.studyTimePerPerson
        )

        startRound()
    }

    private fun startRound() {
        val params = NameFaceParameters.forDifficulty(initialDifficulty)
        val people = personGenerator.generatePeople(
            count = params.peopleCount,
            includeRole = params.includeRole,
            includeCompany = params.includeCompany,
            includeDepartment = params.includeDepartment
        )

        _state.value = _state.value.copy(
            nameFacePhase = NameFacePhase.STUDY,
            peopleToLearn = people,
            currentStudyIndex = 0,
            studyStartTime = System.currentTimeMillis(),
            questions = emptyList(),
            currentQuestionIndex = 0,
            answeredQuestions = emptyMap(),
            correctAnswers = 0,
            incorrectAnswers = 0
        )

        startStudyTimer()
    }

    private fun startStudyTimer() {
        studyTimerJob?.cancel()
        studyTimerJob = coroutineScope.launch {
            delay(_state.value.studyTimePerPerson)
            if (_state.value.currentStudyIndex < _state.value.peopleToLearn.size - 1) {
                nextPerson()
            } else {
                // Study phase complete, move to test
                processEvent(NameFaceEvent.StartTest)
            }
        }
    }

    private fun nextPerson() {
        val newIndex = _state.value.currentStudyIndex + 1
        _state.value = _state.value.copy(currentStudyIndex = newIndex)

        if (newIndex < _state.value.peopleToLearn.size) {
            startStudyTimer()
        }
    }

    private fun startTest() {
        studyTimerJob?.cancel()

        val studyTime = System.currentTimeMillis() - _state.value.studyStartTime
        studyTimes.add(studyTime)

        val params = NameFaceParameters.forDifficulty(initialDifficulty)
        val questions = generateQuestions(params)

        _state.value = _state.value.copy(
            nameFacePhase = NameFacePhase.TEST,
            questions = questions,
            currentQuestionIndex = 0,
            testStartTime = System.currentTimeMillis()
        )
    }

    private fun generateQuestions(params: NameFaceParameters): List<TestQuestion> {
        val people = _state.value.peopleToLearn
        val questions = mutableListOf<TestQuestion>()

        // Determine question types based on difficulty
        val questionTypes = mutableListOf<QuestionType>()

        // Always include NAME questions
        questionTypes.add(QuestionType.NAME)

        if (params.includeRole) {
            questionTypes.add(QuestionType.ROLE)
        }

        // Add face-from-name questions for variety
        questionTypes.add(QuestionType.FACE_FROM_NAME)

        // Generate questions for each person
        people.forEach { person ->
            val questionType = questionTypes.random()

            val options = when (questionType) {
                QuestionType.FACE_FROM_NAME -> {
                    // For this type, we need different people as options
                    val otherPeople = people.filter { it.id != person.id }.shuffled().take(3)
                    (otherPeople + person).shuffled()
                }
                else -> {
                    // For name/role questions, we show the same face with different text options
                    val otherPeople = people.filter { it.id != person.id }.shuffled().take(3)
                    (otherPeople + person).shuffled()
                }
            }

            val questionText = when (questionType) {
                QuestionType.NAME -> "¿Cuál es el nombre de esta persona?"
                QuestionType.ROLE -> "¿Cuál es el rol de ${person.fullName}?"
                QuestionType.FACE_FROM_NAME -> "¿Cuál es el rostro de ${person.fullName}?"
                QuestionType.COMPLETE -> "¿Quién es esta persona y cuál es su rol?"
            }

            questions.add(
                TestQuestion(
                    id = "q_${person.id}",
                    type = questionType,
                    correctPerson = person,
                    options = options,
                    questionText = questionText
                )
            )
        }

        return questions.shuffled()
    }

    private fun answerQuestion(questionId: String, selectedPersonId: String) {
        val currentQuestion = _state.value.questions.find { it.id == questionId } ?: return
        val isCorrect = currentQuestion.correctPerson.id == selectedPersonId

        val newAnsweredQuestions = _state.value.answeredQuestions + (questionId to isCorrect)
        val newCorrectAnswers = if (isCorrect) _state.value.correctAnswers + 1 else _state.value.correctAnswers
        val newIncorrectAnswers = if (!isCorrect) _state.value.incorrectAnswers + 1 else _state.value.incorrectAnswers

        // Calculate score
        val pointsForAnswer = scoringSystem.calculateAnswerScore(
            isCorrect = isCorrect,
            responseTime = 0L,
            difficulty = _state.value.difficulty,
            currentStreak = if (isCorrect) 1 else 0
        )

        _state.value = _state.value.copy(
            answeredQuestions = newAnsweredQuestions,
            correctAnswers = newCorrectAnswers,
            incorrectAnswers = newIncorrectAnswers,
            score = _state.value.score + pointsForAnswer
        )
    }

    private fun nextQuestion() {
        val newIndex = _state.value.currentQuestionIndex + 1

        if (newIndex < _state.value.questions.size) {
            _state.value = _state.value.copy(currentQuestionIndex = newIndex)
        } else {
            // All questions answered
            completeTest()
        }
    }

    private fun completeTest() {
        val isPerfectRound = _state.value.incorrectAnswers == 0
        perfectRounds.add(isPerfectRound)

        _state.value = _state.value.copy(
            nameFacePhase = NameFacePhase.REVIEW
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

        // Resume timer if in study phase
        if (_state.value.nameFacePhase == NameFacePhase.STUDY) {
            startStudyTimer()
        }
    }

    private fun finishGame() {
        _state.value = _state.value.copy(phase = GamePhase.COMPLETED)
    }

    private fun finishGameInternal(): NameFaceResult {
        studyTimerJob?.cancel()

        val totalTime = System.currentTimeMillis() - gameStartTime
        val accuracy = _state.value.accuracy

        val xpEarned = scoringSystem.calculateSessionXP(
            totalScore = _state.value.score,
            accuracy = accuracy,
            difficulty = _state.value.difficulty,
            timeTaken = totalTime
        )

        val result = NameFaceResult(
            score = _state.value.score,
            accuracy = accuracy,
            timeTaken = totalTime,
            difficulty = _state.value.difficulty,
            xpEarned = xpEarned,
            passed = scoringSystem.hasPassed(accuracy),
            totalRounds = _state.value.totalRounds,
            totalPeople = _state.value.peopleToLearn.size * _state.value.totalRounds,
            correctAnswers = _state.value.correctAnswers,
            incorrectAnswers = _state.value.incorrectAnswers,
            perfectRounds = perfectRounds.count { it },
            averageStudyTime = if (studyTimes.isNotEmpty()) studyTimes.average().toLong() else 0L
        )

        _state.value = _state.value.copy(
            phase = GamePhase.COMPLETED
        )

        return result
    }
}
