package com.hiittimer.app.timer

import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.hiittimer.app.performance.PerformanceManager

/**
 * Manages the HIIT timer functionality with precise timing and audio cues
 * Now includes workout session tracking (FR-010: Workout Session Tracking)
 * Enhanced with performance optimization (TS-003, TS-004)
 */
class TimerManager(
    private val audioManager: AudioManager? = null,
    private val workoutHistoryRepository: WorkoutHistoryRepository? = null,
    private val performanceManager: PerformanceManager? = null
) {
    private val _timerStatus = MutableStateFlow(TimerStatus())
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Session tracking variables (FR-010: Workout Session Tracking)
    private var sessionStartTime: Long = 0
    private var currentPresetId: String? = null
    private var currentPresetName: String = "Custom Workout"
    private var currentExerciseName: String? = null
    private var actualWorkTimeMs: Long = 0
    private var actualRestTimeMs: Long = 0
    private var lastIntervalStartTime: Long = 0
    
    /**
     * Start the timer with the given configuration
     * Now includes session tracking initialization (FR-010)
     */
    fun start(config: TimerConfig, presetId: String? = null, presetName: String = "Custom Workout", exerciseName: String? = null) {
        try {
            if (_timerStatus.value.state != TimerState.IDLE) return

            // Initialize session tracking (FR-010: Workout Session Tracking)
            sessionStartTime = System.currentTimeMillis()
            currentPresetId = presetId
            currentPresetName = presetName
            currentExerciseName = exerciseName
            actualWorkTimeMs = 0
            actualRestTimeMs = 0
            lastIntervalStartTime = sessionStartTime

            _timerStatus.value = TimerStatus(
                state = TimerState.RUNNING,
                currentInterval = IntervalType.WORK,
                timeRemainingSeconds = config.workTimeSeconds,
                timeRemainingMilliseconds = 0, // Start at 0 milliseconds
                currentRound = 1,
                config = config
            )

            // Play work interval start sound (FR-006: Audio cues)
            try {
                audioManager?.playWorkIntervalSound()
            } catch (e: Exception) {
                // Continue without audio if there's an audio issue
            }

            startCountdown()
        } catch (e: Exception) {
            // Reset to idle state if start fails
            _timerStatus.value = TimerStatus()
        }
    }
    
    /**
     * Pause the timer
     */
    fun pause() {
        if (_timerStatus.value.state != TimerState.RUNNING) return

        timerJob?.cancel()
        _timerStatus.value = _timerStatus.value.copy(state = TimerState.PAUSED)
    }
    
    /**
     * Resume the timer from paused state
     */
    fun resume() {
        if (_timerStatus.value.state != TimerState.PAUSED) return

        _timerStatus.value = _timerStatus.value.copy(state = TimerState.RUNNING)
        startCountdown()
    }
    
    /**
     * Reset the timer to initial state
     * Now includes session saving if workout was started (FR-010)
     * Preserves current configuration settings
     */
    fun reset() {
        // Save session if workout was started (FR-010: Workout Session Tracking)
        if (sessionStartTime > 0) {
            saveCurrentSession()
        }

        timerJob?.cancel()
        resetSessionTracking()

        // Preserve current configuration when resetting
        val currentConfig = _timerStatus.value.config
        _timerStatus.value = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = currentConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = currentConfig
        )
    }
    
    /**
     * Update timer configuration (only when timer is idle)
     */
    fun updateConfig(config: TimerConfig) {
        if (_timerStatus.value.state == TimerState.IDLE) {
            _timerStatus.value = _timerStatus.value.copy(
                config = config,
                timeRemainingSeconds = config.workTimeSeconds,
                timeRemainingMilliseconds = 0
            )
        }
    }
    
    private fun startCountdown() {
        timerJob = scope.launch {
            try {
                while (_timerStatus.value.state == TimerState.RUNNING &&
                       (_timerStatus.value.timeRemainingSeconds > 0 || _timerStatus.value.timeRemainingMilliseconds > 0)) {

                    // Use performance-optimized interval (TS-003: Performance optimization)
                    val interval = performanceManager?.getOptimalTimerInterval() ?: 100L
                    val startTime = System.currentTimeMillis()
                    delay(interval)

                val currentStatus = _timerStatus.value
                var newSeconds = currentStatus.timeRemainingSeconds
                var newMilliseconds = currentStatus.timeRemainingMilliseconds - interval.toInt()

                // Track timer accuracy for performance monitoring (TS-003)
                val actualDelay = System.currentTimeMillis() - startTime
                performanceManager?.updateTimerAccuracy(kotlin.math.abs(actualDelay - interval))

                // Handle millisecond underflow
                if (newMilliseconds < 0) {
                    newSeconds -= 1
                    newMilliseconds = 1000 - interval.toInt() // Adjust based on interval
                }

                // Play countdown beeps (FR-006: 3-second countdown beeps)
                if (newSeconds in 1..3 && newMilliseconds == 0) {
                    audioManager?.playCountdownBeep()
                }

                if (newSeconds <= 0 && newMilliseconds <= 0) {
                    // Interval finished, switch to next interval or round
                    handleIntervalComplete()
                } else {
                    // Update time remaining
                    _timerStatus.value = currentStatus.copy(
                        timeRemainingSeconds = newSeconds,
                        timeRemainingMilliseconds = newMilliseconds
                    )
                }
            }
            } catch (e: Exception) {
                // Handle any errors during countdown
                _timerStatus.value = _timerStatus.value.copy(state = TimerState.IDLE)
            }
        }
    }
    
    private fun handleIntervalComplete() {
        val currentStatus = _timerStatus.value
        val config = currentStatus.config

        // Track actual time spent in current interval (FR-010: Session tracking)
        val now = System.currentTimeMillis()
        val intervalDuration = now - lastIntervalStartTime

        when (currentStatus.currentInterval) {
            IntervalType.WORK -> {
                actualWorkTimeMs += intervalDuration
                // Work interval finished, check if rest is disabled (FR-001: "No Rest" toggle)
                if (config.noRest) {
                    // Skip rest interval, go directly to next round
                    val nextRound = currentStatus.currentRound + 1

                    if (config.isUnlimited || nextRound <= config.totalRounds) {
                        // Start next round immediately
                        _timerStatus.value = currentStatus.copy(
                            currentInterval = IntervalType.WORK,
                            timeRemainingSeconds = config.workTimeSeconds,
                            timeRemainingMilliseconds = 0,
                            currentRound = nextRound
                        )
                        // Play work interval start sound (FR-006: Audio cues)
                        audioManager?.playWorkIntervalSound()
                        lastIntervalStartTime = System.currentTimeMillis()
                    } else {
                        // All rounds completed
                        timerJob?.cancel()
                        _timerStatus.value = currentStatus.copy(
                            state = TimerState.FINISHED,
                            timeRemainingSeconds = 0,
                            timeRemainingMilliseconds = 0
                        )
                        // Play completion sound (FR-006: Audio cues)
                        audioManager?.playCompletionSound()
                        // Save completed session (FR-010: Workout Session Tracking)
                        saveCurrentSession()
                    }
                } else {
                    // Start rest interval
                    _timerStatus.value = currentStatus.copy(
                        currentInterval = IntervalType.REST,
                        timeRemainingSeconds = config.restTimeSeconds,
                        timeRemainingMilliseconds = 0
                    )
                    // Play rest interval start sound (FR-006: Audio cues)
                    audioManager?.playRestIntervalSound()
                    lastIntervalStartTime = System.currentTimeMillis()
                }
            }
            IntervalType.REST -> {
                actualRestTimeMs += intervalDuration
                // Rest interval finished, check if we should continue to next round
                val nextRound = currentStatus.currentRound + 1

                if (config.isUnlimited || nextRound <= config.totalRounds) {
                    // Start next round
                    _timerStatus.value = currentStatus.copy(
                        currentInterval = IntervalType.WORK,
                        timeRemainingSeconds = config.workTimeSeconds,
                        timeRemainingMilliseconds = 0,
                        currentRound = nextRound
                    )
                    // Play work interval start sound (FR-006: Audio cues)
                    audioManager?.playWorkIntervalSound()
                    lastIntervalStartTime = System.currentTimeMillis()
                } else {
                    // All rounds completed
                    timerJob?.cancel()
                    _timerStatus.value = currentStatus.copy(
                        state = TimerState.FINISHED,
                        timeRemainingSeconds = 0,
                        timeRemainingMilliseconds = 0
                    )
                    // Play completion sound (FR-006: Audio cues)
                    audioManager?.playCompletionSound()
                    // Save completed session (FR-010: Workout Session Tracking)
                    saveCurrentSession()
                }
            }
        }
    }
    
    /**
     * Save current workout session (FR-010: Workout Session Tracking)
     */
    private fun saveCurrentSession() {
        if (sessionStartTime == 0L || workoutHistoryRepository == null) return

        val currentStatus = _timerStatus.value
        val endTime = System.currentTimeMillis()

        // Calculate completed rounds based on current state
        val completedRounds = when (currentStatus.state) {
            TimerState.FINISHED -> currentStatus.currentRound
            TimerState.RUNNING, TimerState.PAUSED -> {
                // If we're in a work interval, count current round as completed
                if (currentStatus.currentInterval == IntervalType.WORK) {
                    currentStatus.currentRound - 1
                } else {
                    currentStatus.currentRound
                }
            }
            else -> 0
        }

        val session = WorkoutSession.fromTimerSession(
            config = currentStatus.config,
            presetId = currentPresetId,
            presetName = currentPresetName,
            exerciseName = currentExerciseName,
            completedRounds = completedRounds,
            startTime = sessionStartTime,
            endTime = endTime,
            actualWorkTimeMs = actualWorkTimeMs,
            actualRestTimeMs = actualRestTimeMs
        )

        // Save session asynchronously
        scope.launch {
            try {
                workoutHistoryRepository.saveWorkoutSession(session)
            } catch (e: Exception) {
                // Log error but don't crash the app
                // In a real app, you might want to emit an error state
            }
        }
    }

    /**
     * Reset session tracking variables
     */
    private fun resetSessionTracking() {
        sessionStartTime = 0
        currentPresetId = null
        currentPresetName = "Custom Workout"
        currentExerciseName = null
        actualWorkTimeMs = 0
        actualRestTimeMs = 0
        lastIntervalStartTime = 0
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        // Save session if workout was in progress
        if (sessionStartTime > 0) {
            saveCurrentSession()
        }

        timerJob?.cancel()
        scope.cancel()
    }
}
