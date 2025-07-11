package com.hiittimer.app.data

import com.hiittimer.app.utils.Constants
import java.util.*

/**
 * Data class representing a completed workout session (FR-010: Workout Session Tracking)
 */
data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val date: Date = Date(),
    val workoutName: String = "",
    val presetId: String? = null, // Reference to preset used (if any)
    val presetName: String, // Snapshot of preset name at time of workout
    val exerciseName: String? = null, // Exercise name from preset
    val workTimeSeconds: Int = 0, // For simple workouts
    val restTimeSeconds: Int = 0, // For simple workouts
    val plannedRounds: Int = 0, // For simple workouts
    val completedRounds: Int,
    val totalRounds: Int = 0, // Total exercises/rounds for complex workouts
    val isUnlimited: Boolean = false,
    val noRest: Boolean = false,
    val startTime: Long = System.currentTimeMillis(), // Timestamp when workout started
    val endTime: Long = System.currentTimeMillis(), // Timestamp when workout ended/stopped
    val completionPercentage: Float = 0f, // Calculated completion percentage
    val totalDurationMs: Long, // Total workout duration in milliseconds
    val actualWorkTimeMs: Long, // Actual time spent in work intervals
    val actualRestTimeMs: Long, // Actual time spent in rest intervals
    val isCompleted: Boolean = false, // True if ≥70% of planned rounds completed (FR-010)
    val completedPhases: List<String> = emptyList() // For complex workouts
) {
    init {
        require(presetName.isNotBlank() || workoutName.isNotBlank()) { "Workout name cannot be blank" }
        // For simple workouts, validate work/rest times
        if (workTimeSeconds > 0 || restTimeSeconds > 0) {
            require(workTimeSeconds > 0) { "Work time must be positive" }
            require(noRest || restTimeSeconds > 0) { "Rest time must be positive when rest is enabled" }
            require(plannedRounds > 0 || isUnlimited) { "Planned rounds must be positive or unlimited" }
        }
        require(completedRounds >= 0) { "Completed rounds cannot be negative" }
        require(startTime > 0) { "Start time must be positive" }
        require(endTime >= startTime) { "End time must be after start time" }
        require(completionPercentage in 0f..100f) { "Completion percentage must be between 0 and 100" }
        require(totalDurationMs >= 0) { "Total duration cannot be negative" }
        require(actualWorkTimeMs >= 0) { "Actual work time cannot be negative" }
        require(actualRestTimeMs >= 0) { "Actual rest time cannot be negative" }
    }

    /**
     * Get formatted duration string
     */
    fun getFormattedDuration(): String {
        val totalSeconds = totalDurationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }

    /**
     * Get completion status text
     */
    fun getCompletionStatusText(): String {
        return if (isUnlimited) {
            "$completedRounds rounds completed"
        } else {
            "$completedRounds of $plannedRounds rounds (${completionPercentage.toInt()}%)"
        }
    }

    /**
     * Get workout summary text
     */
    fun getWorkoutSummary(): String {
        val workTime = "${workTimeSeconds}s work"
        val restTime = if (noRest) "no rest" else "${restTimeSeconds}s rest"
        return "$workTime, $restTime"
    }

    /**
     * Check if this is a personal record for duration
     */
    fun isLongestSession(allSessions: List<WorkoutSession>): Boolean {
        return allSessions.none { it.totalDurationMs > this.totalDurationMs }
    }

    /**
     * Check if this is a personal record for rounds completed
     */
    fun isMostRoundsCompleted(allSessions: List<WorkoutSession>): Boolean {
        return allSessions.none { it.completedRounds > this.completedRounds }
    }

    companion object {
        /**
         * Calculate completion percentage based on rounds completed vs planned
         */
        fun calculateCompletionPercentage(completedRounds: Int, plannedRounds: Int, isUnlimited: Boolean): Float {
            return if (isUnlimited) {
                100f // Unlimited workouts are always considered 100% complete
            } else {
                (completedRounds.toFloat() / plannedRounds.toFloat() * 100f).coerceIn(0f, 100f)
            }
        }

        /**
         * Determine if workout is considered completed (≥70% of planned rounds)
         */
        fun isWorkoutCompleted(completedRounds: Int, plannedRounds: Int, isUnlimited: Boolean): Boolean {
            return if (isUnlimited) {
                completedRounds > 0 // Any rounds completed for unlimited is considered complete
            } else {
                calculateCompletionPercentage(completedRounds, plannedRounds, false) >= Constants.WORKOUT_COMPLETION_THRESHOLD_PERCENT
            }
        }

        /**
         * Create WorkoutSession from TimerConfig and session data
         */
        fun fromTimerSession(
            config: TimerConfig,
            presetId: String? = null,
            presetName: String,
            exerciseName: String? = null,
            completedRounds: Int,
            startTime: Long,
            endTime: Long,
            actualWorkTimeMs: Long,
            actualRestTimeMs: Long
        ): WorkoutSession {
            val completionPercentage = calculateCompletionPercentage(
                completedRounds, 
                config.totalRounds, 
                config.isUnlimited
            )
            val isCompleted = isWorkoutCompleted(
                completedRounds, 
                config.totalRounds, 
                config.isUnlimited
            )

            return WorkoutSession(
                date = Date(startTime),
                workoutName = presetName,
                presetId = presetId,
                presetName = presetName,
                exerciseName = exerciseName,
                workTimeSeconds = config.workTimeSeconds,
                restTimeSeconds = config.restTimeSeconds,
                plannedRounds = config.totalRounds,
                completedRounds = completedRounds,
                totalRounds = config.totalRounds,
                isUnlimited = config.isUnlimited,
                noRest = config.noRest,
                startTime = startTime,
                endTime = endTime,
                completionPercentage = completionPercentage,
                totalDurationMs = endTime - startTime,
                actualWorkTimeMs = actualWorkTimeMs,
                actualRestTimeMs = actualRestTimeMs,
                isCompleted = isCompleted
            )
        }
    }
}

/**
 * Filter options for workout history (FR-011: History Management)
 */
enum class DateFilter {
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_3_MONTHS,
    ALL_TIME
}

enum class CompletionFilter {
    ALL,
    COMPLETED_ONLY,    // ≥70% completion
    INCOMPLETE_ONLY    // <70% completion
}

/**
 * Data class for workout history filters
 */
data class WorkoutHistoryFilter(
    val dateFilter: DateFilter = DateFilter.ALL_TIME,
    val completionFilter: CompletionFilter = CompletionFilter.ALL,
    val presetNameFilter: String? = null,
    val searchQuery: String = ""
)

/**
 * Repository interface for workout history management (FR-011, FR-012)
 */
interface WorkoutHistoryRepository {
    suspend fun saveWorkoutSession(session: WorkoutSession)
    suspend fun getAllSessions(): List<WorkoutSession>
    suspend fun getSessionById(id: String): WorkoutSession?
    suspend fun getFilteredSessions(filter: WorkoutHistoryFilter): List<WorkoutSession>
    suspend fun deleteSession(id: String)
    suspend fun deleteOldSessions(olderThanMs: Long) // For automatic cleanup (FR-013)
    suspend fun getSessionCount(): Int
    suspend fun searchSessions(query: String): List<WorkoutSession>
    
    // Analytics methods (FR-012: Progress Analytics)
    suspend fun getWeeklyCompletionRate(): Float
    suspend fun getMonthlyCompletionRate(): Float
    suspend fun getTotalWorkoutsCompleted(): Int
    suspend fun getTotalWorkoutsAttempted(): Int
    suspend fun getAverageWorkoutDuration(): Long
    suspend fun getLongestSession(): WorkoutSession?
    suspend fun getMostRoundsSession(): WorkoutSession?
    suspend fun getConsistencyMetrics(): ConsistencyMetrics
}

/**
 * Data class for consistency metrics (FR-012: Progress Analytics)
 */
data class ConsistencyMetrics(
    val currentStreak: Int, // Days with at least one workout
    val longestStreak: Int,
    val averageWorkoutsPerWeek: Float,
    val totalActiveDays: Int
)

/**
 * In-memory implementation of WorkoutHistoryRepository for development/testing
 */
class InMemoryWorkoutHistoryRepository : WorkoutHistoryRepository {
    private val sessions = mutableMapOf<String, WorkoutSession>()
    private val maxSessions = 1000 // FR-013: Maximum 1000 workout sessions

    override suspend fun saveWorkoutSession(session: WorkoutSession) {
        // Remove oldest session if at capacity
        if (sessions.size >= maxSessions && !sessions.containsKey(session.id)) {
            val oldestSession = sessions.values.minByOrNull { it.startTime }
            oldestSession?.let { sessions.remove(it.id) }
        }
        sessions[session.id] = session
    }

    override suspend fun getAllSessions(): List<WorkoutSession> {
        return sessions.values.sortedByDescending { it.startTime }
    }

    override suspend fun getSessionById(id: String): WorkoutSession? {
        return sessions[id]
    }

    override suspend fun getFilteredSessions(filter: WorkoutHistoryFilter): List<WorkoutSession> {
        val now = System.currentTimeMillis()
        val cutoffTime = when (filter.dateFilter) {
            DateFilter.LAST_7_DAYS -> now - (Constants.RECENT_WORKOUTS_DAYS_7 * Constants.MILLISECONDS_PER_DAY)
            DateFilter.LAST_30_DAYS -> now - (Constants.RECENT_WORKOUTS_DAYS_30 * Constants.MILLISECONDS_PER_DAY)
            DateFilter.LAST_3_MONTHS -> now - (Constants.QUARTERLY_WORKOUTS_DAYS * Constants.MILLISECONDS_PER_DAY)
            DateFilter.ALL_TIME -> 0L
        }

        return sessions.values
            .filter { session ->
                // Date filter
                session.startTime >= cutoffTime &&
                // Completion filter
                when (filter.completionFilter) {
                    CompletionFilter.ALL -> true
                    CompletionFilter.COMPLETED_ONLY -> session.isCompleted
                    CompletionFilter.INCOMPLETE_ONLY -> !session.isCompleted
                } &&
                // Preset name filter
                (filter.presetNameFilter.isNullOrBlank() ||
                 session.presetName.contains(filter.presetNameFilter, ignoreCase = true)) &&
                // Search query filter
                (filter.searchQuery.isBlank() ||
                 session.presetName.contains(filter.searchQuery, ignoreCase = true) ||
                 session.exerciseName?.contains(filter.searchQuery, ignoreCase = true) == true)
            }
            .sortedByDescending { it.startTime }
    }

    override suspend fun deleteSession(id: String) {
        sessions.remove(id)
    }

    override suspend fun deleteOldSessions(olderThanMs: Long) {
        val cutoffTime = System.currentTimeMillis() - olderThanMs
        val oldSessions = sessions.values.filter { it.startTime < cutoffTime }
        oldSessions.forEach { sessions.remove(it.id) }
    }

    override suspend fun getSessionCount(): Int {
        return sessions.size
    }

    override suspend fun searchSessions(query: String): List<WorkoutSession> {
        val lowercaseQuery = query.lowercase()
        return sessions.values
            .filter { session ->
                session.presetName.lowercase().contains(lowercaseQuery) ||
                session.exerciseName?.lowercase()?.contains(lowercaseQuery) == true
            }
            .sortedByDescending { it.startTime }
    }

    // Analytics methods implementation
    override suspend fun getWeeklyCompletionRate(): Float {
        val weekAgo = System.currentTimeMillis() - (Constants.RECENT_WORKOUTS_DAYS_7 * Constants.MILLISECONDS_PER_DAY)
        val recentSessions = sessions.values.filter { it.startTime >= weekAgo }
        if (recentSessions.isEmpty()) return 0f

        val completedCount = recentSessions.count { it.isCompleted }
        return (completedCount.toFloat() / recentSessions.size.toFloat()) * 100f
    }

    override suspend fun getMonthlyCompletionRate(): Float {
        val monthAgo = System.currentTimeMillis() - (Constants.MONTHLY_WORKOUTS_DAYS * Constants.MILLISECONDS_PER_DAY)
        val recentSessions = sessions.values.filter { it.startTime >= monthAgo }
        if (recentSessions.isEmpty()) return 0f

        val completedCount = recentSessions.count { it.isCompleted }
        return (completedCount.toFloat() / recentSessions.size.toFloat()) * 100f
    }

    override suspend fun getTotalWorkoutsCompleted(): Int {
        return sessions.values.count { it.isCompleted }
    }

    override suspend fun getTotalWorkoutsAttempted(): Int {
        return sessions.size
    }

    override suspend fun getAverageWorkoutDuration(): Long {
        if (sessions.isEmpty()) return 0L
        return sessions.values.map { it.totalDurationMs }.average().toLong()
    }

    override suspend fun getLongestSession(): WorkoutSession? {
        return sessions.values.maxByOrNull { it.totalDurationMs }
    }

    override suspend fun getMostRoundsSession(): WorkoutSession? {
        return sessions.values.maxByOrNull { it.completedRounds }
    }

    override suspend fun getConsistencyMetrics(): ConsistencyMetrics {
        if (sessions.isEmpty()) {
            return ConsistencyMetrics(0, 0, 0f, 0)
        }

        val sortedSessions = sessions.values.sortedBy { it.startTime }
        val dayMs = Constants.MILLISECONDS_PER_DAY

        // Group sessions by day
        val sessionsByDay = sortedSessions.groupBy { session ->
            session.startTime / dayMs
        }

        val activeDays = sessionsByDay.keys.sorted()
        val totalActiveDays = activeDays.size

        // Calculate current streak
        val today = System.currentTimeMillis() / dayMs
        var currentStreak = 0
        var checkDay = today

        while (activeDays.contains(checkDay)) {
            currentStreak++
            checkDay--
        }

        // Calculate longest streak
        var longestStreak = 0
        var tempStreak = 0
        var previousDay = -1L

        for (day in activeDays) {
            if (previousDay == -1L || day == previousDay + 1) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
            previousDay = day
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        // Calculate average workouts per week
        val firstDay = activeDays.firstOrNull() ?: today
        val daysSinceFirst = maxOf(1, (today - firstDay + 1).toInt())
        val weeksSinceFirst = daysSinceFirst / 7f
        val averageWorkoutsPerWeek = if (weeksSinceFirst > 0) {
            sessions.size / weeksSinceFirst
        } else {
            0f
        }

        return ConsistencyMetrics(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            averageWorkoutsPerWeek = averageWorkoutsPerWeek,
            totalActiveDays = totalActiveDays
        )
    }
}
