package com.hiittimer.app.timer

import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the HIIT timer functionality with precise timing and audio cues
 */
class TimerManager(private val audioManager: AudioManager? = null) {
    private val _timerStatus = MutableStateFlow(TimerStatus())
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()
    
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Start the timer with the given configuration
     */
    fun start(config: TimerConfig) {
        if (_timerStatus.value.state != TimerState.IDLE) return

        _timerStatus.value = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = config.workTimeSeconds,
            timeRemainingMilliseconds = 0, // Start at 0 milliseconds
            currentRound = 1,
            config = config
        )

        // Play work interval start sound (FR-006: Audio cues)
        audioManager?.playWorkIntervalSound()

        startCountdown()
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
     * Resume the timer
     */
    fun resume() {
        if (_timerStatus.value.state != TimerState.PAUSED) return
        
        _timerStatus.value = _timerStatus.value.copy(state = TimerState.RUNNING)
        startCountdown()
    }
    
    /**
     * Reset the timer to initial state
     */
    fun reset() {
        timerJob?.cancel()
        _timerStatus.value = TimerStatus()
    }
    
    /**
     * Update timer configuration (only when timer is idle)
     */
    fun updateConfig(config: TimerConfig) {
        if (_timerStatus.value.state == TimerState.IDLE) {
            _timerStatus.value = _timerStatus.value.copy(config = config)
        }
    }
    
    private fun startCountdown() {
        timerJob = scope.launch {
            while (_timerStatus.value.state == TimerState.RUNNING &&
                   (_timerStatus.value.timeRemainingSeconds > 0 || _timerStatus.value.timeRemainingMilliseconds > 0)) {
                delay(100) // 100ms intervals for millisecond precision (FR-017)

                val currentStatus = _timerStatus.value
                var newSeconds = currentStatus.timeRemainingSeconds
                var newMilliseconds = currentStatus.timeRemainingMilliseconds - 100

                // Handle millisecond underflow
                if (newMilliseconds < 0) {
                    newSeconds -= 1
                    newMilliseconds = 900 // Reset to 900ms (0.9 seconds)
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
        }
    }
    
    private fun handleIntervalComplete() {
        val currentStatus = _timerStatus.value
        val config = currentStatus.config

        when (currentStatus.currentInterval) {
            IntervalType.WORK -> {
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
                }
            }
            IntervalType.REST -> {
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
                }
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        timerJob?.cancel()
        scope.cancel()
    }
}
