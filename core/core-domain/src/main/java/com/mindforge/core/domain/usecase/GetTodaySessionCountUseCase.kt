package com.mindforge.core.domain.usecase

import com.mindforge.core.data.local.dao.GameSessionDao
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case to get today's session count
 */
class GetTodaySessionCountUseCase @Inject constructor(
    private val gameSessionDao: GameSessionDao
) {
    suspend operator fun invoke(userId: String): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        return gameSessionDao.getTodaySessionCount(userId, startOfDay)
    }
}
