package com.hiittimer.app.data

/**
 * Represents the current state of the timer
 */
enum class TimerState {
    IDLE,       // Timer is not running and at initial state
    RUNNING,    // Timer is actively counting down
    PAUSED,     // Timer is paused but can be resumed
    FINISHED    // Timer has completed all rounds
}

/**
 * Represents the current interval type
 */
enum class IntervalType {
    WORK,
    REST
}

/**
 * Data class representing the timer configuration
 */
data class TimerConfig(
    val workTimeSeconds: Int = 20,
    val restTimeSeconds: Int = 10,
    val totalRounds: Int = 5,
    val isUnlimited: Boolean = false
) {
    init {
        require(workTimeSeconds in 5..900) { "Work time must be between 5 and 900 seconds" }
        require(restTimeSeconds in 5..300) { "Rest time must be between 5 and 300 seconds" }
        require(totalRounds in 1..99 || isUnlimited) { "Total rounds must be between 1 and 99 or unlimited" }
    }
}

/**
 * Data class representing the current timer status
 */
data class TimerStatus(
    val state: TimerState = TimerState.IDLE,
    val currentInterval: IntervalType = IntervalType.WORK,
    val timeRemainingSeconds: Int = 0,
    val currentRound: Int = 1,
    val config: TimerConfig = TimerConfig()
) {
    val isWorkInterval: Boolean get() = currentInterval == IntervalType.WORK
    val isRestInterval: Boolean get() = currentInterval == IntervalType.REST
    val isRunning: Boolean get() = state == TimerState.RUNNING
    val isPaused: Boolean get() = state == TimerState.PAUSED
    val isFinished: Boolean get() = state == TimerState.FINISHED
    val canStart: Boolean get() = state == TimerState.IDLE
    val canPause: Boolean get() = state == TimerState.RUNNING
    val canResume: Boolean get() = state == TimerState.PAUSED
    val canReset: Boolean get() = state != TimerState.IDLE
    
    /**
     * Format time remaining as MM:SS
     */
    fun formatTimeRemaining(): String {
        val minutes = timeRemainingSeconds / 60
        val seconds = timeRemainingSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Get round progress text
     */
    fun getRoundProgressText(): String {
        return if (config.isUnlimited) {
            "Round $currentRound"
        } else {
            "Round $currentRound of ${config.totalRounds}"
        }
    }

    /**
     * Get next interval preview text (FR-005: Next interval preview)
     */
    fun getNextIntervalPreview(): String? {
        if (state != TimerState.RUNNING || timeRemainingSeconds > 5) return null

        return when (currentInterval) {
            IntervalType.WORK -> "Next: REST (${config.restTimeSeconds}s)"
            IntervalType.REST -> {
                val nextRound = currentRound + 1
                if (config.isUnlimited || nextRound <= config.totalRounds) {
                    "Next: WORK (${config.workTimeSeconds}s)"
                } else {
                    "Next: FINISHED"
                }
            }
        }
    }

    /**
     * Check if next interval preview should be shown
     */
    val shouldShowNextIntervalPreview: Boolean
        get() = state == TimerState.RUNNING && timeRemainingSeconds <= 5
}
