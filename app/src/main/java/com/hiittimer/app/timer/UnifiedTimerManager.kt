package com.hiittimer.app.timer

import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import com.hiittimer.app.error.ErrorHandler
import com.hiittimer.app.performance.PerformanceManager
import com.hiittimer.app.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unified timer manager that handles both simple and complex workouts
 * Acts as a facade to delegate to either TimerManager or ComplexTimerManager
 */
class UnifiedTimerManager(
    private val audioManager: AudioManager? = null,
    private val workoutHistoryRepository: WorkoutHistoryRepository? = null,
    private val complexWorkoutRepository: ComplexWorkoutRepository? = null,
    private val performanceManager: PerformanceManager? = null
) {
    // Timer managers
    private val simpleTimerManager = TimerManager(audioManager, workoutHistoryRepository, performanceManager)
    private val complexTimerManager = ComplexTimerManager(audioManager, workoutHistoryRepository, performanceManager)
    
    // Current workout mode
    private val _workoutMode = MutableStateFlow(WorkoutMode.SIMPLE)
    val workoutMode: StateFlow<WorkoutMode> = _workoutMode.asStateFlow()
    
    // Unified state that combines both timer states
    private val _unifiedState = MutableStateFlow(UnifiedTimerState())
    val unifiedState: StateFlow<UnifiedTimerState> = _unifiedState.asStateFlow()
    
    // Active workout
    private var activeComplexWorkout: ComplexWorkout? = null
    
    // Coroutine scope for state observation
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    enum class WorkoutMode {
        SIMPLE,
        COMPLEX
    }
    
    init {
        // Observe simple timer state changes
        scope.launch {
            simpleTimerManager.timerStatus.collect { status ->
                if (_workoutMode.value == WorkoutMode.SIMPLE) {
                    _unifiedState.value = UnifiedTimerState.fromSimpleTimer(status)
                }
            }
        }
        
        // Observe complex timer state changes
        scope.launch {
            complexTimerManager.timerState.collect { state ->
                if (_workoutMode.value == WorkoutMode.COMPLEX) {
                    _unifiedState.value = UnifiedTimerState.fromComplexTimer(
                        state, 
                        activeComplexWorkout
                    )
                }
            }
        }
    }
    
    /**
     * Start a simple workout
     */
    fun startSimpleWorkout(
        config: TimerConfig, 
        presetId: String? = null, 
        presetName: String = "Custom Workout", 
        exerciseName: String? = null
    ) {
        try {
            _workoutMode.value = WorkoutMode.SIMPLE
            simpleTimerManager.start(config, presetId, presetName, exerciseName)
            
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Started simple workout: $presetName")
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Failed to start simple workout", e)
            throw e
        }
    }
    
    /**
     * Start a complex workout
     */
    suspend fun startComplexWorkout(workoutId: String) {
        try {
            val workout = complexWorkoutRepository?.getWorkout(workoutId)
                ?: throw IllegalArgumentException("Workout not found: $workoutId")
            
            _workoutMode.value = WorkoutMode.COMPLEX
            activeComplexWorkout = workout
            complexTimerManager.startWorkout(workout)
            
            // Mark workout as used
            complexWorkoutRepository.markWorkoutUsed(workoutId)
            
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Started complex workout: ${workout.name}")
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Failed to start complex workout", e)
            throw e
        }
    }
    
    /**
     * Start a complex workout directly
     */
    fun startComplexWorkoutDirect(workout: ComplexWorkout) {
        try {
            _workoutMode.value = WorkoutMode.COMPLEX
            activeComplexWorkout = workout
            complexTimerManager.startWorkout(workout)
            
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Started complex workout directly: ${workout.name}")
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Failed to start complex workout", e)
            throw e
        }
    }
    
    /**
     * Pause the current workout
     */
    fun pause() {
        when (_workoutMode.value) {
            WorkoutMode.SIMPLE -> simpleTimerManager.pause()
            WorkoutMode.COMPLEX -> complexTimerManager.pause()
        }
    }
    
    /**
     * Resume the current workout
     */
    fun resume() {
        when (_workoutMode.value) {
            WorkoutMode.SIMPLE -> simpleTimerManager.resume()
            WorkoutMode.COMPLEX -> complexTimerManager.resume()
        }
    }
    
    /**
     * Reset the current workout
     */
    fun reset() {
        when (_workoutMode.value) {
            WorkoutMode.SIMPLE -> simpleTimerManager.reset()
            WorkoutMode.COMPLEX -> complexTimerManager.reset()
        }
        activeComplexWorkout = null
        _unifiedState.value = UnifiedTimerState()
    }
    
    /**
     * Update configuration for simple workouts
     */
    fun updateConfig(config: TimerConfig) {
        if (_workoutMode.value == WorkoutMode.SIMPLE) {
            simpleTimerManager.updateConfig(config)
        }
    }
    
    /**
     * Mark rep completed for complex workouts
     */
    fun markRepCompleted() {
        if (_workoutMode.value == WorkoutMode.COMPLEX) {
            complexTimerManager.markRepCompleted()
        }
    }
    
    /**
     * Skip to next exercise for complex workouts
     */
    fun skipToNext() {
        if (_workoutMode.value == WorkoutMode.COMPLEX) {
            complexTimerManager.skipToNext()
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        simpleTimerManager.cleanup()
        complexTimerManager.cleanup()
    }
}

/**
 * Unified timer state that works for both simple and complex workouts
 */
data class UnifiedTimerState(
    // Common fields
    val timerState: TimerState = TimerState.STOPPED,
    val displayTime: String = "00:00",
    val timeRemainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val canStart: Boolean = true,
    val canPause: Boolean = false,
    val canResume: Boolean = false,
    val canReset: Boolean = false,
    
    // Simple workout fields
    val currentRound: Int = 1,
    val totalRounds: Int = 1,
    val intervalType: IntervalType = IntervalType.WORK,
    val isUnlimited: Boolean = false,
    
    // Complex workout fields
    val currentPhaseName: String? = null,
    val currentExerciseName: String? = null,
    val currentPhaseIndex: Int = 0,
    val totalPhases: Int = 0,
    val statusText: String = "",
    val progressText: String = "",
    val exerciseMode: WorkoutMode? = null,
    val needsRepInput: Boolean = false,
    val amrapRounds: Int = 0,
    
    // UI hints
    val backgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    val shouldFlash: Boolean = false,
    val countdownText: String? = null
) {
    companion object {
        /**
         * Create unified state from simple timer status
         */
        fun fromSimpleTimer(status: TimerStatus): UnifiedTimerState {
            val displayTime = when {
                // BEGIN countdown - show countdown seconds
                status.state == TimerState.BEGIN -> {
                    status.timeRemainingSeconds.toString()
                }
                else -> {
                    val minutes = status.timeRemainingSeconds / 60
                    val seconds = status.timeRemainingSeconds % 60
                    String.format("%02d:%02d", minutes, seconds)
                }
            }
            
            return UnifiedTimerState(
                timerState = status.state,
                displayTime = displayTime,
                timeRemainingSeconds = status.timeRemainingSeconds,
                isRunning = status.state == TimerState.RUNNING,
                canStart = status.canStart,
                canPause = status.canPause,
                canResume = status.canResume,
                canReset = status.canReset,
                currentRound = status.currentRound,
                totalRounds = status.config.totalRounds,
                intervalType = status.currentInterval,
                isUnlimited = status.config.isUnlimited,
                statusText = when {
                    status.state == TimerState.BEGIN -> "Get Ready!"
                    status.currentInterval == IntervalType.WORK -> "Work"
                    status.currentInterval == IntervalType.REST -> "Rest"
                    else -> ""
                },
                progressText = if (status.config.isUnlimited) {
                    "Round ${status.currentRound}"
                } else {
                    "Round ${status.currentRound} of ${status.config.totalRounds}"
                },
                backgroundColor = when {
                    status.state == TimerState.BEGIN -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
                    status.currentInterval == IntervalType.WORK -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                    status.currentInterval == IntervalType.REST -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
                    else -> androidx.compose.ui.graphics.Color.Transparent
                },
                shouldFlash = status.shouldFlashBlue,
                countdownText = status.countdownText
            )
        }
        
        /**
         * Create unified state from complex timer
         */
        fun fromComplexTimer(state: ComplexTimerState, workout: ComplexWorkout?): UnifiedTimerState {
            if (workout == null) {
                return UnifiedTimerState()
            }
            
            val phase = state.getCurrentPhase(workout)
            val exercise = state.getCurrentExercise(workout)
            val remaining = state.getRemainingSeconds(workout)
            
            val displayTime = when {
                // BEGIN countdown - show countdown seconds
                state.timerState == TimerState.BEGIN -> {
                    state.currentIntervalSeconds.toString()
                }
                remaining != null -> {
                    val minutes = remaining / 60
                    val seconds = remaining % 60
                    String.format("%02d:%02d", minutes, seconds)
                }
                exercise?.mode == WorkoutMode.REP_BASED -> {
                    "${state.completedReps}/${exercise.targetReps}"
                }
                else -> "--:--"
            }
            
            return UnifiedTimerState(
                timerState = state.timerState,
                displayTime = displayTime,
                timeRemainingSeconds = remaining ?: 0,
                isRunning = state.timerState == TimerState.RUNNING,
                canStart = state.timerState == TimerState.STOPPED || state.timerState == TimerState.FINISHED,
                canPause = state.timerState == TimerState.RUNNING || state.timerState == TimerState.BEGIN,
                canResume = state.timerState == TimerState.PAUSED,
                canReset = state.timerState != TimerState.STOPPED,
                currentPhaseName = phase?.name,
                currentExerciseName = exercise?.name,
                currentPhaseIndex = state.currentPhaseIndex,
                totalPhases = workout.phases.size,
                statusText = state.getStatusText(workout),
                progressText = "Phase ${state.currentPhaseIndex + 1} of ${workout.phases.size}",
                exerciseMode = exercise?.mode,
                needsRepInput = exercise?.mode == WorkoutMode.REP_BASED || 
                               exercise?.mode == WorkoutMode.FOR_TIME,
                amrapRounds = state.amrapRoundsCompleted,
                backgroundColor = when {
                    state.isInRestPeriod -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
                    exercise?.mode == WorkoutMode.STATIC_HOLD -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                    else -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                },
                shouldFlash = false,
                countdownText = if (state.timerState == TimerState.BEGIN) {
                    "Starting in ${state.currentIntervalSeconds}"
                } else null
            )
        }
    }
}