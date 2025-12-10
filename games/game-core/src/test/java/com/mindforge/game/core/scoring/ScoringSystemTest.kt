package com.mindforge.game.core.scoring

import com.mindforge.core.domain.model.DifficultyLevel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ScoringSystem Tests")
class ScoringSystemTest {

    private lateinit var scoringSystem: ScoringSystem

    @BeforeEach
    fun setup() {
        scoringSystem = ScoringSystem()
    }

    @Test
    @DisplayName("Correct answer with no bonuses should give base score")
    fun testBasicCorrectAnswer() {
        val score = scoringSystem.calculateAnswerScore(
            isCorrect = true,
            responseTime = 5000L,
            currentStreak = 0,
            difficulty = DifficultyLevel.BEGINNER
        )

        assertEquals(ScoringSystem.BASE_SCORE_CORRECT, score)
    }

    @Test
    @DisplayName("Incorrect answer should give negative score")
    fun testIncorrectAnswer() {
        val score = scoringSystem.calculateAnswerScore(
            isCorrect = false,
            responseTime = 5000L
        )

        assertEquals(ScoringSystem.BASE_SCORE_INCORRECT, score)
    }

    @Test
    @DisplayName("Fast response should apply time bonus")
    fun testFastResponseBonus() {
        val score = scoringSystem.calculateAnswerScore(
            isCorrect = true,
            responseTime = 1500L, // Fast response
            currentStreak = 0,
            difficulty = DifficultyLevel.BEGINNER
        )

        val expectedScore = (ScoringSystem.BASE_SCORE_CORRECT * ScoringSystem.FAST_RESPONSE_MULTIPLIER).toInt()
        assertEquals(expectedScore, score)
    }

    @Test
    @DisplayName("Medium response should apply medium time bonus")
    fun testMediumResponseBonus() {
        val score = scoringSystem.calculateAnswerScore(
            isCorrect = true,
            responseTime = 3000L, // Medium response
            currentStreak = 0,
            difficulty = DifficultyLevel.BEGINNER
        )

        val expectedScore = (ScoringSystem.BASE_SCORE_CORRECT * ScoringSystem.MEDIUM_RESPONSE_MULTIPLIER).toInt()
        assertEquals(expectedScore, score)
    }

    @Test
    @DisplayName("Streak should increase score")
    fun testStreakBonus() {
        val streakMultiplier = scoringSystem.calculateStreakBonus(5)
        assertTrue(streakMultiplier > 1.0f)
        assertTrue(streakMultiplier <= ScoringSystem.MAX_STREAK_MULTIPLIER)
    }

    @Test
    @DisplayName("Higher difficulty should increase score")
    fun testDifficultyBonus() {
        val beginnerMultiplier = scoringSystem.calculateDifficultyBonus(DifficultyLevel.BEGINNER)
        val expertMultiplier = scoringSystem.calculateDifficultyBonus(DifficultyLevel.EXPERT)

        assertTrue(expertMultiplier > beginnerMultiplier)
    }

    @Test
    @DisplayName("Calculate accuracy correctly")
    fun testAccuracyCalculation() {
        val accuracy = scoringSystem.calculateAccuracy(7, 10)
        assertEquals(0.7f, accuracy, 0.01f)
    }

    @Test
    @DisplayName("Zero total answers should give zero accuracy")
    fun testZeroAccuracy() {
        val accuracy = scoringSystem.calculateAccuracy(0, 0)
        assertEquals(0f, accuracy)
    }

    @Test
    @DisplayName("Perfect accuracy should give bonus XP")
    fun testPerfectAccuracyXP() {
        val xpPerfect = scoringSystem.calculateSessionXP(
            totalScore = 1000,
            accuracy = 1.0f,
            difficulty = DifficultyLevel.BEGINNER,
            timeTaken = 60000L
        )

        val xpNormal = scoringSystem.calculateSessionXP(
            totalScore = 1000,
            accuracy = 0.7f,
            difficulty = DifficultyLevel.BEGINNER,
            timeTaken = 60000L
        )

        assertTrue(xpPerfect > xpNormal)
    }

    @Test
    @DisplayName("Player passes with 60% or higher accuracy")
    fun testPassingAccuracy() {
        assertTrue(scoringSystem.hasPassed(0.6f))
        assertTrue(scoringSystem.hasPassed(0.8f))
        assertTrue(scoringSystem.hasPassed(1.0f))
        assertFalse(scoringSystem.hasPassed(0.5f))
    }

    @Test
    @DisplayName("Session XP should have minimum value")
    fun testMinimumSessionXP() {
        val xp = scoringSystem.calculateSessionXP(
            totalScore = 0,
            accuracy = 0.0f,
            difficulty = DifficultyLevel.BEGINNER,
            timeTaken = 60000L
        )

        assertTrue(xp >= 10)
    }

    @Test
    @DisplayName("Combined bonuses should multiply score correctly")
    fun testCombinedBonuses() {
        val score = scoringSystem.calculateAnswerScore(
            isCorrect = true,
            responseTime = 1500L, // Fast response bonus
            currentStreak = 5, // Streak bonus
            difficulty = DifficultyLevel.EXPERT // Difficulty bonus
        )

        assertTrue(score > ScoringSystem.BASE_SCORE_CORRECT)
    }
}
