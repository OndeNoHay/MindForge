package com.mindforge.game.meetingrecall.model

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GameEvent
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.engine.GameResult
import com.mindforge.game.core.engine.GameState

/**
 * Represents a meeting summary
 */
data class Meeting(
    val id: String,
    val title: String,
    val date: String,
    val attendees: List<Attendee>,
    val topics: List<Topic>,
    val actionItems: List<ActionItem>,
    val duration: String? = null,
    val location: String? = null
)

/**
 * Meeting attendee
 */
data class Attendee(
    val name: String,
    val role: String
)

/**
 * Discussion topic
 */
data class Topic(
    val title: String,
    val keyPoints: List<String>,
    val decisions: List<String>
)

/**
 * Action item assigned during meeting
 */
data class ActionItem(
    val assignee: String,
    val task: String,
    val deadline: String,
    val priority: Priority = Priority.NORMAL
) {
    enum class Priority {
        LOW, NORMAL, HIGH, CRITICAL
    }
}

/**
 * Type of question asked about the meeting
 */
enum class QuestionType {
    ATTENDEE,           // Who attended?
    TOPIC,              // What was discussed?
    DECISION,           // What was decided?
    ACTION_ITEM,        // Who was assigned what?
    DEADLINE,           // When is the deadline?
    DETAIL              // Specific detail from meeting
}

/**
 * A test question about the meeting
 */
data class MeetingQuestion(
    val id: String,
    val type: QuestionType,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,  // The correct option
    val isDetailQuestion: Boolean = false  // Bonus points for detail questions
)

/**
 * Game phase specific to Meeting Recall
 */
enum class MeetingRecallPhase {
    READING,    // Reading/studying the meeting summary
    TEST,       // Answering questions
    REVIEW      // Review results
}

/**
 * State of the Meeting Recall game
 */
data class MeetingRecallState(
    override val phase: GamePhase = GamePhase.READY,
    override val score: Int = 0,
    override val timeRemaining: Long? = null,
    override val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    // Meeting Recall specific state
    val meetingRecallPhase: MeetingRecallPhase = MeetingRecallPhase.READING,
    val currentRound: Int = 0,
    val totalRounds: Int = 3,
    val currentMeeting: Meeting? = null,
    val readingTimePerMeeting: Long = 60000,  // milliseconds

    // Test phase
    val questions: List<MeetingQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answeredQuestions: Map<String, Boolean> = emptyMap(),  // questionId -> isCorrect
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val detailQuestionsCorrect: Int = 0,  // Bonus metric

    // Timing
    val readingStartTime: Long = 0,
    val testStartTime: Long = 0
) : GameState {
    val accuracy: Float
        get() {
            val total = correctAnswers + incorrectAnswers
            return if (total > 0) correctAnswers.toFloat() / total.toFloat() else 0f
        }

    val isReadingComplete: Boolean
        get() = meetingRecallPhase == MeetingRecallPhase.READING && timeRemaining == 0L

    val currentQuestion: MeetingQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val allQuestionsAnswered: Boolean
        get() = answeredQuestions.size == questions.size
}

/**
 * Events in the Meeting Recall game
 */
sealed class MeetingRecallEvent : GameEvent {
    object StartGame : MeetingRecallEvent()
    object StartRound : MeetingRecallEvent()
    object StartReading : MeetingRecallEvent()
    object FinishReading : MeetingRecallEvent()
    object StartTest : MeetingRecallEvent()
    data class AnswerQuestion(val questionId: String, val selectedAnswer: String) : MeetingRecallEvent()
    object NextQuestion : MeetingRecallEvent()
    object CompleteTest : MeetingRecallEvent()
    object NextRound : MeetingRecallEvent()
    object PauseGame : MeetingRecallEvent()
    object ResumeGame : MeetingRecallEvent()
    object FinishGame : MeetingRecallEvent()
}

/**
 * Result of a Meeting Recall game session
 */
data class MeetingRecallResult(
    override val score: Int,
    override val accuracy: Float,
    override val timeTaken: Long,
    override val difficulty: DifficultyLevel,
    override val xpEarned: Int,
    override val passed: Boolean,

    // Meeting Recall specific results
    val totalRounds: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val detailQuestionsCorrect: Int,
    val averageReadingTime: Long,
    val perfectRounds: Int
) : GameResult

/**
 * Parameters for different difficulty levels
 */
data class MeetingRecallParameters(
    val roundsCount: Int,
    val readingTimePerMeeting: Long,  // milliseconds
    val attendeesCount: Int,
    val topicsCount: Int,
    val actionItemsCount: Int,
    val questionsPerMeeting: Int,
    val includeDetailQuestions: Boolean,
    val includeLocation: Boolean,
    val includeDuration: Boolean
) {
    companion object {
        fun forDifficulty(level: DifficultyLevel): MeetingRecallParameters {
            return when (level) {
                DifficultyLevel.BEGINNER -> MeetingRecallParameters(
                    roundsCount = 2,
                    readingTimePerMeeting = 45000,  // 45s
                    attendeesCount = 3,
                    topicsCount = 2,
                    actionItemsCount = 2,
                    questionsPerMeeting = 4,
                    includeDetailQuestions = false,
                    includeLocation = false,
                    includeDuration = false
                )
                DifficultyLevel.EASY -> MeetingRecallParameters(
                    roundsCount = 2,
                    readingTimePerMeeting = 50000,  // 50s
                    attendeesCount = 4,
                    topicsCount = 2,
                    actionItemsCount = 3,
                    questionsPerMeeting = 5,
                    includeDetailQuestions = false,
                    includeLocation = true,
                    includeDuration = false
                )
                DifficultyLevel.MEDIUM -> MeetingRecallParameters(
                    roundsCount = 3,
                    readingTimePerMeeting = 60000,  // 60s
                    attendeesCount = 5,
                    topicsCount = 3,
                    actionItemsCount = 4,
                    questionsPerMeeting = 6,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
                DifficultyLevel.HARD -> MeetingRecallParameters(
                    roundsCount = 3,
                    readingTimePerMeeting = 70000,  // 70s
                    attendeesCount = 6,
                    topicsCount = 4,
                    actionItemsCount = 5,
                    questionsPerMeeting = 7,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
                DifficultyLevel.EXPERT -> MeetingRecallParameters(
                    roundsCount = 4,
                    readingTimePerMeeting = 75000,  // 75s
                    attendeesCount = 7,
                    topicsCount = 4,
                    actionItemsCount = 6,
                    questionsPerMeeting = 8,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
                DifficultyLevel.MASTER -> MeetingRecallParameters(
                    roundsCount = 4,
                    readingTimePerMeeting = 80000,  // 80s
                    attendeesCount = 8,
                    topicsCount = 5,
                    actionItemsCount = 7,
                    questionsPerMeeting = 9,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
                DifficultyLevel.GRANDMASTER -> MeetingRecallParameters(
                    roundsCount = 5,
                    readingTimePerMeeting = 85000,  // 85s
                    attendeesCount = 9,
                    topicsCount = 5,
                    actionItemsCount = 8,
                    questionsPerMeeting = 10,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
                DifficultyLevel.LEGENDARY -> MeetingRecallParameters(
                    roundsCount = 5,
                    readingTimePerMeeting = 90000,  // 90s
                    attendeesCount = 10,
                    topicsCount = 6,
                    actionItemsCount = 9,
                    questionsPerMeeting = 11,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
                DifficultyLevel.MYTHIC -> MeetingRecallParameters(
                    roundsCount = 6,
                    readingTimePerMeeting = 95000,  // 95s
                    attendeesCount = 11,
                    topicsCount = 6,
                    actionItemsCount = 10,
                    questionsPerMeeting = 12,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
                DifficultyLevel.DIVINE -> MeetingRecallParameters(
                    roundsCount = 7,
                    readingTimePerMeeting = 100000,  // 100s
                    attendeesCount = 12,
                    topicsCount = 7,
                    actionItemsCount = 11,
                    questionsPerMeeting = 13,
                    includeDetailQuestions = true,
                    includeLocation = true,
                    includeDuration = true
                )
            }
        }
    }
}
