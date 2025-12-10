package com.mindforge.game.core.scoring

/**
 * Represents different types of score modifiers
 */
sealed class ScoreModifier {
    /**
     * Base score for a correct answer
     */
    data class BaseScore(val points: Int) : ScoreModifier()

    /**
     * Time bonus - faster responses get more points
     */
    data class TimeBonus(val multiplier: Float) : ScoreModifier()

    /**
     * Streak bonus - consecutive correct answers
     */
    data class StreakBonus(val multiplier: Float) : ScoreModifier()

    /**
     * Difficulty bonus - harder levels give more points
     */
    data class DifficultyBonus(val multiplier: Float) : ScoreModifier()

    /**
     * Accuracy bonus - high accuracy gives extra points
     */
    data class AccuracyBonus(val multiplier: Float) : ScoreModifier()

    /**
     * Penalty for incorrect answers
     */
    data class Penalty(val points: Int) : ScoreModifier()
}
