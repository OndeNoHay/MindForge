package com.mindforge.game.core.gamification

import com.mindforge.core.domain.model.UserProgress
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GamificationSystem Tests")
class GamificationSystemTest {

    private lateinit var gamificationSystem: GamificationSystem

    @BeforeEach
    fun setup() {
        gamificationSystem = GamificationSystem()
    }

    @Test
    @DisplayName("Calculate correct level from XP")
    fun testCalculateLevel() {
        assertEquals(1, gamificationSystem.calculateLevel(0))
        assertEquals(1, gamificationSystem.calculateLevel(500))
        assertEquals(1, gamificationSystem.calculateLevel(999))
        assertEquals(2, gamificationSystem.calculateLevel(1000))
        assertEquals(2, gamificationSystem.calculateLevel(1500))
        assertEquals(3, gamificationSystem.calculateLevel(2000))
        assertEquals(11, gamificationSystem.calculateLevel(10000))
    }

    @Test
    @DisplayName("Calculate XP needed for next level")
    fun testXPForNextLevel() {
        assertEquals(1000, gamificationSystem.calculateXPForNextLevel(1))
        assertEquals(2000, gamificationSystem.calculateXPForNextLevel(2))
        assertEquals(5000, gamificationSystem.calculateXPForNextLevel(5))
    }

    @Test
    @DisplayName("Calculate level progress percentage")
    fun testLevelProgress() {
        // 500 XP in level 1 (0-999) = 50% progress
        assertEquals(0.5f, gamificationSystem.calculateLevelProgress(500, 1), 0.01f)

        // 1750 XP in level 2 (1000-1999) = 75% progress
        assertEquals(0.75f, gamificationSystem.calculateLevelProgress(1750, 2), 0.01f)

        // 0 XP = 0% progress
        assertEquals(0.0f, gamificationSystem.calculateLevelProgress(0, 1), 0.01f)
    }

    @Test
    @DisplayName("Streak bonus increases with streak")
    fun testStreakBonus() {
        val bonus0 = gamificationSystem.calculateStreakBonus(0)
        val bonus5 = gamificationSystem.calculateStreakBonus(5)
        val bonus10 = gamificationSystem.calculateStreakBonus(10)

        assertEquals(1.0f, bonus0)
        assertTrue(bonus5 > bonus0)
        assertTrue(bonus10 > bonus5)
    }

    @Test
    @DisplayName("Streak bonus capped at 2x")
    fun testStreakBonusCap() {
        val bonus = gamificationSystem.calculateStreakBonus(20)
        assertTrue(bonus <= 2.0f)
    }

    @Test
    @DisplayName("Daily goal progress calculated correctly")
    fun testDailyGoalProgress() {
        assertEquals(0.5f, gamificationSystem.calculateDailyGoalProgress(15, 30))
        assertEquals(1.0f, gamificationSystem.calculateDailyGoalProgress(30, 30))
        assertEquals(1.0f, gamificationSystem.calculateDailyGoalProgress(45, 30)) // Capped at 100%
    }

    @Test
    @DisplayName("Zero daily goal returns zero progress")
    fun testZeroDailyGoal() {
        assertEquals(0.0f, gamificationSystem.calculateDailyGoalProgress(15, 0))
    }

    @Test
    @DisplayName("XP with streak bonus calculated correctly")
    fun testXPWithStreakBonus() {
        val baseXP = 100
        val xpWith0Streak = gamificationSystem.calculateXPWithStreakBonus(baseXP, 0)
        val xpWith5Streak = gamificationSystem.calculateXPWithStreakBonus(baseXP, 5)

        assertEquals(100, xpWith0Streak)
        assertTrue(xpWith5Streak > xpWith0Streak)
    }

    @Test
    @DisplayName("Check level achievement unlock")
    fun testLevelAchievementUnlock() {
        val progress = UserProgress(
            totalXP = 5000,
            level = 6,
            currentStreak = 3,
            longestStreak = 10,
            achievements = emptyList(),
            dailyGoalProgress = 0.5f
        )

        assertTrue(
            gamificationSystem.checkAchievementUnlock(
                progress,
                AchievementType.LevelReached(5)
            )
        )

        assertFalse(
            gamificationSystem.checkAchievementUnlock(
                progress,
                AchievementType.LevelReached(10)
            )
        )
    }

    @Test
    @DisplayName("Check streak achievement unlock")
    fun testStreakAchievementUnlock() {
        val progress = UserProgress(
            totalXP = 2000,
            level = 3,
            currentStreak = 7,
            longestStreak = 10,
            achievements = emptyList(),
            dailyGoalProgress = 0.8f
        )

        assertTrue(
            gamificationSystem.checkAchievementUnlock(
                progress,
                AchievementType.StreakReached(7)
            )
        )

        assertFalse(
            gamificationSystem.checkAchievementUnlock(
                progress,
                AchievementType.StreakReached(10)
            )
        )
    }

    @Test
    @DisplayName("Check XP achievement unlock")
    fun testXPAchievementUnlock() {
        val progress = UserProgress(
            totalXP = 10000,
            level = 11,
            currentStreak = 5,
            longestStreak = 15,
            achievements = emptyList(),
            dailyGoalProgress = 1.0f
        )

        assertTrue(
            gamificationSystem.checkAchievementUnlock(
                progress,
                AchievementType.XPReached(10000)
            )
        )

        assertTrue(
            gamificationSystem.checkAchievementUnlock(
                progress,
                AchievementType.XPReached(5000)
            )
        )

        assertFalse(
            gamificationSystem.checkAchievementUnlock(
                progress,
                AchievementType.XPReached(20000)
            )
        )
    }

    @Test
    @DisplayName("Predefined achievements are accessible")
    fun testPredefinedAchievements() {
        val allAchievements = PredefinedAchievements.getAllAchievements()

        assertTrue(allAchievements.isNotEmpty())
        assertTrue(allAchievements.any { it.id == "first_steps" })
        assertTrue(allAchievements.any { it.id == "level_5" })
        assertTrue(allAchievements.any { it.id == "streak_7" })
    }

    @Test
    @DisplayName("Negative XP should return level 1")
    fun testNegativeXP() {
        assertEquals(1, gamificationSystem.calculateLevel(-100))
    }
}
