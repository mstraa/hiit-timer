package com.hiittimer.app.data

import android.content.Context
import com.hiittimer.app.error.ErrorHandler
import com.hiittimer.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Repository for managing complex workouts
 */
class ComplexWorkoutRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    private val workoutsFile = File(context.filesDir, "complex_workouts.json")
    private val _workouts = MutableStateFlow<List<ComplexWorkout>>(emptyList())
    val workouts: Flow<List<ComplexWorkout>> = _workouts.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val MAX_WORKOUTS = 100
    }
    
    init {
        loadWorkouts()
    }
    
    /**
     * Load workouts from storage
     */
    private fun loadWorkouts() {
        try {
            if (workoutsFile.exists()) {
                val jsonString = workoutsFile.readText()
                val workoutList = json.decodeFromString<List<ComplexWorkout>>(jsonString)
                _workouts.value = workoutList
                Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                    "Loaded ${workoutList.size} complex workouts")
            } else {
                // Initialize with default templates
                val defaultWorkouts = listOf(
                    WorkoutTemplates.crossfitWod(),
                    WorkoutTemplates.hiitCircuit(),
                    WorkoutTemplates.tabataWorkout()
                )
                _workouts.value = defaultWorkouts
                // Save workouts in a coroutine
                scope.launch {
                    saveWorkouts()
                }
                Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                    "Initialized with default workout templates")
            }
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Failed to load complex workouts", e)
            _workouts.value = emptyList()
        }
    }
    
    /**
     * Save workouts to storage
     */
    private suspend fun saveWorkouts() = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(_workouts.value)
            workoutsFile.writeText(jsonString)
            Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Saved ${_workouts.value.size} complex workouts")
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Failed to save complex workouts", e)
            throw ErrorHandler.DatabaseException.DatabaseConnectionError(
                "Failed to save workouts", e)
        }
    }
    
    /**
     * Get workout by ID
     */
    suspend fun getWorkout(id: String): ComplexWorkout? = withContext(Dispatchers.IO) {
        _workouts.value.find { it.id == id }
    }
    
    /**
     * Save a new workout
     */
    suspend fun saveWorkout(workout: ComplexWorkout): Result<ComplexWorkout> = withContext(Dispatchers.IO) {
        try {
            val currentWorkouts = _workouts.value.toMutableList()
            
            // Check limit
            if (currentWorkouts.size >= MAX_WORKOUTS) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Maximum number of workouts ($MAX_WORKOUTS) reached"
                    )
                )
            }
            
            // Validate workout
            val validation = workout.validate()
            if (!validation.isValid) {
                return@withContext Result.failure(
                    IllegalArgumentException(validation.errorMessage)
                )
            }
            
            // Add or update workout
            val existingIndex = currentWorkouts.indexOfFirst { it.id == workout.id }
            if (existingIndex >= 0) {
                currentWorkouts[existingIndex] = workout
            } else {
                currentWorkouts.add(workout)
            }
            
            _workouts.value = currentWorkouts
            saveWorkouts()
            
            Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Saved workout: ${workout.name}")
            
            Result.success(workout)
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Failed to save workout", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update workout usage
     */
    suspend fun markWorkoutUsed(workoutId: String) = withContext(Dispatchers.IO) {
        try {
            val currentWorkouts = _workouts.value.toMutableList()
            val index = currentWorkouts.indexOfFirst { it.id == workoutId }
            
            if (index >= 0) {
                val workout = currentWorkouts[index]
                currentWorkouts[index] = workout.copy(
                    lastUsedAt = System.currentTimeMillis(),
                    usageCount = workout.usageCount + 1
                )
                
                _workouts.value = currentWorkouts
                saveWorkouts()
            }
        } catch (e: Exception) {
            Logger.w(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Failed to update workout usage", e)
        }
    }
    
    /**
     * Delete a workout
     */
    suspend fun deleteWorkout(workoutId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentWorkouts = _workouts.value.toMutableList()
            val removed = currentWorkouts.removeAll { it.id == workoutId }
            
            if (removed) {
                _workouts.value = currentWorkouts
                saveWorkouts()
                Logger.d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                    "Deleted workout: $workoutId")
                Result.success(Unit)
            } else {
                Result.failure(
                    NoSuchElementException("Workout not found")
                )
            }
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Failed to delete workout", e)
            Result.failure(e)
        }
    }
    
    /**
     * Import workout from JSON string
     */
    suspend fun importWorkout(jsonString: String): Result<ComplexWorkout> = withContext(Dispatchers.IO) {
        try {
            val workout = json.decodeFromString<ComplexWorkout>(jsonString)
            
            // Generate new ID to avoid conflicts
            val importedWorkout = workout.copy(
                id = java.util.UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                lastUsedAt = null,
                usageCount = 0
            )
            
            saveWorkout(importedWorkout)
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Failed to import workout", e)
            Result.failure(e)
        }
    }
    
    /**
     * Export workout to JSON string
     */
    suspend fun exportWorkout(workoutId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val workout = getWorkout(workoutId)
            if (workout != null) {
                val jsonString = json.encodeToString(workout)
                Result.success(jsonString)
            } else {
                Result.failure(
                    NoSuchElementException("Workout not found")
                )
            }
        } catch (e: Exception) {
            Logger.e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, 
                "Failed to export workout", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get workouts by category
     */
    fun getWorkoutsByCategory(category: ExerciseCategory): List<ComplexWorkout> {
        return _workouts.value.filter { workout ->
            workout.getExerciseCategories().contains(category)
        }
    }
    
    /**
     * Get workouts by difficulty
     */
    fun getWorkoutsByDifficulty(difficulty: WorkoutDifficulty): List<ComplexWorkout> {
        return _workouts.value.filter { it.difficulty == difficulty }
    }
    
    /**
     * Search workouts by name or tag
     */
    fun searchWorkouts(query: String): List<ComplexWorkout> {
        val lowercaseQuery = query.lowercase()
        return _workouts.value.filter { workout ->
            workout.name.lowercase().contains(lowercaseQuery) ||
            workout.description.lowercase().contains(lowercaseQuery) ||
            workout.tags.any { it.lowercase().contains(lowercaseQuery) }
        }
    }
}