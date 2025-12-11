package com.mindforge.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // Repository dependencies will be injected here when available
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userLevel = 1,
            currentXP = 0,
            xpForNextLevel = 1000,
            currentStreak = 0,
            dailyGoalSessions = 5,
            sessionsToday = 0
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            // TODO: Load user data from repository
            // For now, using mock data
            _uiState.value = HomeUiState(
                userLevel = 1,
                currentXP = 250,
                xpForNextLevel = 1000,
                currentStreak = 3,
                dailyGoalSessions = 5,
                sessionsToday = 2
            )
        }
    }

    fun refreshUserData() {
        loadUserData()
    }
}

/**
 * UI state for the Home screen
 */
data class HomeUiState(
    val userLevel: Int = 1,
    val currentXP: Int = 0,
    val xpForNextLevel: Int = 1000,
    val currentStreak: Int = 0,
    val dailyGoalSessions: Int = 5,
    val sessionsToday: Int = 0
) {
    val xpProgress: Float
        get() = if (xpForNextLevel > 0) {
            currentXP.toFloat() / xpForNextLevel.toFloat()
        } else 0f

    val dailyGoalProgress: Float
        get() = if (dailyGoalSessions > 0) {
            (sessionsToday.toFloat() / dailyGoalSessions.toFloat()).coerceAtMost(1f)
        } else 0f
}
