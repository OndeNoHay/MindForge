package com.mindforge.core.domain.repository

import com.mindforge.core.domain.model.User
import com.mindforge.core.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User-related operations
 */
interface UserRepository {
    /**
     * Get the current user as a Flow
     */
    fun getUser(): Flow<User?>

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): User?

    /**
     * Create or update user
     */
    suspend fun saveUser(user: User)

    /**
     * Update user's XP
     */
    suspend fun updateXP(userId: String, xpToAdd: Int)

    /**
     * Update user's streak
     */
    suspend fun updateStreak(userId: String, newStreak: Int)

    /**
     * Get user's progress
     */
    fun getUserProgress(userId: String): Flow<UserProgress>
}
