package com.hiittimer.app.data

import java.util.UUID

/**
 * Data class representing a workout preset (FR-008: Preset System)
 */
data class Preset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val workTimeSeconds: Int,
    val restTimeSeconds: Int,
    val totalRounds: Int,
    val isUnlimited: Boolean = false,
    val noRest: Boolean = false,
    val exerciseName: String? = null, // FR-009: Exercise Descriptions
    val description: String? = null,  // FR-009: Exercise Descriptions (max 200 chars)
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long? = null,
    val isComplex: Boolean = false, // Flag to indicate if this preset has complex structure
    val complexPresetId: String? = null // Reference to ComplexPreset if this is a complex preset
) {
    init {
        require(name.isNotBlank()) { "Preset name cannot be blank" }
        require(name.length <= 50) { "Preset name cannot exceed 50 characters" }
        require(workTimeSeconds in 5..900) { "Work time must be between 5 and 900 seconds" }
        require(noRest || restTimeSeconds in 5..300) { "Rest time must be between 5 and 300 seconds when rest is enabled" }
        require(totalRounds in 1..99 || isUnlimited) { "Total rounds must be between 1 and 99 or unlimited" }
        require(description == null || description.length <= 200) { "Description cannot exceed 200 characters" }
        require(exerciseName == null || exerciseName.length <= 100) { "Exercise name cannot exceed 100 characters" }
    }

    /**
     * Convert preset to TimerConfig for use with timer
     */
    fun toTimerConfig(): TimerConfig {
        return TimerConfig(
            workTimeSeconds = workTimeSeconds,
            restTimeSeconds = restTimeSeconds,
            totalRounds = totalRounds,
            isUnlimited = isUnlimited,
            noRest = noRest
        )
    }

    /**
     * Create a copy with updated lastUsed timestamp
     */
    fun markAsUsed(): Preset {
        return copy(lastUsed = System.currentTimeMillis())
    }

    /**
     * Get display text for preset summary
     */
    fun getSummaryText(): String {
        val workTime = "${workTimeSeconds}s work"
        val restTime = if (noRest) "no rest" else "${restTimeSeconds}s rest"
        val rounds = if (isUnlimited) "unlimited rounds" else "$totalRounds rounds"
        return "$workTime, $restTime, $rounds"
    }
}

/**
 * Repository interface for preset management (FR-008)
 */
interface PresetRepository {
    suspend fun getAllPresets(): List<Preset>
    suspend fun getPresetById(id: String): Preset?
    suspend fun savePreset(preset: Preset)
    suspend fun updatePreset(preset: Preset)
    suspend fun deletePreset(id: String)
    suspend fun getRecentPresets(limit: Int = 5): List<Preset>
    suspend fun searchPresets(query: String): List<Preset>
    
    // Complex preset support
    suspend fun getAllComplexPresets(): List<ComplexPreset>
    suspend fun getComplexPresetById(id: String): ComplexPreset?
    suspend fun saveComplexPreset(preset: ComplexPreset)
    suspend fun updateComplexPreset(preset: ComplexPreset)
    suspend fun deleteComplexPreset(id: String)
}

/**
 * In-memory implementation of PresetRepository for development/testing
 */
class InMemoryPresetRepository : PresetRepository {
    private val presets = mutableMapOf<String, Preset>()
    private val complexPresets = mutableMapOf<String, ComplexPreset>()

    init {
        // Add some default presets
        val defaultPresets = listOf(
            Preset(
                name = "Quick HIIT",
                workTimeSeconds = 20,
                restTimeSeconds = 10,
                totalRounds = 8,
                exerciseName = "High Intensity Interval Training",
                description = "Classic 20/10 Tabata-style workout"
            ),
            Preset(
                name = "Strength Circuit",
                workTimeSeconds = 45,
                restTimeSeconds = 15,
                totalRounds = 6,
                exerciseName = "Strength Training",
                description = "Longer work intervals for strength exercises"
            ),
            Preset(
                name = "Cardio Blast",
                workTimeSeconds = 30,
                restTimeSeconds = 30,
                totalRounds = 10,
                exerciseName = "Cardio Training",
                description = "Equal work and rest for sustained cardio"
            ),
            Preset(
                name = "EMOM",
                workTimeSeconds = 60,
                restTimeSeconds = 0,
                totalRounds = 10,
                noRest = true,
                exerciseName = "Every Minute on the Minute",
                description = "Complete exercise every minute"
            )
        )
        
        defaultPresets.forEach { preset ->
            presets[preset.id] = preset
        }
        
        // Add a sample complex preset
        val complexPreset = ComplexPreset(
            name = "Circuit Training",
            description = "Full body circuit with different exercises",
            roundGroups = listOf(
                RoundGroup(
                    name = "Warm-up",
                    description = "Light cardio to prepare",
                    rounds = 1,
                    workPhases = listOf(
                        WorkPhase(name = "Jumping Jacks", durationSeconds = 30),
                        WorkPhase(name = "Arm Circles", durationSeconds = 20),
                        WorkPhase(name = "Leg Swings", durationSeconds = 20)
                    ),
                    restBetweenPhases = 10,
                    restBetweenRounds = 0,
                    specialRestAfterGroup = 60
                ),
                RoundGroup(
                    name = "Main Circuit",
                    description = "High intensity exercises",
                    rounds = 3,
                    workPhases = listOf(
                        WorkPhase(name = "Push-ups", description = "Standard or modified", durationSeconds = 45),
                        WorkPhase(name = "Squats", description = "Keep back straight", durationSeconds = 45),
                        WorkPhase(name = "Burpees", description = "Full body movement", durationSeconds = 30),
                        WorkPhase(name = "Plank", description = "Hold position", durationSeconds = 30)
                    ),
                    restBetweenPhases = 15,
                    restBetweenRounds = 60,
                    specialRestAfterGroup = 90
                ),
                RoundGroup(
                    name = "Cool-down",
                    description = "Stretching and recovery",
                    rounds = 1,
                    workPhases = listOf(
                        WorkPhase(name = "Forward Fold", durationSeconds = 30),
                        WorkPhase(name = "Quad Stretch", durationSeconds = 30),
                        WorkPhase(name = "Shoulder Stretch", durationSeconds = 30)
                    ),
                    restBetweenPhases = 5,
                    restBetweenRounds = 0,
                    specialRestAfterGroup = null
                )
            )
        )
        
        // Save the complex preset
        complexPresets[complexPreset.id] = complexPreset
        val simpleVersion = complexPreset.toSimplePreset()
        presets[simpleVersion.id] = simpleVersion
    }

    override suspend fun getAllPresets(): List<Preset> {
        return presets.values.sortedByDescending { it.lastUsed ?: it.createdAt }
    }

    override suspend fun getPresetById(id: String): Preset? {
        return presets[id]
    }

    override suspend fun savePreset(preset: Preset) {
        if (presets.size >= 50 && !presets.containsKey(preset.id)) {
            throw IllegalStateException("Maximum 50 presets allowed") // FR-008: Maximum 50 presets
        }
        presets[preset.id] = preset
    }

    override suspend fun updatePreset(preset: Preset) {
        if (presets.containsKey(preset.id)) {
            presets[preset.id] = preset
        } else {
            throw IllegalArgumentException("Preset not found: ${preset.id}")
        }
    }

    override suspend fun deletePreset(id: String) {
        presets.remove(id)
    }

    override suspend fun getRecentPresets(limit: Int): List<Preset> {
        return presets.values
            .filter { it.lastUsed != null }
            .sortedByDescending { it.lastUsed }
            .take(limit)
    }

    override suspend fun searchPresets(query: String): List<Preset> {
        val lowercaseQuery = query.lowercase()
        return presets.values.filter { preset ->
            preset.name.lowercase().contains(lowercaseQuery) ||
            preset.exerciseName?.lowercase()?.contains(lowercaseQuery) == true ||
            preset.description?.lowercase()?.contains(lowercaseQuery) == true
        }.sortedBy { it.name }
    }
    
    // Complex preset implementations
    override suspend fun getAllComplexPresets(): List<ComplexPreset> {
        return complexPresets.values.sortedByDescending { it.lastUsed ?: it.createdAt }
    }
    
    override suspend fun getComplexPresetById(id: String): ComplexPreset? {
        return complexPresets[id]
    }
    
    override suspend fun saveComplexPreset(preset: ComplexPreset) {
        if (complexPresets.size >= 20 && !complexPresets.containsKey(preset.id)) {
            throw IllegalStateException("Maximum 20 complex presets allowed")
        }
        complexPresets[preset.id] = preset
        
        // Also create a simple preset reference for compatibility
        val simplePreset = preset.toSimplePreset()
        presets[simplePreset.id] = simplePreset
    }
    
    override suspend fun updateComplexPreset(preset: ComplexPreset) {
        if (complexPresets.containsKey(preset.id)) {
            complexPresets[preset.id] = preset
            
            // Find and update the existing simple preset reference
            val existingSimplePreset = presets.values.find { it.complexPresetId == preset.id }
            if (existingSimplePreset != null) {
                // Update existing simple preset with new values
                val updatedSimplePreset = preset.toSimplePreset().copy(
                    id = existingSimplePreset.id // Keep the same ID
                )
                presets[existingSimplePreset.id] = updatedSimplePreset
            } else {
                // Create new simple preset if none exists
                val simplePreset = preset.toSimplePreset()
                presets[simplePreset.id] = simplePreset
            }
        } else {
            throw IllegalArgumentException("Complex preset not found: ${preset.id}")
        }
    }
    
    override suspend fun deleteComplexPreset(id: String) {
        complexPresets.remove(id)
        // Find and remove the simple preset reference by complexPresetId
        val simplePresetToDelete = presets.values.find { it.complexPresetId == id }
        simplePresetToDelete?.let { presets.remove(it.id) }
    }
}
