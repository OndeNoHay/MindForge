package com.mindforge.game.nameface.model

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GameEvent
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.engine.GameResult
import com.mindforge.game.core.engine.GameState

/**
 * Represents a person card with their information
 */
data class PersonCard(
    val id: String,
    val firstName: String,
    val lastName: String,
    val role: String? = null,
    val company: String? = null,
    val department: String? = null,
    val avatarColor: Long  // Color for visual avatar
) {
    val fullName: String
        get() = "$firstName $lastName"
}

/**
 * Question type for the test phase
 */
enum class QuestionType {
    NAME,           // Given face, select name
    ROLE,           // Given face, select role
    FACE_FROM_NAME, // Given name, select face
    COMPLETE        // Given face, select name + role
}

/**
 * A test question with options
 */
data class TestQuestion(
    val id: String,
    val type: QuestionType,
    val correctPerson: PersonCard,
    val options: List<PersonCard>,  // Includes correct answer
    val questionText: String
)

/**
 * Game phase specific to Name-Face game
 */
enum class NameFacePhase {
    STUDY,    // Memorization phase
    TEST,     // Testing phase
    REVIEW    // Review results
}

/**
 * State of the Name-Face game
 */
data class NameFaceState(
    override val phase: GamePhase = GamePhase.READY,
    override val score: Int = 0,
    override val timeRemaining: Long? = null,
    override val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    // Name-Face specific state
    val nameFacePhase: NameFacePhase = NameFacePhase.STUDY,
    val currentRound: Int = 0,
    val totalRounds: Int = 3,
    val peopleToLearn: List<PersonCard> = emptyList(),
    val currentStudyIndex: Int = 0,
    val studyTimePerPerson: Long = 5000,  // milliseconds

    // Test phase
    val questions: List<TestQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answeredQuestions: Map<String, Boolean> = emptyMap(),  // questionId -> isCorrect
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,

    // Timing
    val studyStartTime: Long = 0,
    val testStartTime: Long = 0
) : GameState {
    val accuracy: Float
        get() {
            val total = correctAnswers + incorrectAnswers
            return if (total > 0) correctAnswers.toFloat() / total.toFloat() else 0f
        }

    val isStudyComplete: Boolean
        get() = nameFacePhase == NameFacePhase.STUDY && currentStudyIndex >= peopleToLearn.size

    val currentQuestion: TestQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val allQuestionsAnswered: Boolean
        get() = answeredQuestions.size == questions.size
}

/**
 * Events in the Name-Face game
 */
sealed class NameFaceEvent : GameEvent {
    object StartGame : NameFaceEvent()
    object StartRound : NameFaceEvent()
    object NextPerson : NameFaceEvent()
    object StartTest : NameFaceEvent()
    data class AnswerQuestion(val questionId: String, val selectedPersonId: String) : NameFaceEvent()
    object NextQuestion : NameFaceEvent()
    object CompleteTest : NameFaceEvent()
    object NextRound : NameFaceEvent()
    object PauseGame : NameFaceEvent()
    object ResumeGame : NameFaceEvent()
    object FinishGame : NameFaceEvent()
}

/**
 * Result of a Name-Face game session
 */
data class NameFaceResult(
    override val score: Int,
    override val accuracy: Float,
    override val timeTaken: Long,
    override val difficulty: DifficultyLevel,
    override val xpEarned: Int,
    override val passed: Boolean,

    // Name-Face specific results
    val totalRounds: Int,
    val totalPeople: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val perfectRounds: Int,
    val averageStudyTime: Long
) : GameResult

/**
 * Parameters for different difficulty levels
 */
data class NameFaceParameters(
    val peopleCount: Int,
    val roundsCount: Int,
    val studyTimePerPerson: Long,  // milliseconds
    val includeRole: Boolean,
    val includeCompany: Boolean,
    val includeDepartment: Boolean,
    val questionsPerRound: Int
) {
    companion object {
        fun forDifficulty(level: DifficultyLevel): NameFaceParameters {
            return when (level) {
                DifficultyLevel.BEGINNER -> NameFaceParameters(
                    peopleCount = 3,
                    roundsCount = 2,
                    studyTimePerPerson = 8000,
                    includeRole = false,
                    includeCompany = false,
                    includeDepartment = false,
                    questionsPerRound = 3
                )
                DifficultyLevel.EASY -> NameFaceParameters(
                    peopleCount = 4,
                    roundsCount = 3,
                    studyTimePerPerson = 7000,
                    includeRole = true,
                    includeCompany = false,
                    includeDepartment = false,
                    questionsPerRound = 4
                )
                DifficultyLevel.MEDIUM -> NameFaceParameters(
                    peopleCount = 5,
                    roundsCount = 3,
                    studyTimePerPerson = 6000,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = false,
                    questionsPerRound = 5
                )
                DifficultyLevel.HARD -> NameFaceParameters(
                    peopleCount = 6,
                    roundsCount = 4,
                    studyTimePerPerson = 5000,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = true,
                    questionsPerRound = 6
                )
                DifficultyLevel.EXPERT -> NameFaceParameters(
                    peopleCount = 7,
                    roundsCount = 4,
                    studyTimePerPerson = 4500,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = true,
                    questionsPerRound = 7
                )
                DifficultyLevel.MASTER -> NameFaceParameters(
                    peopleCount = 8,
                    roundsCount = 5,
                    studyTimePerPerson = 4000,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = true,
                    questionsPerRound = 8
                )
                DifficultyLevel.GRANDMASTER -> NameFaceParameters(
                    peopleCount = 9,
                    roundsCount = 5,
                    studyTimePerPerson = 3500,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = true,
                    questionsPerRound = 9
                )
                DifficultyLevel.LEGENDARY -> NameFaceParameters(
                    peopleCount = 10,
                    roundsCount = 6,
                    studyTimePerPerson = 3000,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = true,
                    questionsPerRound = 10
                )
                DifficultyLevel.MYTHIC -> NameFaceParameters(
                    peopleCount = 12,
                    roundsCount = 6,
                    studyTimePerPerson = 2500,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = true,
                    questionsPerRound = 12
                )
                DifficultyLevel.DIVINE -> NameFaceParameters(
                    peopleCount = 15,
                    roundsCount = 7,
                    studyTimePerPerson = 2000,
                    includeRole = true,
                    includeCompany = true,
                    includeDepartment = true,
                    questionsPerRound = 15
                )
            }
        }
    }
}
