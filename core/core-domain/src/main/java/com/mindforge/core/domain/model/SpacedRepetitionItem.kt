package com.mindforge.core.domain.model

import java.time.LocalDate

/**
 * Domain model for spaced repetition items
 */
data class SpacedRepetitionItem(
    val id: String,
    val content: String,
    val answer: String,
    val sourceGame: GameType,
    val easinessFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val nextReviewDate: LocalDate,
    val createdAt: Long
)
