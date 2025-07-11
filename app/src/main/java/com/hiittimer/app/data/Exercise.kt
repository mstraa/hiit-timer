package com.hiittimer.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single exercise within a workout
 */
@Serializable
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.CUSTOM,
    val description: String = "",
    
    // Timing configuration based on mode
    val mode: WorkoutMode = WorkoutMode.TIME_BASED,
    
    // For TIME_BASED, STATIC_HOLD
    val durationSeconds: Int = 30,
    
    // For REP_BASED, FOR_TIME
    val targetReps: Int = 10,
    
    // For REP_BASED (number of sets)
    val sets: Int = 3,
    
    // Rest duration after this exercise
    val restSeconds: Int = 10,
    
    // For EMOM - work window within the minute
    val emomWorkWindowSeconds: Int = 40,
    
    // Optional: equipment needed
    val equipment: String? = null,
    
    // Optional: weight/resistance information
    val weight: String? = null,
    
    // Optional: notes or coaching cues
    val notes: String? = null
) {
    /**
     * Calculate total duration for this exercise including rest
     */
    fun getTotalDuration(): Int {
        return when (mode) {
            WorkoutMode.TIME_BASED, WorkoutMode.STATIC_HOLD -> {
                durationSeconds + restSeconds
            }
            WorkoutMode.REP_BASED -> {
                // Estimate based on sets and rest
                val estimatedWorkTime = 30 // seconds per set estimate
                (sets * estimatedWorkTime) + ((sets - 1) * restSeconds)
            }
            WorkoutMode.EMOM -> {
                60 // Always 1 minute for EMOM
            }
            WorkoutMode.TABATA -> {
                8 * 30 // 8 rounds of 20s work + 10s rest
            }
            WorkoutMode.AMRAP, WorkoutMode.FOR_TIME -> {
                durationSeconds // User-defined duration
            }
        }
    }
    
    /**
     * Get display text for the exercise based on its mode
     */
    fun getDisplayText(): String {
        return when (mode) {
            WorkoutMode.TIME_BASED -> "$name - ${durationSeconds}s"
            WorkoutMode.REP_BASED -> "$name - $targetReps reps x $sets sets"
            WorkoutMode.AMRAP -> "$name - AMRAP ${durationSeconds}s"
            WorkoutMode.EMOM -> "$name - EMOM"
            WorkoutMode.TABATA -> "$name - Tabata"
            WorkoutMode.FOR_TIME -> "$name - For Time: $targetReps reps"
            WorkoutMode.STATIC_HOLD -> "$name - Hold ${durationSeconds}s"
        }
    }
}

/**
 * Common exercise templates
 */
object ExerciseTemplates {
    val burpees = Exercise(
        name = "Burpees",
        category = ExerciseCategory.PLYOMETRIC,
        mode = WorkoutMode.REP_BASED,
        targetReps = 10,
        sets = 3,
        restSeconds = 30
    )
    
    val plank = Exercise(
        name = "Plank",
        category = ExerciseCategory.CORE,
        mode = WorkoutMode.STATIC_HOLD,
        durationSeconds = 60,
        restSeconds = 20
    )
    
    val rowing = Exercise(
        name = "Rowing",
        category = ExerciseCategory.CARDIO,
        mode = WorkoutMode.TIME_BASED,
        durationSeconds = 300, // 5 minutes
        restSeconds = 0,
        equipment = "Rowing Machine"
    )
    
    val backSquat = Exercise(
        name = "Back Squat",
        category = ExerciseCategory.STRENGTH,
        mode = WorkoutMode.REP_BASED,
        targetReps = 5,
        sets = 3,
        restSeconds = 120,
        equipment = "Barbell"
    )
}