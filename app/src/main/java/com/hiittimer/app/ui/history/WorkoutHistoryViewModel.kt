package com.hiittimer.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiittimer.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for workout history screen (FR-011, FR-012)
 */
data class WorkoutHistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val filteredSessions: List<WorkoutSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: WorkoutHistoryFilter = WorkoutHistoryFilter(),
    val analytics: WorkoutAnalytics? = null,
    val isAnalyticsLoading: Boolean = false
)

/**
 * Data class for workout analytics (FR-012: Progress Analytics)
 */
data class WorkoutAnalytics(
    val weeklyCompletionRate: Float,
    val monthlyCompletionRate: Float,
    val totalWorkoutsCompleted: Int,
    val totalWorkoutsAttempted: Int,
    val averageWorkoutDuration: Long,
    val longestSession: WorkoutSession?,
    val mostRoundsSession: WorkoutSession?,
    val consistencyMetrics: ConsistencyMetrics
) {
    val overallCompletionRate: Float
        get() = if (totalWorkoutsAttempted > 0) {
            (totalWorkoutsCompleted.toFloat() / totalWorkoutsAttempted.toFloat()) * 100f
        } else 0f
        
    fun getFormattedAverageDuration(): String {
        val totalSeconds = averageWorkoutDuration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }
}

/**
 * ViewModel for workout history and analytics (FR-011, FR-012)
 */
class WorkoutHistoryViewModel(
    private val workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutHistoryUiState())
    val uiState: StateFlow<WorkoutHistoryUiState> = _uiState.asStateFlow()

    init {
        loadWorkoutHistory()
        loadAnalytics()
    }

    /**
     * Load all workout sessions (FR-011: History Management)
     */
    private fun loadWorkoutHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val sessions = workoutHistoryRepository.getAllSessions()
                _uiState.value = _uiState.value.copy(
                    sessions = sessions,
                    filteredSessions = sessions,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Log the error and provide fallback empty state
                _uiState.value = _uiState.value.copy(
                    sessions = emptyList(),
                    filteredSessions = emptyList(),
                    isLoading = false,
                    error = "Failed to load workout history. Please try again."
                )
            }
        }
    }

    /**
     * Apply filters to workout history (FR-011: Filter by date range, preset, completion)
     */
    fun applyFilter(filter: WorkoutHistoryFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, filter = filter)
            try {
                val filteredSessions = workoutHistoryRepository.getFilteredSessions(filter)
                _uiState.value = _uiState.value.copy(
                    filteredSessions = filteredSessions,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to filter sessions: ${e.message}"
                )
            }
        }
    }

    /**
     * Search workout sessions (FR-011: Search functionality)
     */
    fun searchSessions(query: String) {
        val updatedFilter = _uiState.value.filter.copy(searchQuery = query)
        applyFilter(updatedFilter)
    }

    /**
     * Delete a workout session
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            try {
                workoutHistoryRepository.deleteSession(sessionId)
                // Reload data after deletion
                loadWorkoutHistory()
                loadAnalytics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete session: ${e.message}"
                )
            }
        }
    }

    /**
     * Load workout analytics (FR-012: Progress Analytics)
     */
    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyticsLoading = true)
            try {
                val analytics = WorkoutAnalytics(
                    weeklyCompletionRate = workoutHistoryRepository.getWeeklyCompletionRate(),
                    monthlyCompletionRate = workoutHistoryRepository.getMonthlyCompletionRate(),
                    totalWorkoutsCompleted = workoutHistoryRepository.getTotalWorkoutsCompleted(),
                    totalWorkoutsAttempted = workoutHistoryRepository.getTotalWorkoutsAttempted(),
                    averageWorkoutDuration = workoutHistoryRepository.getAverageWorkoutDuration(),
                    longestSession = workoutHistoryRepository.getLongestSession(),
                    mostRoundsSession = workoutHistoryRepository.getMostRoundsSession(),
                    consistencyMetrics = workoutHistoryRepository.getConsistencyMetrics()
                )

                _uiState.value = _uiState.value.copy(
                    analytics = analytics,
                    isAnalyticsLoading = false
                )
            } catch (e: Exception) {
                // Provide fallback analytics with safe defaults
                _uiState.value = _uiState.value.copy(
                    analytics = null,
                    isAnalyticsLoading = false,
                    error = "Analytics temporarily unavailable"
                )
            }
        }
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadWorkoutHistory()
        loadAnalytics()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Export workout data as CSV (FR-012: Export functionality)
     */
    fun exportWorkoutData(): String {
        val sessions = _uiState.value.sessions
        if (sessions.isEmpty()) return ""

        val header = "Date,Preset Name,Exercise Name,Work Time (s),Rest Time (s),Planned Rounds,Completed Rounds,Completion %,Duration (ms),Completed\n"
        val rows = sessions.joinToString("\n") { session ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(session.startTime))
            "${date},${session.presetName},${session.exerciseName ?: ""},${session.workTimeSeconds},${session.restTimeSeconds},${session.plannedRounds},${session.completedRounds},${session.completionPercentage},${session.totalDurationMs},${session.isCompleted}"
        }
        
        return header + rows
    }

    /**
     * Clean up old sessions (FR-013: Automatic cleanup)
     */
    fun cleanupOldSessions(olderThanDays: Int = 365) {
        viewModelScope.launch {
            try {
                val olderThanMs = olderThanDays * 24 * 60 * 60 * 1000L
                workoutHistoryRepository.deleteOldSessions(olderThanMs)
                loadWorkoutHistory()
                loadAnalytics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to cleanup old sessions: ${e.message}"
                )
            }
        }
    }
}
