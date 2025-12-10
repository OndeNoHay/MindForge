package com.mindforge.game.core.difficulty

import com.mindforge.core.domain.model.DifficultyLevel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("AdaptiveDifficultyManager Tests")
class AdaptiveDifficultyManagerTest {

    private lateinit var difficultyManager: AdaptiveDifficultyManager

    @BeforeEach
    fun setup() {
        difficultyManager = AdaptiveDifficultyManager()
    }

    @Test
    @DisplayName("High accuracy and fast response should increase difficulty")
    fun testIncreaseDifficulty() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.BEGINNER,
            accuracy = 0.9f,
            responseTime = 2000L,
            streak = 5
        )

        assertEquals(DifficultyLevel.EASY, nextLevel)
    }

    @Test
    @DisplayName("Low accuracy should decrease difficulty")
    fun testDecreaseDifficulty() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.HARD,
            accuracy = 0.5f,
            responseTime = 5000L,
            streak = 0
        )

        assertEquals(DifficultyLevel.MEDIUM, nextLevel)
    }

    @Test
    @DisplayName("Medium performance should maintain difficulty")
    fun testMaintainDifficulty() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.MEDIUM,
            accuracy = 0.7f,
            responseTime = 5000L,
            streak = 0
        )

        assertEquals(DifficultyLevel.MEDIUM, nextLevel)
    }

    @Test
    @DisplayName("Cannot increase beyond maximum difficulty")
    fun testMaxDifficultyLimit() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.DIVINE,
            accuracy = 1.0f,
            responseTime = 1000L,
            streak = 10
        )

        assertEquals(DifficultyLevel.DIVINE, nextLevel)
    }

    @Test
    @DisplayName("Cannot decrease below minimum difficulty")
    fun testMinDifficultyLimit() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.BEGINNER,
            accuracy = 0.1f,
            responseTime = 15000L,
            streak = -5
        )

        assertEquals(DifficultyLevel.BEGINNER, nextLevel)
    }

    @Test
    @DisplayName("Increase difficulty by one level")
    fun testIncreaseDifficultyOneLevel() {
        val increased = difficultyManager.increaseDifficulty(DifficultyLevel.MEDIUM)
        assertEquals(DifficultyLevel.HARD, increased)
    }

    @Test
    @DisplayName("Decrease difficulty by one level")
    fun testDecreaseDifficultyOneLevel() {
        val decreased = difficultyManager.decreaseDifficulty(DifficultyLevel.MEDIUM)
        assertEquals(DifficultyLevel.EASY, decreased)
    }

    @Test
    @DisplayName("Difficulty parameters should scale with level")
    fun testDifficultyParameters() {
        val beginnerParams = difficultyManager.getDifficultyParameters(DifficultyLevel.BEGINNER)
        val expertParams = difficultyManager.getDifficultyParameters(DifficultyLevel.EXPERT)

        assertTrue(expertParams.itemCount > beginnerParams.itemCount)
        assertTrue(expertParams.timeLimit < beginnerParams.timeLimit)
        assertTrue(expertParams.complexity > beginnerParams.complexity)
    }

    @Test
    @DisplayName("Very slow response should trigger difficulty decrease")
    fun testVerySlowResponse() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.MEDIUM,
            accuracy = 0.7f,
            responseTime = 12000L, // Very slow
            streak = 0
        )

        assertEquals(DifficultyLevel.EASY, nextLevel)
    }

    @Test
    @DisplayName("Good streak should help increase difficulty")
    fun testGoodStreakInfluence() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.BEGINNER,
            accuracy = 0.85f,
            responseTime = 5000L,
            streak = 6 // Good streak
        )

        assertEquals(DifficultyLevel.EASY, nextLevel)
    }

    @Test
    @DisplayName("Poor streak should trigger difficulty decrease")
    fun testPoorStreakInfluence() {
        val nextLevel = difficultyManager.calculateNextDifficulty(
            currentLevel = DifficultyLevel.MEDIUM,
            accuracy = 0.7f,
            responseTime = 5000L,
            streak = -4 // Poor streak
        )

        assertEquals(DifficultyLevel.EASY, nextLevel)
    }
}
