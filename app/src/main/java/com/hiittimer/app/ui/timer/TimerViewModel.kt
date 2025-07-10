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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview

/**
 * ViewModel for the timer screen with background service integration and workout history tracking
 */
@OptIn(FlowPreview::class)
class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    // Service connection for background timer operation
    private val serviceConnection = TimerServiceConnection(application)

    // Fallback for when service is not connected
    private val fallbackAudioManager = AudioManager(application, preferencesManager.audioSettings.value)
    private val fallbackWorkoutHistoryRepository = InMemoryWorkoutHistoryRepository()
    private val fallbackPerformanceManager = PerformanceManager(application)
    private val fallbackTimerManager = TimerManager(fallbackAudioManager, fallbackWorkoutHistoryRepository, fallbackPerformanceManager)

    // Debounce service connection status to prevent rapid switching
    private val stableServiceConnection = serviceConnection.isServiceConnected
        .debounce(300) // Wait 300ms before switching between service and fallback
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // Exposed state flows - combine service and fallback timer status
    val timerStatus: StateFlow<TimerStatus> = combine(
        serviceConnection.timerStatus,
        fallbackTimerManager.timerStatus,
        stableServiceConnection
    ) { serviceStatus, fallbackStatus, isConnected ->
        // Use service status when connected, fallback otherwise
        val selectedStatus = if (isConnected) serviceStatus else fallbackStatus
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "TimerStatus update: isConnected=$isConnected, state=${selectedStatus.state}, canResume=${selectedStatus.canResume}")
        selectedStatus
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
     * STOPPED/FINISHED → BEGIN → RUNNING
     */
    fun startTimer() {
        try {
            // Only start if currently stopped or finished
            if (timerStatus.value.canStart) {
                val config = timerStatus.value.config
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Starting timer: canStart=${timerStatus.value.canStart}, state=${timerStatus.value.state} → BEGIN")

                // Use service if connected, fallback otherwise
                if (serviceConnection.isBound()) {
                    serviceConnection.startTimer(config)
                } else {
                    fallbackTimerManager.start(config)
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
        // Use service if connected, fallback otherwise
        if (serviceConnection.isBound()) {
            serviceConnection.startTimer(config, preset.id, preset.name, preset.exerciseName)
        } else {
            fallbackTimerManager.start(config, preset.id, preset.name, preset.exerciseName)
        }
    }
    
    /**
     * Pause the timer
     * RUNNING → PAUSED
     */
    fun pauseTimer() {
        try {
            // Only pause if currently running
            if (timerStatus.value.canPause) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Pausing timer: canPause=${timerStatus.value.canPause}, state=${timerStatus.value.state} → PAUSED")
                if (serviceConnection.isBound()) {
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Using service to pause timer")
                    serviceConnection.pauseTimer()
                } else {
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Using fallback to pause timer")
                    fallbackTimerManager.pause()
                }
                // Add a small delay to allow state to propagate
                viewModelScope.launch {
                    kotlinx.coroutines.delay(50)
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "After pause: state=${timerStatus.value.state}, canResume=${timerStatus.value.canResume}")
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
     * PAUSED → RUNNING
     */
    fun resumeTimer() {
        try {
            // Only resume if currently paused
            if (timerStatus.value.canResume) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Resuming timer: canResume=${timerStatus.value.canResume}, state=${timerStatus.value.state} → RUNNING")
                if (serviceConnection.isBound()) {
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Using service to resume timer")
                    serviceConnection.resumeTimer()
                } else {
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Using fallback to resume timer")
                    fallbackTimerManager.resume()
                }
                // Add a small delay to allow state to propagate
                viewModelScope.launch {
                    kotlinx.coroutines.delay(50)
                    Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "After resume: state=${timerStatus.value.state}, canPause=${timerStatus.value.canPause}")
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
     * Any state → STOPPED
     */
    fun resetTimer() {
        try {
            if (timerStatus.value.canReset) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Resetting timer: canReset=${timerStatus.value.canReset}, state=${timerStatus.value.state} → STOPPED")
                // Reset using service if connected, fallback otherwise
                if (serviceConnection.isBound()) {
                    serviceConnection.resetTimer()
                } else {
                    fallbackTimerManager.reset()
                }
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
            // Update config using service if connected, fallback otherwise
            if (serviceConnection.isBound()) {
                serviceConnection.updateConfig(newConfig)
            } else {
                fallbackTimerManager.updateConfig(newConfig)
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
