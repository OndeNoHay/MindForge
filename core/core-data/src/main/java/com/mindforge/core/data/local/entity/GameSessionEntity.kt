package com.mindforge.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindforge.core.domain.model.DifficultyLevel

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val gameType: String,
    val startTime: Long,
    val endTime: Long,
    val score: Int,
    val xpEarned: Int,
    val accuracy: Float,
    val difficulty: String,
    val passed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
