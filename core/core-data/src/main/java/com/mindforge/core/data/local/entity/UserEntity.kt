package com.mindforge.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val totalXP: Int,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long,
    val dailyGoal: Int,
    val preferredDifficulty: String
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        totalXP = totalXP,
        level = level,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastActiveDate = lastActiveDate,
        dailyGoal = dailyGoal,
        preferredDifficulty = DifficultyLevel.valueOf(preferredDifficulty)
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        totalXP = totalXP,
        level = level,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastActiveDate = lastActiveDate,
        dailyGoal = dailyGoal,
        preferredDifficulty = preferredDifficulty.name
    )
}
