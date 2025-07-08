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
            while (_timerStatus.value.state == TimerState.RUNNING && _timerStatus.value.timeRemainingSeconds > 0) {
                delay(1000) // 1 second intervals

                val currentStatus = _timerStatus.value
                val newTimeRemaining = currentStatus.timeRemainingSeconds - 1

                // Play countdown beeps (FR-006: 3-second countdown beeps)
                if (newTimeRemaining in 1..3) {
                    audioManager?.playCountdownBeep()
                }

                if (newTimeRemaining <= 0) {
                    // Interval finished, switch to next interval or round
                    handleIntervalComplete()
                } else {
                    // Update time remaining
                    _timerStatus.value = currentStatus.copy(timeRemainingSeconds = newTimeRemaining)
                }
            }
        }
    }
    
    private fun handleIntervalComplete() {
        val currentStatus = _timerStatus.value
        val config = currentStatus.config

        when (currentStatus.currentInterval) {
            IntervalType.WORK -> {
                // Work interval finished, start rest interval
                _timerStatus.value = currentStatus.copy(
                    currentInterval = IntervalType.REST,
                    timeRemainingSeconds = config.restTimeSeconds
                )
                // Play rest interval start sound (FR-006: Audio cues)
                audioManager?.playRestIntervalSound()
            }
            IntervalType.REST -> {
                // Rest interval finished, check if we should continue to next round
                val nextRound = currentStatus.currentRound + 1

                if (config.isUnlimited || nextRound <= config.totalRounds) {
                    // Start next round
                    _timerStatus.value = currentStatus.copy(
                        currentInterval = IntervalType.WORK,
                        timeRemainingSeconds = config.workTimeSeconds,
                        currentRound = nextRound
                    )
                    // Play work interval start sound (FR-006: Audio cues)
                    audioManager?.playWorkIntervalSound()
                } else {
                    // All rounds completed
                    timerJob?.cancel()
                    _timerStatus.value = currentStatus.copy(
                        state = TimerState.FINISHED,
                        timeRemainingSeconds = 0
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
