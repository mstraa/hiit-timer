package com.hiittimer.app.timer

import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import com.hiittimer.app.error.ErrorHandler
import com.hiittimer.app.utils.Logger
import com.hiittimer.app.performance.PerformanceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for complex multi-phase workouts
 * Handles different exercise modes (TIME_BASED, REP_BASED, AMRAP, EMOM, etc.)
 * and transitions between phases and exercises
 */
class ComplexTimerManager(
    private val audioManager: AudioManager? = null,
    private val workoutHistoryRepository: WorkoutHistoryRepository? = null,
    private val performanceManager: PerformanceManager? = null
) {
    private val _timerState = MutableStateFlow(ComplexTimerState(workoutId = ""))
    val timerState: StateFlow<ComplexTimerState> = _timerState.asStateFlow()
    
    private val _currentWorkout = MutableStateFlow<ComplexWorkout?>(null)
    val currentWorkout: StateFlow<ComplexWorkout?> = _currentWorkout.asStateFlow()
    
    private var timerJob: Job? = null
    private val scope = CoroutineScope(
        Dispatchers.Main +
        SupervisorJob() +
        CoroutineName("ComplexTimerManager")
    )
    
    // Session tracking
    private var sessionStartTime: Long = 0
    private var pausedDurationMs: Long = 0
    private var lastPauseTime: Long = 0
    
    /**
     * Start a complex workout
     */
    fun startWorkout(workout: ComplexWorkout) {
        try {
            // Validate workout
            val validation = workout.validate()
            if (!validation.isValid) {
                throw ErrorHandler.TimerException.TimerConfigurationError(
                    validation.errorMessage ?: "Invalid workout configuration"
                )
            }
            
            Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Starting complex workout: ${workout.name}")
            
            _currentWorkout.value = workout
            sessionStartTime = System.currentTimeMillis()
            
            // Initialize timer state
            _timerState.value = ComplexTimerState(
                workoutId = workout.id,
                timerState = TimerState.BEGIN,
                startTimeMillis = sessionStartTime,
                soundEnabled = audioManager != null
            )
            
            // Start countdown
            startBeginCountdown()
            
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Failed to start complex workout", e)
            reset()
            throw e
        }
    }
    
    /**
     * Pause the workout
     */
    fun pause() {
        val currentState = _timerState.value
        
        if (currentState.timerState != TimerState.RUNNING) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Cannot pause from state: ${currentState.timerState}")
            return
        }
        
        timerJob?.cancel()
        lastPauseTime = System.currentTimeMillis()
        
        _timerState.value = currentState.withTimerState(TimerState.PAUSED)
        
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Complex workout paused")
    }
    
    /**
     * Resume the workout
     */
    fun resume() {
        val currentState = _timerState.value
        
        if (currentState.timerState != TimerState.PAUSED) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Cannot resume from state: ${currentState.timerState}")
            return
        }
        
        // Track pause duration
        if (lastPauseTime > 0) {
            pausedDurationMs += System.currentTimeMillis() - lastPauseTime
            lastPauseTime = 0
        }
        
        _timerState.value = currentState.withTimerState(TimerState.RUNNING)
        
        startTimerCountdown()
        
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Complex workout resumed")
    }
    
    /**
     * Reset the workout
     */
    fun reset() {
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Resetting complex workout")
        
        timerJob?.cancel()
        
        // Save session if in progress
        if (sessionStartTime > 0) {
            saveWorkoutSession()
        }
        
        // Reset state
        _timerState.value = ComplexTimerState(workoutId = "")
        _currentWorkout.value = null
        sessionStartTime = 0
        pausedDurationMs = 0
        lastPauseTime = 0
    }
    
    /**
     * Mark current rep as completed (for rep-based exercises)
     */
    fun markRepCompleted() {
        val currentState = _timerState.value
        val workout = _currentWorkout.value ?: return
        val exercise = currentState.getCurrentExercise(workout) ?: return
        
        if (exercise.mode != WorkoutMode.REP_BASED && 
            exercise.mode != WorkoutMode.FOR_TIME) {
            return
        }
        
        val newState = currentState.markRepCompleted()
        _timerState.value = newState
        
        // Check if all reps completed
        if (newState.completedReps >= exercise.targetReps) {
            // Auto-advance to next set or exercise
            advanceToNext()
        }
    }
    
    /**
     * Skip to next exercise
     */
    fun skipToNext() {
        advanceToNext()
    }
    
    // === Private Implementation Methods ===
    
    private fun startBeginCountdown() {
        timerJob = scope.launch {
            try {
                // 5-second countdown
                for (i in 5 downTo 1) {
                    _timerState.value = _timerState.value.copy(
                        elapsedSeconds = 5 - i,
                        currentIntervalSeconds = i
                    )
                    
                    audioManager?.playCountdownBeep()
                    delay(1000)
                }
                
                // Start workout
                _timerState.value = _timerState.value.withTimerState(TimerState.RUNNING)
                audioManager?.playWorkIntervalSound()
                
                startTimerCountdown()
                
            } catch (e: CancellationException) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Begin countdown cancelled")
            }
        }
    }
    
    private fun startTimerCountdown() {
        timerJob = scope.launch {
            try {
                while (isActive) {
                    val workout = _currentWorkout.value ?: break
                    val currentState = _timerState.value
                    
                    if (currentState.timerState != TimerState.RUNNING) break
                    if (currentState.isComplete(workout)) {
                        handleWorkoutComplete()
                        break
                    }
                    
                    // Use 1 second delay for complex workouts
                    delay(1000L)
                    
                    // Update timer
                    updateTimer()
                }
            } catch (e: CancellationException) {
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer cancelled")
            } catch (e: Exception) {
                Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer error", e)
                reset()
            }
        }
    }
    
    private fun updateTimer() {
        val workout = _currentWorkout.value ?: return
        val currentState = _timerState.value
        val exercise = currentState.getCurrentExercise(workout) ?: return
        val phase = currentState.getCurrentPhase(workout) ?: return
        
        // Update elapsed time by 1 second
        val newState = currentState.withElapsedTime(currentState.currentIntervalSeconds + 1)
        _timerState.value = newState
        
        // Check if current interval is complete
        when {
            // AMRAP phases run for fixed duration
            phase.amrapDurationSeconds != null -> {
                if (newState.currentIntervalSeconds >= phase.amrapDurationSeconds) {
                    advanceToNext()
                }
            }
            
            // Time-based exercises
            exercise.mode == WorkoutMode.TIME_BASED || 
            exercise.mode == WorkoutMode.STATIC_HOLD -> {
                val targetSeconds = if (newState.isInRestPeriod) {
                    exercise.restSeconds
                } else {
                    exercise.durationSeconds
                }
                
                if (newState.currentIntervalSeconds >= targetSeconds) {
                    advanceToNext()
                }
            }
            
            // EMOM - advance every minute
            exercise.mode == WorkoutMode.EMOM -> {
                if (newState.currentIntervalSeconds >= 60) {
                    advanceToNext()
                }
            }
            
            // Tabata - 20s work, 10s rest, 8 rounds
            exercise.mode == WorkoutMode.TABATA -> {
                val totalSeconds = 8 * 30 // 8 rounds of 30s each
                if (newState.currentIntervalSeconds >= totalSeconds) {
                    advanceToNext()
                } else {
                    // Play sound at interval transitions
                    val inInterval = newState.currentIntervalSeconds % 30
                    if (inInterval == 0 || inInterval == 20) {
                        playIntervalSound(inInterval == 0)
                    }
                }
            }
            
            // Rep-based exercises don't auto-advance
            exercise.mode == WorkoutMode.REP_BASED ||
            exercise.mode == WorkoutMode.FOR_TIME -> {
                // User must mark reps as completed
            }
        }
        
        // Handle countdown beeps
        val remaining = newState.getRemainingSeconds(workout)
        if (remaining != null && remaining in 1..3) {
            audioManager?.playCountdownBeep()
        }
    }
    
    private fun advanceToNext() {
        val workout = _currentWorkout.value ?: return
        val currentState = _timerState.value
        
        // Advance to next state
        val newState = currentState.advance(workout)
        _timerState.value = newState
        
        // Check if workout complete
        if (newState.isComplete(workout)) {
            handleWorkoutComplete()
        } else {
            // Play appropriate sound for new interval
            val exercise = newState.getCurrentExercise(workout)
            if (exercise != null) {
                playIntervalSound(!newState.isInRestPeriod)
            }
        }
    }
    
    private fun playIntervalSound(isWork: Boolean) {
        try {
            if (isWork) {
                audioManager?.playWorkIntervalSound()
            } else {
                audioManager?.playRestIntervalSound()
            }
        } catch (e: Exception) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                "Failed to play interval sound", e)
        }
    }
    
    private fun handleWorkoutComplete() {
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Complex workout completed")
        
        timerJob?.cancel()
        
        _timerState.value = _timerState.value.withTimerState(TimerState.FINISHED)
        
        audioManager?.playCompletionSound()
        saveWorkoutSession()
    }
    
    private fun saveWorkoutSession() {
        val workout = _currentWorkout.value ?: return
        val state = _timerState.value
        
        if (sessionStartTime == 0L || workoutHistoryRepository == null) return
        
        val endTime = System.currentTimeMillis()
        val totalDuration = endTime - sessionStartTime - pausedDurationMs
        
        // Create session from complex workout
        val session = WorkoutSession(
            id = java.util.UUID.randomUUID().toString(),
            date = java.util.Date(sessionStartTime),
            workoutName = workout.name,
            presetId = workout.id,
            presetName = workout.name,
            exerciseName = workout.phases.firstOrNull()?.exercises?.firstOrNull()?.name,
            completedRounds = state.amrapRoundsCompleted.takeIf { it > 0 } ?: state.completedExercises,
            totalRounds = workout.getTotalExerciseCount(),
            workTimeSeconds = 0, // Complex workouts don't have single work time
            restTimeSeconds = 0,
            totalDurationMs = totalDuration,
            actualWorkTimeMs = totalDuration - pausedDurationMs, // Approximate
            actualRestTimeMs = 0,
            completedPhases = state.completedPhases
        )
        
        scope.launch {
            try {
                workoutHistoryRepository.saveWorkoutSession(session)
                Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                    "Complex workout session saved")
            } catch (e: Exception) {
                Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                    "Failed to save workout session", e)
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        if (sessionStartTime > 0) {
            saveWorkoutSession()
        }
        
        timerJob?.cancel()
        scope.cancel()
    }
}