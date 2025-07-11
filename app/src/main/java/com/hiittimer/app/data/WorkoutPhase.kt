package com.hiittimer.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a phase within a workout (e.g., warm-up, main WOD, cool-down)
 */
@Serializable
data class WorkoutPhase(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PhaseType,
    val exercises: List<Exercise>,
    
    // Phase-level configuration
    val rounds: Int = 1, // How many times to repeat this phase
    val restBetweenRounds: Int = 60, // Rest between phase rounds
    
    // For AMRAP phases
    val amrapDurationSeconds: Int? = null,
    
    // Phase instructions
    val instructions: String? = null
) {
    /**
     * Calculate total duration for this phase
     */
    fun getTotalDuration(): Int {
        val singleRoundDuration = when {
            // AMRAP phase has fixed duration
            amrapDurationSeconds != null -> amrapDurationSeconds
            
            // Sum up all exercise durations
            else -> exercises.sumOf { it.getTotalDuration() }
        }
        
        return (singleRoundDuration * rounds) + (restBetweenRounds * (rounds - 1).coerceAtLeast(0))
    }
    
    /**
     * Get total number of exercises accounting for rounds
     */
    fun getTotalExerciseCount(): Int {
        return exercises.size * rounds
    }
    
    /**
     * Validate phase configuration
     */
    fun validate(): ValidationResult {
        if (exercises.isEmpty()) {
            return ValidationResult(false, "Phase must have at least one exercise")
        }
        
        if (rounds < 1) {
            return ValidationResult(false, "Phase must have at least one round")
        }
        
        if (type == PhaseType.MAIN_WOD && exercises.any { it.mode == WorkoutMode.AMRAP } && amrapDurationSeconds == null) {
            return ValidationResult(false, "AMRAP phase must specify duration")
        }
        
        return ValidationResult(true)
    }
}

/**
 * Result of phase validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Common phase templates
 */
object PhaseTemplates {
    fun warmUp(exercises: List<Exercise> = listOf(
        ExerciseTemplates.rowing,
        Exercise(name = "Jump Rope", category = ExerciseCategory.CARDIO, mode = WorkoutMode.TIME_BASED, durationSeconds = 180),
        Exercise(name = "Arm Circles", category = ExerciseCategory.MOBILITY, mode = WorkoutMode.TIME_BASED, durationSeconds = 60),
        Exercise(name = "Leg Swings", category = ExerciseCategory.MOBILITY, mode = WorkoutMode.TIME_BASED, durationSeconds = 60),
        Exercise(name = "Hip Openers", category = ExerciseCategory.MOBILITY, mode = WorkoutMode.TIME_BASED, durationSeconds = 120)
    )) = WorkoutPhase(
        name = "Warm-up",
        type = PhaseType.WARM_UP,
        exercises = exercises,
        rounds = 1
    )
    
    fun strength(exercises: List<Exercise> = listOf(
        ExerciseTemplates.backSquat,
        Exercise(name = "Deadlift", category = ExerciseCategory.STRENGTH, mode = WorkoutMode.REP_BASED, targetReps = 3, sets = 3, restSeconds = 120)
    )) = WorkoutPhase(
        name = "Strength Work",
        type = PhaseType.STRENGTH,
        exercises = exercises,
        rounds = 1
    )
    
    fun amrapWod(
        durationMinutes: Int = 15,
        exercises: List<Exercise> = listOf(
            Exercise(name = "Burpees", category = ExerciseCategory.PLYOMETRIC, mode = WorkoutMode.REP_BASED, targetReps = 10),
            Exercise(name = "Kettlebell Swings", category = ExerciseCategory.STRENGTH, mode = WorkoutMode.REP_BASED, targetReps = 15),
            Exercise(name = "Box Jumps", category = ExerciseCategory.PLYOMETRIC, mode = WorkoutMode.REP_BASED, targetReps = 10),
            Exercise(name = "Pull-Ups", category = ExerciseCategory.GYMNASTICS, mode = WorkoutMode.REP_BASED, targetReps = 10)
        )
    ) = WorkoutPhase(
        name = "AMRAP $durationMinutes min",
        type = PhaseType.MAIN_WOD,
        exercises = exercises,
        rounds = 1,
        amrapDurationSeconds = durationMinutes * 60
    )
    
    fun coolDown(exercises: List<Exercise> = listOf(
        Exercise(name = "Static Stretching", category = ExerciseCategory.FLEXIBILITY, mode = WorkoutMode.STATIC_HOLD, durationSeconds = 180),
        Exercise(name = "Deep Breathing", category = ExerciseCategory.FLEXIBILITY, mode = WorkoutMode.TIME_BASED, durationSeconds = 120)
    )) = WorkoutPhase(
        name = "Cool Down",
        type = PhaseType.COOL_DOWN,
        exercises = exercises,
        rounds = 1
    )
}