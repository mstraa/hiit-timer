package com.hiittimer.app.data

import kotlinx.serialization.Serializable
import java.util.UUID
import java.util.Date

/**
 * Represents a complete multi-phase workout
 */
@Serializable
data class ComplexWorkout(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val phases: List<WorkoutPhase>,
    
    // Metadata
    val createdAt: Long = Date().time,
    val lastUsedAt: Long? = null,
    val usageCount: Int = 0,
    
    // Optional categorization
    val tags: List<String> = emptyList(),
    val difficulty: WorkoutDifficulty = WorkoutDifficulty.INTERMEDIATE,
    
    // Creator info (for sharing)
    val creatorName: String? = null,
    
    // Compatibility flag for simple timer mode
    val isSimpleWorkout: Boolean = false
) {
    /**
     * Calculate total workout duration
     */
    fun getTotalDuration(): Int {
        return phases.sumOf { it.getTotalDuration() }
    }
    
    /**
     * Get total duration in minutes (rounded up)
     */
    fun getTotalDurationMinutes(): Int {
        return (getTotalDuration() + 59) / 60
    }
    
    /**
     * Count total exercises across all phases
     */
    fun getTotalExerciseCount(): Int {
        return phases.sumOf { it.getTotalExerciseCount() }
    }
    
    /**
     * Get all unique exercise categories used
     */
    fun getExerciseCategories(): Set<ExerciseCategory> {
        return phases.flatMap { phase ->
            phase.exercises.map { it.category }
        }.toSet()
    }
    
    /**
     * Get all unique equipment needed
     */
    fun getRequiredEquipment(): Set<String> {
        return phases.flatMap { phase ->
            phase.exercises.mapNotNull { it.equipment }
        }.toSet()
    }
    
    /**
     * Validate the entire workout
     */
    fun validate(): ValidationResult {
        if (phases.isEmpty()) {
            return ValidationResult(false, "Workout must have at least one phase")
        }
        
        // Validate each phase
        phases.forEach { phase ->
            val phaseValidation = phase.validate()
            if (!phaseValidation.isValid) {
                return ValidationResult(false, "Phase '${phase.name}': ${phaseValidation.errorMessage}")
            }
        }
        
        // Check for reasonable total duration (max 3 hours)
        if (getTotalDuration() > 10800) {
            return ValidationResult(false, "Workout duration exceeds 3 hours")
        }
        
        return ValidationResult(true)
    }
    
    /**
     * Create a simple workout from legacy TimerConfig
     */
    companion object {
        fun fromTimerConfig(config: TimerConfig, name: String = "Simple Workout"): ComplexWorkout {
            val exercise = Exercise(
                name = "Work Interval",
                mode = WorkoutMode.TIME_BASED,
                durationSeconds = config.workTimeSeconds,
                restSeconds = if (config.noRest) 0 else config.restTimeSeconds
            )
            
            val phase = WorkoutPhase(
                name = "Main",
                type = PhaseType.CUSTOM,
                exercises = listOf(exercise),
                rounds = if (config.isUnlimited) 999 else config.totalRounds
            )
            
            return ComplexWorkout(
                name = name,
                phases = listOf(phase),
                isSimpleWorkout = true
            )
        }
    }
}

/**
 * Workout difficulty levels
 */
@Serializable
enum class WorkoutDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ELITE
}

/**
 * Pre-built workout templates
 */
object WorkoutTemplates {
    /**
     * Classic CrossFit-style WOD
     */
    fun crossfitWod() = ComplexWorkout(
        name = "CrossFit WOD",
        description = "45-minute full CrossFit workout with warm-up, strength, and AMRAP",
        phases = listOf(
            PhaseTemplates.warmUp(),
            PhaseTemplates.strength(),
            PhaseTemplates.amrapWod(15),
            PhaseTemplates.coolDown()
        ),
        tags = listOf("crossfit", "strength", "cardio"),
        difficulty = WorkoutDifficulty.ADVANCED
    )
    
    /**
     * HIIT Circuit Training
     */
    fun hiitCircuit() = ComplexWorkout(
        name = "HIIT Circuit",
        description = "30-minute high-intensity circuit training",
        phases = listOf(
            WorkoutPhase(
                name = "Warm-up",
                type = PhaseType.WARM_UP,
                exercises = listOf(
                    Exercise(name = "Jumping Jacks", category = ExerciseCategory.CARDIO, mode = WorkoutMode.TIME_BASED, durationSeconds = 60),
                    Exercise(name = "High Knees", category = ExerciseCategory.CARDIO, mode = WorkoutMode.TIME_BASED, durationSeconds = 60),
                    Exercise(name = "Dynamic Stretching", category = ExerciseCategory.MOBILITY, mode = WorkoutMode.TIME_BASED, durationSeconds = 120)
                )
            ),
            WorkoutPhase(
                name = "Circuit",
                type = PhaseType.MAIN_WOD,
                exercises = listOf(
                    Exercise(name = "Burpees", category = ExerciseCategory.PLYOMETRIC, mode = WorkoutMode.TIME_BASED, durationSeconds = 45, restSeconds = 15),
                    Exercise(name = "Mountain Climbers", category = ExerciseCategory.CARDIO, mode = WorkoutMode.TIME_BASED, durationSeconds = 45, restSeconds = 15),
                    Exercise(name = "Squat Jumps", category = ExerciseCategory.PLYOMETRIC, mode = WorkoutMode.TIME_BASED, durationSeconds = 45, restSeconds = 15),
                    Exercise(name = "Push-ups", category = ExerciseCategory.STRENGTH, mode = WorkoutMode.TIME_BASED, durationSeconds = 45, restSeconds = 15)
                ),
                rounds = 4,
                restBetweenRounds = 60
            ),
            PhaseTemplates.coolDown()
        ),
        tags = listOf("hiit", "bodyweight", "cardio"),
        difficulty = WorkoutDifficulty.INTERMEDIATE
    )
    
    /**
     * Tabata Protocol
     */
    fun tabataWorkout() = ComplexWorkout(
        name = "Tabata Training",
        description = "Classic Tabata protocol with multiple exercises",
        phases = listOf(
            WorkoutPhase(
                name = "Warm-up",
                type = PhaseType.WARM_UP,
                exercises = listOf(
                    Exercise(name = "Light Jogging", category = ExerciseCategory.CARDIO, mode = WorkoutMode.TIME_BASED, durationSeconds = 180)
                )
            ),
            WorkoutPhase(
                name = "Tabata Block 1",
                type = PhaseType.MAIN_WOD,
                exercises = listOf(
                    Exercise(name = "Sprint", category = ExerciseCategory.CARDIO, mode = WorkoutMode.TABATA)
                )
            ),
            WorkoutPhase(
                name = "Recovery",
                type = PhaseType.CUSTOM,
                exercises = listOf(
                    Exercise(name = "Walk", category = ExerciseCategory.CARDIO, mode = WorkoutMode.TIME_BASED, durationSeconds = 120)
                )
            ),
            WorkoutPhase(
                name = "Tabata Block 2",
                type = PhaseType.MAIN_WOD,
                exercises = listOf(
                    Exercise(name = "Burpees", category = ExerciseCategory.PLYOMETRIC, mode = WorkoutMode.TABATA)
                )
            ),
            PhaseTemplates.coolDown()
        ),
        tags = listOf("tabata", "hiit", "cardio"),
        difficulty = WorkoutDifficulty.ADVANCED
    )
}