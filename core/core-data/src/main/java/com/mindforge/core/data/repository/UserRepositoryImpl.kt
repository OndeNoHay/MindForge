package com.mindforge.core.data.repository

import com.mindforge.core.data.local.dao.UserDao
import com.mindforge.core.data.local.entity.toDomain
import com.mindforge.core.data.local.entity.toEntity
import com.mindforge.core.domain.model.User
import com.mindforge.core.domain.model.UserProgress
import com.mindforge.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return userDao.getUser().map { it?.toDomain() }
    }

    override suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toDomain()
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateXP(userId: String, xpToAdd: Int) {
        userDao.updateXP(userId, xpToAdd)
    }

    override suspend fun updateStreak(userId: String, newStreak: Int) {
        userDao.updateStreak(userId, newStreak)
    }

    override fun getUserProgress(userId: String): Flow<UserProgress> {
        return userDao.getUser().map { entity ->
            entity?.let {
                UserProgress(
                    totalXP = it.totalXP,
                    level = it.level,
                    currentStreak = it.currentStreak,
                    longestStreak = it.longestStreak,
                    achievements = emptyList(), // TODO: Load from achievements table
                    dailyGoalProgress = 0f // TODO: Calculate from today's sessions
                )
            } ?: UserProgress(
                totalXP = 0,
                level = 1,
                currentStreak = 0,
                longestStreak = 0,
                achievements = emptyList(),
                dailyGoalProgress = 0f
            )
        }
    }
}
