package com.hiittimer.app.data

import kotlinx.serialization.Serializable

/**
 * Tracks the current state and progress of a complex workout
 */
@Serializable
data class ComplexTimerState(
    // Reference to the workout being executed
    val workoutId: String,
    
    // Current position in workout
    val currentPhaseIndex: Int = 0,
    val currentExerciseIndex: Int = 0,
    val currentPhaseRound: Int = 1,
    
    // Timer state
    val timerState: TimerState = TimerState.STOPPED,
    val elapsedSeconds: Int = 0,
    val currentIntervalSeconds: Int = 0,
    
    // For time-based exercises
    val isInRestPeriod: Boolean = false,
    
    // For rep-based exercises
    val currentSet: Int = 1,
    val completedReps: Int = 0,
    
    // For AMRAP tracking
    val amrapRoundsCompleted: Int = 0,
    val amrapCurrentExerciseIndex: Int = 0,
    
    // Overall progress
    val totalElapsedSeconds: Int = 0,
    val completedPhases: List<String> = emptyList(),
    val completedExercises: Int = 0,
    
    // Performance tracking
    val startTimeMillis: Long? = null,
    val pausedDurationMillis: Long = 0,
    
    // Audio settings (inherit from current app settings)
    val soundEnabled: Boolean = true,
    val voiceCountdownEnabled: Boolean = true
) {
    /**
     * Check if workout is complete
     */
    fun isComplete(workout: ComplexWorkout): Boolean {
        return currentPhaseIndex >= workout.phases.size
    }
    
    /**
     * Get current phase
     */
    fun getCurrentPhase(workout: ComplexWorkout): WorkoutPhase? {
        return workout.phases.getOrNull(currentPhaseIndex)
    }
    
    /**
     * Get current exercise
     */
    fun getCurrentExercise(workout: ComplexWorkout): Exercise? {
        val phase = getCurrentPhase(workout) ?: return null
        
        return when {
            // For AMRAP phases, use amrap-specific index
            phase.amrapDurationSeconds != null -> {
                phase.exercises.getOrNull(amrapCurrentExerciseIndex)
            }
            // For regular phases
            else -> {
                phase.exercises.getOrNull(currentExerciseIndex)
            }
        }
    }
    
    /**
     * Get next exercise
     */
    fun getNextExercise(workout: ComplexWorkout): Exercise? {
        val phase = getCurrentPhase(workout) ?: return null
        
        return when {
            // For AMRAP phases, get next in rotation
            phase.amrapDurationSeconds != null -> {
                val nextIndex = (amrapCurrentExerciseIndex + 1) % phase.exercises.size
                phase.exercises.getOrNull(nextIndex)
            }
            // For regular phases
            else -> {
                // Check if there's a next exercise in current phase
                if (currentExerciseIndex + 1 < phase.exercises.size) {
                    phase.exercises.getOrNull(currentExerciseIndex + 1)
                } else if (currentPhaseRound < phase.rounds) {
                    // Next round of same phase, get first exercise
                    phase.exercises.firstOrNull()
                } else {
                    // Check next phase
                    val nextPhase = workout.phases.getOrNull(currentPhaseIndex + 1)
                    nextPhase?.exercises?.firstOrNull()
                }
            }
        }
    }
    
    /**
     * Calculate remaining time for current interval
     */
    fun getRemainingSeconds(workout: ComplexWorkout): Int? {
        val exercise = getCurrentExercise(workout) ?: return null
        val phase = getCurrentPhase(workout) ?: return null
        
        return when {
            // AMRAP phase - show remaining time for entire phase
            phase.amrapDurationSeconds != null -> {
                phase.amrapDurationSeconds - currentIntervalSeconds
            }
            // Time-based exercise
            exercise.mode == WorkoutMode.TIME_BASED || exercise.mode == WorkoutMode.STATIC_HOLD -> {
                val targetSeconds = if (isInRestPeriod) exercise.restSeconds else exercise.durationSeconds
                targetSeconds - currentIntervalSeconds
            }
            // EMOM - show time until next minute
            exercise.mode == WorkoutMode.EMOM -> {
                60 - (currentIntervalSeconds % 60)
            }
            // Tabata - show current interval time
            exercise.mode == WorkoutMode.TABATA -> {
                val inWork = (currentIntervalSeconds / 30) % 2 == 0
                val intervalTime = if (inWork) 20 else 10
                intervalTime - (currentIntervalSeconds % 30)
            }
            // Rep-based exercises don't have a timer
            else -> null
        }
    }
    
    /**
     * Get display text for current state
     */
    fun getStatusText(workout: ComplexWorkout): String {
        // Handle BEGIN phase
        if (timerState == TimerState.BEGIN) {
            return "Get Ready!"
        }
        
        val phase = getCurrentPhase(workout) ?: return "Workout Complete"
        val exercise = getCurrentExercise(workout) ?: return "Phase Complete"
        
        return when (exercise.mode) {
            WorkoutMode.TIME_BASED, WorkoutMode.STATIC_HOLD -> {
                if (isInRestPeriod) "Rest" else exercise.name
            }
            WorkoutMode.REP_BASED -> {
                "${exercise.name} - Set $currentSet of ${exercise.sets}"
            }
            WorkoutMode.AMRAP -> {
                "AMRAP - Round ${amrapRoundsCompleted + 1}"
            }
            WorkoutMode.EMOM -> {
                "${exercise.name} - EMOM"
            }
            WorkoutMode.TABATA -> {
                val round = (currentIntervalSeconds / 30) + 1
                "${exercise.name} - Tabata Round $round/8"
            }
            WorkoutMode.FOR_TIME -> {
                "${exercise.name} - ${exercise.targetReps} reps"
            }
        }
    }
    
    /**
     * Create next state after completing current interval/exercise
     */
    fun advance(workout: ComplexWorkout): ComplexTimerState {
        val phase = getCurrentPhase(workout) ?: return this
        val exercise = getCurrentExercise(workout) ?: return this
        
        return when {
            // Handle rest period completion
            isInRestPeriod -> {
                // Rest complete, move to next exercise or phase
                advanceToNextExercise(workout, phase)
            }
            
            // Handle AMRAP phase
            phase.amrapDurationSeconds != null -> {
                if (currentIntervalSeconds >= phase.amrapDurationSeconds) {
                    // AMRAP phase complete, move to next phase
                    advanceToNextPhase(workout)
                } else {
                    // Move to next exercise in AMRAP rotation
                    val nextExerciseIndex = (amrapCurrentExerciseIndex + 1) % phase.exercises.size
                    val roundsCompleted = if (nextExerciseIndex == 0) amrapRoundsCompleted + 1 else amrapRoundsCompleted
                    
                    copy(
                        amrapCurrentExerciseIndex = nextExerciseIndex,
                        amrapRoundsCompleted = roundsCompleted
                    )
                }
            }
            
            // Handle rep-based exercise
            exercise.mode == WorkoutMode.REP_BASED -> {
                if (currentSet < exercise.sets) {
                    // Move to rest between sets
                    copy(
                        isInRestPeriod = true,
                        currentIntervalSeconds = 0,
                        currentSet = currentSet + 1
                    )
                } else {
                    // Exercise complete, move to next
                    advanceToNextExercise(workout, phase)
                }
            }
            
            // Handle time-based exercise completion
            exercise.mode == WorkoutMode.TIME_BASED || exercise.mode == WorkoutMode.STATIC_HOLD -> {
                if (exercise.restSeconds > 0) {
                    // Move to rest period
                    copy(
                        isInRestPeriod = true,
                        currentIntervalSeconds = 0
                    )
                } else {
                    // No rest, move to next exercise
                    advanceToNextExercise(workout, phase)
                }
            }
            
            // Handle other modes
            else -> advanceToNextExercise(workout, phase)
        }
    }
    
    private fun advanceToNextExercise(workout: ComplexWorkout, phase: WorkoutPhase): ComplexTimerState {
        return if (currentExerciseIndex + 1 < phase.exercises.size) {
            // Move to next exercise in phase
            copy(
                currentExerciseIndex = currentExerciseIndex + 1,
                currentIntervalSeconds = 0,
                currentSet = 1,
                completedReps = 0,
                isInRestPeriod = false,
                completedExercises = completedExercises + 1
            )
        } else if (currentPhaseRound < phase.rounds) {
            // Start next round of current phase
            copy(
                currentPhaseRound = currentPhaseRound + 1,
                currentExerciseIndex = 0,
                currentIntervalSeconds = 0,
                currentSet = 1,
                completedReps = 0,
                isInRestPeriod = false,
                completedExercises = completedExercises + 1
            )
        } else {
            // Phase complete, move to next phase
            advanceToNextPhase(workout)
        }
    }
    
    private fun advanceToNextPhase(workout: ComplexWorkout): ComplexTimerState {
        val phase = getCurrentPhase(workout)
        return copy(
            currentPhaseIndex = currentPhaseIndex + 1,
            currentExerciseIndex = 0,
            currentPhaseRound = 1,
            currentIntervalSeconds = 0,
            currentSet = 1,
            completedReps = 0,
            isInRestPeriod = false,
            amrapRoundsCompleted = 0,
            amrapCurrentExerciseIndex = 0,
            completedPhases = if (phase != null) completedPhases + phase.id else completedPhases,
            completedExercises = completedExercises + 1
        )
    }
}

/**
 * Extension functions for state management
 */
fun ComplexTimerState.withTimerState(state: TimerState): ComplexTimerState = copy(timerState = state)

fun ComplexTimerState.withElapsedTime(seconds: Int): ComplexTimerState = copy(
    currentIntervalSeconds = seconds,
    totalElapsedSeconds = totalElapsedSeconds + 1
)

fun ComplexTimerState.markRepCompleted(): ComplexTimerState = copy(
    completedReps = completedReps + 1
)