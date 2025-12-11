package com.mindforge.core.domain.usecase

import com.mindforge.core.domain.repository.GameSessionRepository
import javax.inject.Inject

/**
 * Use case to get today's session count
 */
class GetTodaySessionCountUseCase @Inject constructor(
    private val gameSessionRepository: GameSessionRepository
) {
    suspend operator fun invoke(userId: String): Int {
        return gameSessionRepository.getTodaySessionCount(userId)
    }
}
