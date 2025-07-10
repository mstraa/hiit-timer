package com.hiittimer.app.timer

import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import com.hiittimer.app.error.ErrorHandler
import com.hiittimer.app.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.hiittimer.app.performance.PerformanceManager

/**
 * Completely rewritten TimerManager with proper state management and configuration-based timing
 * 
 * State Transitions:
 * STOPPED → BEGIN (start button pressed)
 * BEGIN → RUNNING (after countdown completes)
 * RUNNING → PAUSED (pause button pressed)
 * PAUSED → RUNNING (resume button pressed)
 * PAUSED → STOPPED (reset button pressed)
 * Any state → STOPPED (reset button pressed)
 */
class TimerManager(
    private val audioManager: AudioManager? = null,
    private val workoutHistoryRepository: WorkoutHistoryRepository? = null,
    private val performanceManager: PerformanceManager? = null
) {
    private val _timerStatus = MutableStateFlow(TimerStatus.createDefault())
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(
        Dispatchers.Main +
        SupervisorJob() +
        CoroutineName("TimerManager")
    )

    // Session tracking variables for workout history
    private var sessionStartTime: Long = 0
    private var currentPresetId: String? = null
    private var currentPresetName: String = "Custom Workout"
    private var currentExerciseName: String? = null
    private var actualWorkTimeMs: Long = 0
    private var actualRestTimeMs: Long = 0
    private var lastIntervalStartTime: Long = 0
    
    /**
     * Start the timer: STOPPED/FINISHED → BEGIN
     * Initiates the countdown sequence before starting the actual workout
     */
    fun start(config: TimerConfig, presetId: String? = null, presetName: String = "Custom Workout", exerciseName: String? = null) {
        try {
            val currentState = _timerStatus.value.state
            
            // Validate state transition
            if (!_timerStatus.value.canStart) {
                Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                    "Cannot start timer from state: $currentState")
                throw ErrorHandler.TimerException.TimerStateInvalid("Timer can only be started from STOPPED or FINISHED state")
            }

            // Validate configuration
            validateConfig(config)

            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Starting timer: $currentState → BEGIN, config=${config}")

            // Initialize session tracking
            initializeSession(presetId, presetName, exerciseName)

            // Transition to BEGIN state with countdown
            _timerStatus.value = TimerStatus(
                state = TimerState.BEGIN,
                currentInterval = IntervalType.WORK,
                timeRemainingSeconds = config.countdownDurationSeconds,
                timeRemainingMilliseconds = 0,
                currentRound = 1,
                config = config,
                countdownText = "Start in ${config.countdownDurationSeconds}",
                shouldFlashBlue = false
            )

            startTimerCountdown()
            
        } catch (e: ErrorHandler.TimerException) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer start failed: ${e.message}", e)
            resetToStoppedState()
            throw e
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Unexpected error during timer start", e)
            resetToStoppedState()
            throw ErrorHandler.TimerException.TimerStartFailure("Failed to start timer", e)
        }
    }
    
    /**
     * Pause the timer: RUNNING → PAUSED
     * Only allowed during RUNNING state, not during BEGIN countdown
     */
    fun pause() {
        val currentState = _timerStatus.value.state
        
        if (!_timerStatus.value.canPause) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Cannot pause timer from state: $currentState")
            return
        }

        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Pausing timer: $currentState → PAUSED")

        timerJob?.cancel()
        
        // Transition to PAUSED state, preserving current time and configuration
        _timerStatus.value = _timerStatus.value.copy(
            state = TimerState.PAUSED,
            countdownText = null,
            shouldFlashBlue = false
        )
        
        // Track session timing
        trackIntervalTime()
        
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "Timer paused successfully: state=${_timerStatus.value.state}, canResume=${_timerStatus.value.canResume}")
    }

    /**
     * Resume the timer: PAUSED → RUNNING
     * Continues from the exact position where it was paused
     */
    fun resume() {
        val currentState = _timerStatus.value.state
        
        if (!_timerStatus.value.canResume) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Cannot resume timer from state: $currentState")
            return
        }

        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Resuming timer: $currentState → RUNNING")

        // Transition back to RUNNING state
        _timerStatus.value = _timerStatus.value.copy(
            state = TimerState.RUNNING,
            countdownText = null,
            shouldFlashBlue = false
        )
        
        // Reset interval tracking
        lastIntervalStartTime = System.currentTimeMillis()
        
        startTimerCountdown()
        
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION,
            "Timer resumed successfully: state=${_timerStatus.value.state}, canPause=${_timerStatus.value.canPause}")
    }
    
    /**
     * Reset the timer: Any state → STOPPED
     * Returns timer to initial configuration values
     */
    fun reset() {
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "Resetting timer: ${_timerStatus.value.state} → STOPPED")

        // Save session if workout was in progress
        if (sessionStartTime > 0) {
            saveCurrentSession()
        }

        timerJob?.cancel()
        resetSessionTracking()
        resetToStoppedState()
    }
    
    /**
     * Update timer configuration
     * Only allowed when timer is in STOPPED or FINISHED state
     */
    fun updateConfig(config: TimerConfig) {
        val currentState = _timerStatus.value.state
        
        if (currentState != TimerState.STOPPED && currentState != TimerState.FINISHED && currentState != TimerState.PAUSED) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Cannot update configuration while timer is $currentState")
            return
        }

        try {
            validateConfig(config)
            
            // Update configuration and reset to initial display values
            _timerStatus.value = TimerStatus(
                state = currentState,
                currentInterval = IntervalType.WORK,
                timeRemainingSeconds = config.workTimeSeconds,
                timeRemainingMilliseconds = 0,
                currentRound = 1,
                config = config,
                countdownText = null,
                shouldFlashBlue = false
            )
            
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Configuration updated: work=${config.workTimeSeconds}s, rest=${config.restTimeSeconds}s, rounds=${config.totalRounds}")
                
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to update configuration", e)
        }
    }

    /**
     * Sync timer state with external state (for service coordination)
     */
    fun syncState(timerStatus: TimerStatus) {
        _timerStatus.value = timerStatus
    }
    
    // === Private Implementation Methods ===
    
    private fun startTimerCountdown() {
        timerJob = scope.launch {
            try {
                while (isTimerActive()) {
                    val startTime = System.currentTimeMillis()
                    
                    // Use performance-optimized interval
                    val interval = performanceManager?.getOptimalTimerInterval() ?: 100L
                    delay(interval)
                    
                    val actualDelay = System.currentTimeMillis() - startTime
                    
                    // Track timer accuracy for performance monitoring
                    performanceManager?.updateTimerAccuracy(kotlin.math.abs(actualDelay - interval))
                    
                    updateTimerTime(interval.toInt())
                }
            } catch (e: CancellationException) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer countdown cancelled")
            } catch (e: Exception) {
                Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Error during countdown", e)
                resetToStoppedState()
            }
        }
    }
    
    private fun updateTimerTime(intervalMs: Int) {
        val currentStatus = _timerStatus.value
        var newSeconds = currentStatus.timeRemainingSeconds
        var newMilliseconds = currentStatus.timeRemainingMilliseconds - intervalMs

        // Handle millisecond underflow
        if (newMilliseconds < 0) {
            newSeconds -= 1
            newMilliseconds += 1000
        }

        if (newSeconds <= 0 && newMilliseconds <= 0) {
            // Time finished, handle state transition
            handleTimerComplete()
        } else {
            // Update time remaining and handle state-specific logic
            when (currentStatus.state) {
                TimerState.BEGIN -> updateBeginCountdown(newSeconds, newMilliseconds)
                TimerState.RUNNING -> updateRunningTimer(newSeconds, newMilliseconds)
                else -> {
                    // Update time for other states
                    _timerStatus.value = currentStatus.copy(
                        timeRemainingSeconds = newSeconds,
                        timeRemainingMilliseconds = newMilliseconds
                    )
                }
            }
        }
    }
    
    private fun updateBeginCountdown(newSeconds: Int, newMilliseconds: Int) {
        val currentStatus = _timerStatus.value
        
        // Handle BEGIN countdown with blue flash and text updates
        val shouldFlash = newMilliseconds <= 100 && newSeconds != currentStatus.timeRemainingSeconds
        val countdownText = if (newSeconds > 0) "Start in $newSeconds" else "GO!"

        _timerStatus.value = currentStatus.copy(
            timeRemainingSeconds = newSeconds,
            timeRemainingMilliseconds = newMilliseconds,
            countdownText = countdownText,
            shouldFlashBlue = shouldFlash
        )
    }
    
    private fun updateRunningTimer(newSeconds: Int, newMilliseconds: Int) {
        val currentStatus = _timerStatus.value
        
        // Handle audio cues for last 3 seconds
        if (newSeconds in 1..3 && newMilliseconds <= 100 && newSeconds != currentStatus.timeRemainingSeconds) {
            audioManager?.playCountdownBeep()
        }

        _timerStatus.value = currentStatus.copy(
            timeRemainingSeconds = newSeconds,
            timeRemainingMilliseconds = newMilliseconds,
            countdownText = null,
            shouldFlashBlue = false
        )
    }
    
    private fun handleTimerComplete() {
        val currentStatus = _timerStatus.value
        
        when (currentStatus.state) {
            TimerState.BEGIN -> handleBeginComplete()
            TimerState.RUNNING -> handleIntervalComplete()
            else -> { /* Should not happen */ }
        }
    }
    
    private fun handleBeginComplete() {
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "BEGIN countdown complete → RUNNING")
        
        val currentStatus = _timerStatus.value
        val config = currentStatus.config
        
        // Transition to RUNNING state with first work interval
        _timerStatus.value = currentStatus.copy(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = config.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            countdownText = null,
            shouldFlashBlue = false
        )

        // Play work interval start sound and track timing
        playIntervalStartSound(IntervalType.WORK)
        lastIntervalStartTime = System.currentTimeMillis()
    }
    
    private fun handleIntervalComplete() {
        val currentStatus = _timerStatus.value
        val config = currentStatus.config
        
        // Track actual time spent in current interval
        trackIntervalTime()

        when (currentStatus.currentInterval) {
            IntervalType.WORK -> handleWorkIntervalComplete(currentStatus, config)
            IntervalType.REST -> handleRestIntervalComplete(currentStatus, config)
        }
    }
    
    private fun handleWorkIntervalComplete(currentStatus: TimerStatus, config: TimerConfig) {
        if (config.noRest) {
            // Skip rest, go directly to next round
            handleNextRound(currentStatus, config, IntervalType.WORK)
        } else {
            // Start rest interval
            _timerStatus.value = currentStatus.copy(
                currentInterval = IntervalType.REST,
                timeRemainingSeconds = config.restTimeSeconds,
                timeRemainingMilliseconds = 0
            )
            playIntervalStartSound(IntervalType.REST)
            lastIntervalStartTime = System.currentTimeMillis()
        }
    }
    
    private fun handleRestIntervalComplete(currentStatus: TimerStatus, config: TimerConfig) {
        // Rest finished, start next round
        handleNextRound(currentStatus, config, IntervalType.WORK)
    }
    
    private fun handleNextRound(currentStatus: TimerStatus, config: TimerConfig, intervalType: IntervalType) {
        val nextRound = currentStatus.currentRound + 1

        if (config.isUnlimited || nextRound <= config.totalRounds) {
            // Continue to next round
            val nextIntervalTime = if (intervalType == IntervalType.WORK) config.workTimeSeconds else config.restTimeSeconds
            
            _timerStatus.value = currentStatus.copy(
                currentInterval = intervalType,
                timeRemainingSeconds = nextIntervalTime,
                timeRemainingMilliseconds = 0,
                currentRound = nextRound
            )
            
            playIntervalStartSound(intervalType)
            lastIntervalStartTime = System.currentTimeMillis()
        } else {
            // All rounds completed
            handleWorkoutComplete()
        }
    }
    
    private fun handleWorkoutComplete() {
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Workout completed → FINISHED")
        
        timerJob?.cancel()
        
        _timerStatus.value = _timerStatus.value.copy(
            state = TimerState.FINISHED,
            timeRemainingSeconds = 0,
            timeRemainingMilliseconds = 0,
            countdownText = null,
            shouldFlashBlue = false
        )
        
        // Play completion sound and save session
        audioManager?.playCompletionSound()
        saveCurrentSession()
    }
    
    private fun isTimerActive(): Boolean {
        val state = _timerStatus.value.state
        return (state == TimerState.BEGIN || state == TimerState.RUNNING) && 
               (_timerStatus.value.timeRemainingSeconds > 0 || _timerStatus.value.timeRemainingMilliseconds > 0)
    }
    
    private fun resetToStoppedState() {
        val currentConfig = _timerStatus.value.config
        
        _timerStatus.value = TimerStatus(
            state = TimerState.STOPPED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = currentConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = currentConfig,
            countdownText = null,
            shouldFlashBlue = false
        )
    }
    
    private fun validateConfig(config: TimerConfig) {
        if (config.workTimeSeconds <= 0) {
            throw ErrorHandler.TimerException.TimerConfigurationError("Work time must be greater than 0")
        }
        // Additional validation is handled by TimerConfig's init block
    }
    
    private fun initializeSession(presetId: String?, presetName: String, exerciseName: String?) {
        sessionStartTime = System.currentTimeMillis()
        currentPresetId = presetId
        currentPresetName = presetName
        currentExerciseName = exerciseName
        actualWorkTimeMs = 0
        actualRestTimeMs = 0
        lastIntervalStartTime = sessionStartTime
    }
    
    private fun trackIntervalTime() {
        if (lastIntervalStartTime > 0) {
            val now = System.currentTimeMillis()
            val intervalDuration = now - lastIntervalStartTime
            
            when (_timerStatus.value.currentInterval) {
                IntervalType.WORK -> actualWorkTimeMs += intervalDuration
                IntervalType.REST -> actualRestTimeMs += intervalDuration
            }
        }
    }
    
    private fun playIntervalStartSound(intervalType: IntervalType) {
        try {
            when (intervalType) {
                IntervalType.WORK -> {
                    audioManager?.playWorkIntervalSound()
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Playing work interval sound")
                }
                IntervalType.REST -> {
                    audioManager?.playRestIntervalSound()
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Playing rest interval sound")
                }
            }
        } catch (e: Exception) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to play interval sound", e)
        }
    }
    
    private fun saveCurrentSession() {
        if (sessionStartTime == 0L || workoutHistoryRepository == null) return

        val currentStatus = _timerStatus.value
        val endTime = System.currentTimeMillis()

        // Calculate completed rounds based on current state
        val completedRounds = when (currentStatus.state) {
            TimerState.FINISHED -> currentStatus.currentRound
            TimerState.RUNNING, TimerState.PAUSED -> {
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
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Workout session saved successfully")
            } catch (e: Exception) {
                Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to save workout session", e)
            }
        }
    }
    
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