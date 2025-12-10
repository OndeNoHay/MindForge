package com.mindforge.core.domain.model

/**
 * Difficulty levels for games
 */
enum class DifficultyLevel(val value: Int) {
    BEGINNER(1),
    EASY(2),
    MEDIUM(3),
    HARD(4),
    EXPERT(5),
    MASTER(6),
    GRANDMASTER(7),
    LEGENDARY(8),
    MYTHIC(9),
    DIVINE(10);

    companion object {
        fun fromValue(value: Int): DifficultyLevel {
            return values().find { it.value == value } ?: BEGINNER
        }
    }
}
