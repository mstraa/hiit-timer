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
import kotlinx.coroutines.flow.StateFlow
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

    // Exposed state flows - use fallback timer manager for reliable state
    val timerStatus: StateFlow<TimerStatus> = fallbackTimerManager.timerStatus
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
        // Only start if currently idle
        if (timerStatus.value.canStart) {
            val config = timerStatus.value.config
            // Always use fallback timer manager for reliable operation
            fallbackTimerManager.start(config)

            // Also start service for background operation if available
            if (serviceConnection.isBound()) {
                serviceConnection.startTimer(config)
            }
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
        // Only pause if currently running
        if (timerStatus.value.canPause) {
            fallbackTimerManager.pause()
            if (serviceConnection.isBound()) {
                serviceConnection.pauseTimer()
            }
        }
    }

    /**
     * Resume the timer
     */
    fun resumeTimer() {
        // Only resume if currently paused
        if (timerStatus.value.canResume) {
            fallbackTimerManager.resume()
            if (serviceConnection.isBound()) {
                serviceConnection.resumeTimer()
            }
        }
    }

    /**
     * Reset the timer
     */
    fun resetTimer() {
        // Ensure both fallback and service are reset to maintain synchronization
        fallbackTimerManager.reset()
        if (serviceConnection.isBound()) {
            serviceConnection.resetTimer()
        }
        // Force state synchronization after reset
        val currentConfig = timerStatus.value.config
        fallbackTimerManager.updateConfig(currentConfig)
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
            fallbackTimerManager.updateConfig(newConfig)
        } catch (e: IllegalArgumentException) {
            // Handle validation errors - in a real app, you might want to emit an error state
            // For now, we'll just ignore invalid configurations
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
