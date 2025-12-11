package com.mindforge.core.domain.usecase

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.domain.model.User
import com.mindforge.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to initialize a default user if none exists
 */
class InitializeUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User {
        // Check if user already exists
        val existingUser = userRepository.getUser().first()

        return if (existingUser != null) {
            existingUser
        } else {
            // Create default user
            val defaultUser = User(
                id = UUID.randomUUID().toString(),
                name = "Player",
                totalXP = 0,
                level = 1,
                currentStreak = 0,
                longestStreak = 0,
                lastActiveDate = System.currentTimeMillis(),
                dailyGoal = 5,
                preferredDifficulty = DifficultyLevel.EASY
            )

            // Save to database
            userRepository.saveUser(defaultUser)

            defaultUser
        }
    }
}
