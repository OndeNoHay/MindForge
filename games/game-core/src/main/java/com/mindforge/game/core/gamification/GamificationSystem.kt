package com.mindforge.game.core.gamification

import com.mindforge.core.common.Constants
import com.mindforge.core.domain.model.UserProgress
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor

/**
 * Manages gamification elements: XP, levels, streaks, and daily goals
 */
class GamificationSystem {

    companion object {
        const val XP_PER_LEVEL = Constants.XP_PER_LEVEL // 1000
        const val STREAK_BONUS_MULTIPLIER = Constants.STREAK_BONUS_MULTIPLIER // 1.5f
    }

    /**
     * Calculate level from total XP
     *
     * @param totalXP Total XP earned
     * @return Current level
     */
    fun calculateLevel(totalXP: Int): Int {
        if (totalXP < 0) return 1
        return floor(totalXP.toDouble() / XP_PER_LEVEL).toInt() + 1
    }

    /**
     * Calculate XP needed for next level
     *
     * @param currentLevel Current level
     * @return XP needed for next level
     */
    fun calculateXPForNextLevel(currentLevel: Int): Int {
        return currentLevel * XP_PER_LEVEL
    }

    /**
     * Calculate progress to next level as a percentage
     *
     * @param totalXP Total XP
     * @param currentLevel Current level
     * @return Progress percentage (0.0 to 1.0)
     */
    fun calculateLevelProgress(totalXP: Int, currentLevel: Int): Float {
        val xpInCurrentLevel = totalXP % XP_PER_LEVEL
        return xpInCurrentLevel.toFloat() / XP_PER_LEVEL.toFloat()
    }

    /**
     * Update streak based on last active date
     *
     * @param lastActiveDate Last active date (timestamp in millis)
     * @param currentStreak Current streak count
     * @return Updated streak count
     */
    fun updateStreak(lastActiveDate: Long, currentStreak: Int): Int {
        val today = LocalDate.now()
        val lastActive = LocalDate.ofEpochDay(lastActiveDate / (24 * 60 * 60 * 1000))

        val daysDifference = ChronoUnit.DAYS.between(lastActive, today)

        return when {
            daysDifference == 0L -> currentStreak // Same day, no change
            daysDifference == 1L -> currentStreak + 1 // Next day, increment streak
            else -> 0 // Streak broken
        }
    }

    /**
     * Calculate streak bonus multiplier
     *
     * @param streak Current streak
     * @return Bonus multiplier
     */
    fun calculateStreakBonus(streak: Int): Float {
        if (streak <= 0) return 1.0f
        // Each day adds 10% bonus, capped at 2x
        val bonus = 1.0f + (streak * 0.1f)
        return bonus.coerceAtMost(2.0f)
    }

    /**
     * Calculate daily goal progress
     *
     * @param minutesPlayedToday Minutes played today
     * @param dailyGoal Daily goal in minutes
     * @return Progress percentage (0.0 to 1.0)
     */
    fun calculateDailyGoalProgress(minutesPlayedToday: Int, dailyGoal: Int): Float {
        if (dailyGoal <= 0) return 0f
        return (minutesPlayedToday.toFloat() / dailyGoal.toFloat()).coerceAtMost(1.0f)
    }

    /**
     * Calculate total XP with streak bonus applied
     *
     * @param baseXP Base XP earned
     * @param streak Current streak
     * @return Total XP with bonus
     */
    fun calculateXPWithStreakBonus(baseXP: Int, streak: Int): Int {
        val multiplier = calculateStreakBonus(streak)
        return (baseXP * multiplier).toInt()
    }

    /**
     * Check if achievement criteria is met
     *
     * @param progress Current user progress
     * @param achievementType Type of achievement
     * @return True if achievement should be unlocked
     */
    fun checkAchievementUnlock(
        progress: UserProgress,
        achievementType: AchievementType
    ): Boolean {
        return when (achievementType) {
            is AchievementType.LevelReached -> progress.level >= achievementType.level
            is AchievementType.StreakReached -> progress.currentStreak >= achievementType.days
            is AchievementType.XPReached -> progress.totalXP >= achievementType.xp
            is AchievementType.GamesCompleted -> false // Need game count from repository
        }
    }
}

/**
 * Types of achievements
 */
sealed class AchievementType {
    data class LevelReached(val level: Int) : AchievementType()
    data class StreakReached(val days: Int) : AchievementType()
    data class XPReached(val xp: Int) : AchievementType()
    data class GamesCompleted(val count: Int) : AchievementType()
}

/**
 * Predefined achievements
 */
object PredefinedAchievements {
    val FIRST_STEPS = Achievement(
        id = "first_steps",
        title = "First Steps",
        description = "Complete your first game",
        type = AchievementType.GamesCompleted(1),
        iconRes = "ic_achievement_first_steps"
    )

    val LEVEL_5 = Achievement(
        id = "level_5",
        title = "Rising Star",
        description = "Reach level 5",
        type = AchievementType.LevelReached(5),
        iconRes = "ic_achievement_level_5"
    )

    val LEVEL_10 = Achievement(
        id = "level_10",
        title = "Expert Mind",
        description = "Reach level 10",
        type = AchievementType.LevelReached(10),
        iconRes = "ic_achievement_level_10"
    )

    val STREAK_7 = Achievement(
        id = "streak_7",
        title = "Week Warrior",
        description = "Maintain a 7-day streak",
        type = AchievementType.StreakReached(7),
        iconRes = "ic_achievement_streak_7"
    )

    val STREAK_30 = Achievement(
        id = "streak_30",
        title = "Dedication Master",
        description = "Maintain a 30-day streak",
        type = AchievementType.StreakReached(30),
        iconRes = "ic_achievement_streak_30"
    )

    val XP_10000 = Achievement(
        id = "xp_10000",
        title = "Power Learner",
        description = "Earn 10,000 XP",
        type = AchievementType.XPReached(10000),
        iconRes = "ic_achievement_xp_10k"
    )

    fun getAllAchievements(): List<Achievement> {
        return listOf(
            FIRST_STEPS,
            LEVEL_5,
            LEVEL_10,
            STREAK_7,
            STREAK_30,
            XP_10000
        )
    }
}

/**
 * Data class for an achievement
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val iconRes: String
)
