package com.hiittimer.app.data

import java.io.Serializable

/**
 * Represents the current state of the timer
 */
enum class TimerState {
    STOPPED,    // Timer is not running and at initial config state
    BEGIN,      // Timer is in countdown before starting first round
    RUNNING,    // Timer is actively counting down
    PAUSED,     // Timer is paused but can be resumed from current position
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
    val isUnlimited: Boolean = false,
    val noRest: Boolean = false, // FR-001: "No Rest" toggle to disable rest periods
    val countdownDurationSeconds: Int = 5 // Duration of BEGIN state countdown
) : Serializable {
    init {
        require(workTimeSeconds in 5..900) { "Work time must be between 5 and 900 seconds" }
        require(noRest || restTimeSeconds in 5..300) { "Rest time must be between 5 and 300 seconds when rest is enabled" }
        require(totalRounds in 1..99 || isUnlimited) { "Total rounds must be between 1 and 99 or unlimited" }
        require(countdownDurationSeconds in 3..10) { "Countdown duration must be between 3 and 10 seconds" }
    }
}

/**
 * Data class representing the current timer status
 */
data class TimerStatus(
    val state: TimerState = TimerState.STOPPED,
    val currentInterval: IntervalType = IntervalType.WORK,
    val timeRemainingSeconds: Int = 0,
    val timeRemainingMilliseconds: Int = 0, // FR-017: Millisecond precision
    val currentRound: Int = 1,
    val config: TimerConfig = TimerConfig(),
    val countdownText: String? = null, // Text to display during BEGIN countdown (e.g., "Start in 3", "GO!")
    val shouldFlashBlue: Boolean = false // Trigger for blue flash effect during countdown
) {
    companion object {
        /**
         * Create a default TimerStatus with proper initial display values (FR-042)
         * Shows the default work time instead of 00:00.0
         */
        fun createDefault(): TimerStatus {
            val defaultConfig = TimerConfig()
            return TimerStatus(
                state = TimerState.STOPPED,
                currentInterval = IntervalType.WORK,
                timeRemainingSeconds = defaultConfig.workTimeSeconds,
                timeRemainingMilliseconds = 0,
                currentRound = 1,
                config = defaultConfig,
                countdownText = null,
                shouldFlashBlue = false
            )
        }
    }
    val isWorkInterval: Boolean get() = currentInterval == IntervalType.WORK
    val isRestInterval: Boolean get() = currentInterval == IntervalType.REST
    val isRunning: Boolean get() = state == TimerState.RUNNING
    val isPaused: Boolean get() = state == TimerState.PAUSED
    val isFinished: Boolean get() = state == TimerState.FINISHED
    val isBegin: Boolean get() = state == TimerState.BEGIN
    val canStart: Boolean get() = state == TimerState.STOPPED || state == TimerState.FINISHED
    val canPause: Boolean get() = state == TimerState.RUNNING
    val canResume: Boolean get() = state == TimerState.PAUSED
    val canReset: Boolean get() = state == TimerState.BEGIN || state == TimerState.PAUSED || state == TimerState.FINISHED // Can reset from BEGIN, PAUSED or FINISHED states only
    
    /**
     * Format time remaining with conditional formatting (FR-019: Timer Display Format Enhancement)
     * - Less than 60 seconds: "SS.d" format (e.g., "45.3")
     * - 60 seconds or greater: "MM:SS.d" format (e.g., "01:45.3")
     */
    fun formatTimeRemaining(): String {
        val minutes = timeRemainingSeconds / 60
        val seconds = timeRemainingSeconds % 60
        val deciseconds = timeRemainingMilliseconds / 100 // Display deciseconds (0-9)

        return if (timeRemainingSeconds < 60) {
            // FR-019: Show only seconds and deciseconds when less than 60 seconds
            String.format("%02d.%01d", timeRemainingSeconds, deciseconds)
        } else {
            // FR-017: Show full MM:SS.d format when 60 seconds or greater
            String.format("%02d:%02d.%01d", minutes, seconds, deciseconds)
        }
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
        if (state != TimerState.RUNNING) return null

        return when (currentInterval) {
            IntervalType.WORK -> {
                if (config.noRest) {
                    val nextRound = currentRound + 1
                    if (config.isUnlimited || nextRound <= config.totalRounds) {
                        "Next: WORK (${config.workTimeSeconds}s)"
                    } else {
                        "Next: FINISHED"
                    }
                } else {
                    "Next: REST (${config.restTimeSeconds}s)"
                }
            }
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
        get() = state == TimerState.RUNNING

    /**
     * Get countdown display text for BEGIN state
     */
    fun getCountdownDisplayText(): String? {
        return when (state) {
            TimerState.BEGIN -> {
                if (timeRemainingSeconds > 0) {
                    "Start in $timeRemainingSeconds"
                } else {
                    "GO!"
                }
            }
            else -> null
        }
    }
}
