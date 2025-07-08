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
    val lastUsed: Long? = null
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
}

/**
 * In-memory implementation of PresetRepository for development/testing
 */
class InMemoryPresetRepository : PresetRepository {
    private val presets = mutableMapOf<String, Preset>()

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
}
