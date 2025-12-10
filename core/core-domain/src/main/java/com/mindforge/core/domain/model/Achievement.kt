package com.mindforge.core.domain.model

/**
 * Domain model for Achievement
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: String,
    val unlockedAt: Long?,
    val progress: Float
) {
    val isUnlocked: Boolean
        get() = unlockedAt != null
}
