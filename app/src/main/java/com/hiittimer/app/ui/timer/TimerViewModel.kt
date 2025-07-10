package com.hiittimer.app.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.audio.AudioSettings
import com.hiittimer.app.data.*
import com.hiittimer.app.performance.PerformanceManager
import com.hiittimer.app.service.TimerServiceConnection
import com.hiittimer.app.timer.TimerManager
import com.hiittimer.app.utils.Logger
import com.hiittimer.app.error.ErrorHandler
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * ViewModel for the timer screen with background service integration and workout history tracking
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    // Service connection for background timer operation
    private val serviceConnection = TimerServiceConnection(application)

    // Fallback for when service is not connected
    private val fallbackAudioManager = AudioManager(application, preferencesManager.audioSettings.value)
    private val fallbackWorkoutHistoryRepository = InMemoryWorkoutHistoryRepository()
    private val fallbackPerformanceManager = PerformanceManager(application)
    private val fallbackTimerManager = TimerManager(fallbackAudioManager, fallbackWorkoutHistoryRepository, fallbackPerformanceManager)

    // Exposed state flows - combine service and fallback timer status
    val timerStatus: StateFlow<TimerStatus> = combine(
        serviceConnection.timerStatus,
        fallbackTimerManager.timerStatus,
        serviceConnection.isServiceConnected
    ) { serviceStatus, fallbackStatus, isConnected ->
        // Use service status when connected, fallback otherwise
        if (isConnected) serviceStatus else fallbackStatus
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TimerStatus.createDefault() // FR-042: Proper initial display
    )
    val audioSettings: StateFlow<AudioSettings> = preferencesManager.audioSettings
    val themePreference: StateFlow<ThemePreference> = preferencesManager.themePreference
    val isServiceConnected: StateFlow<Boolean> = serviceConnection.isServiceConnected

    init {
        // Initialize performance monitoring
        fallbackPerformanceManager.initialize()

        // Bind to timer service for background operation
        serviceConnection.bindService()
    }

    /**
     * Start the timer with current configuration
     */
    fun startTimer() {
        try {
            // Only start if currently idle or finished
            if (timerStatus.value.canStart) {
                val config = timerStatus.value.config
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Starting timer: canStart=${timerStatus.value.canStart}, state=${timerStatus.value.state}")

                // Always use fallback timer manager for reliable operation
                fallbackTimerManager.start(config)

                // Also start service for background operation if available
                if (serviceConnection.isBound()) {
                    serviceConnection.startTimer(config)
                }
            } else {
                Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Cannot start timer: canStart=${timerStatus.value.canStart}, state=${timerStatus.value.state}")
            }
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to start timer: ${e.message}", e)
            // In a production app, you might want to emit an error state here
        }
    }

    /**
     * Start the timer with a preset (FR-010: Session tracking with preset info)
     */
    fun startTimerWithPreset(preset: Preset) {
        val config = preset.toTimerConfig()
        updateConfig(
            workTimeSeconds = config.workTimeSeconds,
            restTimeSeconds = config.restTimeSeconds,
            totalRounds = config.totalRounds,
            isUnlimited = config.isUnlimited,
            noRest = config.noRest
        )
        // Always use fallback timer manager for reliable operation
        fallbackTimerManager.start(config, preset.id, preset.name, preset.exerciseName)

        // Also start service for background operation if available
        if (serviceConnection.isBound()) {
            serviceConnection.startTimer(config, preset.id, preset.name, preset.exerciseName)
        }
    }
    
    /**
     * Pause the timer
     */
    fun pauseTimer() {
        try {
            // Only pause if currently running
            if (timerStatus.value.canPause) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Pausing timer: canPause=${timerStatus.value.canPause}, state=${timerStatus.value.state}")
                fallbackTimerManager.pause()
                if (serviceConnection.isBound()) {
                    serviceConnection.pauseTimer()
                }
            } else {
                Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Cannot pause timer: canPause=${timerStatus.value.canPause}, state=${timerStatus.value.state}")
            }
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to pause timer: ${e.message}", e)
        }
    }

    /**
     * Resume the timer
     */
    fun resumeTimer() {
        try {
            // Only resume if currently paused
            if (timerStatus.value.canResume) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Resuming timer: canResume=${timerStatus.value.canResume}, state=${timerStatus.value.state}")
                fallbackTimerManager.resume()
                if (serviceConnection.isBound()) {
                    serviceConnection.resumeTimer()
                }
            } else {
                Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Cannot resume timer: canResume=${timerStatus.value.canResume}, state=${timerStatus.value.state}")
            }
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to resume timer: ${e.message}", e)
        }
    }

    /**
     * Reset the timer
     */
    fun resetTimer() {
        try {
            if (timerStatus.value.canReset) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Resetting timer: canReset=${timerStatus.value.canReset}, state=${timerStatus.value.state}")
                // Ensure both fallback and service are reset to maintain synchronization
                fallbackTimerManager.reset()
                if (serviceConnection.isBound()) {
                    serviceConnection.resetTimer()
                }
                // Force state synchronization after reset
                val currentConfig = timerStatus.value.config
                fallbackTimerManager.updateConfig(currentConfig)
            } else {
                Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Cannot reset timer: canReset=${timerStatus.value.canReset}, state=${timerStatus.value.state}")
            }
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to reset timer: ${e.message}", e)
        }
    }
    
    /**
     * Update timer configuration
     */
    fun updateConfig(
        workTimeSeconds: Int = timerStatus.value.config.workTimeSeconds,
        restTimeSeconds: Int = timerStatus.value.config.restTimeSeconds,
        totalRounds: Int = timerStatus.value.config.totalRounds,
        isUnlimited: Boolean = timerStatus.value.config.isUnlimited,
        noRest: Boolean = timerStatus.value.config.noRest
    ) {
        try {
            val newConfig = TimerConfig(
                workTimeSeconds = workTimeSeconds,
                restTimeSeconds = restTimeSeconds,
                totalRounds = totalRounds,
                isUnlimited = isUnlimited,
                noRest = noRest
            )
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Updating config: work=${workTimeSeconds}s, rest=${restTimeSeconds}s, rounds=${totalRounds}, state=${timerStatus.value.state}")
            fallbackTimerManager.updateConfig(newConfig)

            // Also update service if connected
            if (serviceConnection.isBound()) {
                serviceConnection.updateConfig(newConfig)
            }
        } catch (e: IllegalArgumentException) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Invalid configuration: ${e.message}", e)
            // Handle validation errors - in a real app, you might want to emit an error state
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to update configuration: ${e.message}", e)
        }
    }
    
    /**
     * Toggle audio enabled/disabled (FR-007: Audio Controls)
     */
    fun toggleAudio() {
        preferencesManager.toggleAudioEnabled()
        // Update audio settings in service or fallback
        val audioSettings = preferencesManager.audioSettings.value
        serviceConnection.getAudioManager()?.updateSettings(audioSettings)
            ?: fallbackAudioManager.updateSettings(audioSettings)
    }

    /**
     * Set audio volume (FR-007: Audio Controls)
     */
    fun setAudioVolume(volume: Float) {
        preferencesManager.setAudioVolume(volume)
        // Update audio settings in service or fallback
        val audioSettings = preferencesManager.audioSettings.value
        serviceConnection.getAudioManager()?.updateSettings(audioSettings)
            ?: fallbackAudioManager.updateSettings(audioSettings)
    }

    /**
     * Set theme preference (FR-014: Manual theme override)
     */
    fun setThemePreference(preference: ThemePreference) {
        preferencesManager.setThemePreference(preference)
    }

    /**
     * Get workout history repository for history screen (FR-011, FR-012)
     */
    fun getWorkoutHistoryRepository(): WorkoutHistoryRepository {
        return serviceConnection.getWorkoutHistoryRepository() ?: fallbackWorkoutHistoryRepository
    }

    /**
     * Get performance manager for performance monitoring (TS-003, TS-004)
     */
    fun getPerformanceManager(): PerformanceManager {
        return fallbackPerformanceManager
    }

    /**
     * Force memory cleanup for performance optimization
     */
    fun forceMemoryCleanup() {
        fallbackPerformanceManager.forceMemoryCleanup()
    }

    override fun onCleared() {
        super.onCleared()
        serviceConnection.cleanup()
        fallbackPerformanceManager.cleanup()
        fallbackTimerManager.cleanup()
        fallbackAudioManager.cleanup()
    }
}
