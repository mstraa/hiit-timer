package com.hiittimer.app.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.audio.AudioSettings
import com.hiittimer.app.data.*
import com.hiittimer.app.timer.TimerManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the timer screen with audio management and workout history tracking
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val audioManager = AudioManager(application)
    private val workoutHistoryRepository = InMemoryWorkoutHistoryRepository()
    private val timerManager = TimerManager(audioManager, workoutHistoryRepository)

    val timerStatus: StateFlow<TimerStatus> = timerManager.timerStatus
    val audioSettings: StateFlow<AudioSettings> = preferencesManager.audioSettings
    val themePreference: StateFlow<ThemePreference> = preferencesManager.themePreference

    /**
     * Start the timer with current configuration
     */
    fun startTimer() {
        val config = timerStatus.value.config
        timerManager.start(config)
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
        timerManager.start(config, preset.id, preset.name, preset.exerciseName)
    }
    
    /**
     * Pause the timer
     */
    fun pauseTimer() {
        timerManager.pause()
    }
    
    /**
     * Resume the timer
     */
    fun resumeTimer() {
        timerManager.resume()
    }
    
    /**
     * Reset the timer
     */
    fun resetTimer() {
        timerManager.reset()
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
            timerManager.updateConfig(newConfig)
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
        audioManager.updateSettings(preferencesManager.audioSettings.value)
    }

    /**
     * Set audio volume (FR-007: Audio Controls)
     */
    fun setAudioVolume(volume: Float) {
        preferencesManager.setAudioVolume(volume)
        audioManager.updateSettings(preferencesManager.audioSettings.value)
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
    fun getWorkoutHistoryRepository(): WorkoutHistoryRepository = workoutHistoryRepository

    override fun onCleared() {
        super.onCleared()
        timerManager.cleanup()
        audioManager.cleanup()
    }
}
