package com.hiittimer.app.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.audio.AudioSettings
import com.hiittimer.app.data.*
import com.hiittimer.app.performance.PerformanceManager
import com.hiittimer.app.service.TimerServiceConnection
import com.hiittimer.app.timer.UnifiedTimerManager
import com.hiittimer.app.timer.UnifiedTimerState
import com.hiittimer.app.utils.Logger
import com.hiittimer.app.error.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Enhanced TimerViewModel that supports both simple and complex workouts
 */
class UnifiedTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    
    // Repositories
    private val complexWorkoutRepository = ComplexWorkoutRepository(application)
    private val workoutHistoryRepository = InMemoryWorkoutHistoryRepository()
    
    // Service connection for background timer operation
    private val serviceConnection = TimerServiceConnection(application)
    
    // Audio and performance managers
    private val audioManager = AudioManager(application, preferencesManager.audioSettings.value)
    private val performanceManager = PerformanceManager(application)
    
    // Unified timer manager
    private val unifiedTimerManager = UnifiedTimerManager(
        audioManager = audioManager,
        workoutHistoryRepository = workoutHistoryRepository,
        complexWorkoutRepository = complexWorkoutRepository,
        performanceManager = performanceManager
    )
    
    // Exposed state flows
    val unifiedTimerState: StateFlow<UnifiedTimerState> = unifiedTimerManager.unifiedState
    val complexWorkouts: Flow<List<ComplexWorkout>> = complexWorkoutRepository.workouts
    val audioSettings: StateFlow<AudioSettings> = preferencesManager.audioSettings
    val themePreference: StateFlow<ThemePreference> = preferencesManager.themePreference
    val isServiceConnected: StateFlow<Boolean> = serviceConnection.isServiceConnected
    
    // Current workout mode
    val workoutMode: StateFlow<UnifiedTimerManager.WorkoutMode> = unifiedTimerManager.workoutMode
    
    // Presets for UI
    val presets: StateFlow<List<Preset>> = flow<List<Preset>> {
        emit(emptyList()) // TODO: Load presets from a preset repository
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList<Preset>()
    )
    
    // Legacy timer status for backward compatibility
    val timerStatus: StateFlow<TimerStatus> = unifiedTimerState.map { state ->
        TimerStatus(
            state = state.timerState,
            currentInterval = state.intervalType,
            timeRemainingSeconds = state.timeRemainingSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = state.currentRound,
            config = TimerConfig(), // Default config
            countdownText = state.countdownText,
            shouldFlashBlue = state.shouldFlash
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TimerStatus.createDefault()
    )
    
    init {
        // Initialize performance monitoring
        performanceManager.initialize()
        
        // Bind to timer service for background operation
        serviceConnection.bindService()
        
        // Update audio settings when changed
        viewModelScope.launch {
            preferencesManager.audioSettings.collect { settings ->
                audioManager.updateSettings(settings)
            }
        }
    }
    
    /**
     * Start a simple timer with current configuration
     */
    fun startTimer() {
        try {
            val config = timerStatus.value.config
            unifiedTimerManager.startSimpleWorkout(config)
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Started simple timer")
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to start timer", e)
        }
    }
    
    /**
     * Start the timer with a preset
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
        unifiedTimerManager.startSimpleWorkout(config, preset.id, preset.name, preset.exerciseName)
    }
    
    /**
     * Start a complex workout
     */
    fun startComplexWorkout(workoutId: String) {
        viewModelScope.launch {
            try {
                unifiedTimerManager.startComplexWorkout(workoutId)
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Started complex workout: $workoutId")
            } catch (e: Exception) {
                Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to start complex workout", e)
            }
        }
    }
    
    /**
     * Start a complex workout directly
     */
    fun startComplexWorkoutDirect(workout: ComplexWorkout) {
        try {
            unifiedTimerManager.startComplexWorkoutDirect(workout)
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Started complex workout: ${workout.name}")
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to start complex workout", e)
        }
    }
    
    /**
     * Pause the timer
     */
    fun pauseTimer() {
        unifiedTimerManager.pause()
    }
    
    /**
     * Resume the timer
     */
    fun resumeTimer() {
        unifiedTimerManager.resume()
    }
    
    /**
     * Reset the timer
     */
    fun resetTimer() {
        unifiedTimerManager.reset()
    }
    
    /**
     * Mark current rep as completed (for rep-based exercises)
     */
    fun markRepCompleted() {
        unifiedTimerManager.markRepCompleted()
    }
    
    /**
     * Skip to next exercise
     */
    fun skipToNext() {
        unifiedTimerManager.skipToNext()
    }
    
    /**
     * Update timer configuration (for simple workouts)
     */
    fun updateConfig(
        workTimeSeconds: Int = timerStatus.value.config.workTimeSeconds,
        restTimeSeconds: Int = timerStatus.value.config.restTimeSeconds,
        totalRounds: Int = timerStatus.value.config.totalRounds,
        isUnlimited: Boolean = timerStatus.value.config.isUnlimited,
        noRest: Boolean = timerStatus.value.config.noRest,
        countdownDurationSeconds: Int = timerStatus.value.config.countdownDurationSeconds
    ) {
        try {
            val newConfig = TimerConfig(
                workTimeSeconds = workTimeSeconds,
                restTimeSeconds = restTimeSeconds,
                totalRounds = totalRounds,
                isUnlimited = isUnlimited,
                noRest = noRest,
                countdownDurationSeconds = countdownDurationSeconds
            )
            unifiedTimerManager.updateConfig(newConfig)
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Failed to update configuration", e)
        }
    }
    
    /**
     * Update configuration and reset timer
     */
    fun updateConfigAndReset(
        workTimeSeconds: Int = timerStatus.value.config.workTimeSeconds,
        restTimeSeconds: Int = timerStatus.value.config.restTimeSeconds,
        totalRounds: Int = timerStatus.value.config.totalRounds,
        isUnlimited: Boolean = timerStatus.value.config.isUnlimited,
        noRest: Boolean = timerStatus.value.config.noRest,
        countdownDurationSeconds: Int = timerStatus.value.config.countdownDurationSeconds
    ) {
        updateConfig(workTimeSeconds, restTimeSeconds, totalRounds, isUnlimited, noRest, countdownDurationSeconds)
        resetTimer()
    }
    
    /**
     * Save a complex workout
     */
    fun saveComplexWorkout(workout: ComplexWorkout) {
        viewModelScope.launch {
            try {
                complexWorkoutRepository.saveWorkout(workout)
                Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Saved workout: ${workout.name}")
            } catch (e: Exception) {
                Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Failed to save workout", e)
            }
        }
    }
    
    /**
     * Delete a complex workout
     */
    fun deleteComplexWorkout(workoutId: String) {
        viewModelScope.launch {
            try {
                complexWorkoutRepository.deleteWorkout(workoutId)
                Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Deleted workout: $workoutId")
            } catch (e: Exception) {
                Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Failed to delete workout", e)
            }
        }
    }
    
    /**
     * Search complex workouts
     */
    fun searchComplexWorkouts(query: String): List<ComplexWorkout> {
        return complexWorkoutRepository.searchWorkouts(query)
    }
    
    /**
     * Import workout from JSON
     */
    fun importWorkout(jsonString: String) {
        viewModelScope.launch {
            try {
                complexWorkoutRepository.importWorkout(jsonString)
                Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Imported workout successfully")
            } catch (e: Exception) {
                Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Failed to import workout", e)
            }
        }
    }
    
    /**
     * Export workout to JSON
     */
    suspend fun exportWorkout(workoutId: String): String? {
        return try {
            complexWorkoutRepository.exportWorkout(workoutId).getOrNull()
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Failed to export workout", e)
            null
        }
    }
    
    /**
     * Toggle audio enabled/disabled
     */
    fun toggleAudio() {
        preferencesManager.toggleAudioEnabled()
        audioManager.updateSettings(preferencesManager.audioSettings.value)
    }
    
    /**
     * Set audio volume
     */
    fun setAudioVolume(volume: Float) {
        preferencesManager.setAudioVolume(volume)
        audioManager.updateSettings(preferencesManager.audioSettings.value)
    }
    
    /**
     * Set theme preference
     */
    fun setThemePreference(preference: ThemePreference) {
        preferencesManager.setThemePreference(preference)
    }
    
    /**
     * Get workout history repository
     */
    fun getWorkoutHistoryRepository(): WorkoutHistoryRepository {
        return workoutHistoryRepository
    }
    
    /**
     * Get performance manager
     */
    fun getPerformanceManager(): PerformanceManager {
        return performanceManager
    }
    
    /**
     * Force memory cleanup
     */
    fun forceMemoryCleanup() {
        performanceManager.forceMemoryCleanup()
    }
    
    override fun onCleared() {
        super.onCleared()
        serviceConnection.cleanup()
        performanceManager.cleanup()
        unifiedTimerManager.cleanup()
        audioManager.cleanup()
    }
}