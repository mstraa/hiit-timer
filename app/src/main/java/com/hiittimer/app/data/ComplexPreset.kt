package com.hiittimer.app.data

import java.util.UUID

/**
 * Represents a single work phase within a round
 * Each work phase can have its own name and description
 */
data class WorkPhase(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val durationSeconds: Int
) {
    init {
        require(name.isNotBlank()) { "Work phase name cannot be blank" }
        require(name.length <= 50) { "Work phase name cannot exceed 50 characters" }
        require(description == null || description.length <= 200) { "Work phase description cannot exceed 200 characters" }
        require(durationSeconds in 5..900) { "Work phase duration must be between 5 and 900 seconds" }
    }
}

/**
 * Represents a group of rounds with its own configuration
 * Each round group can have multiple rounds with specific work/rest patterns
 */
data class RoundGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val rounds: Int,
    val workPhases: List<WorkPhase>,
    val restBetweenPhases: Int, // Rest between work phases within a round
    val restBetweenRounds: Int, // Rest between rounds within this group
    val specialRestAfterGroup: Int? = null // Special rest after this entire group completes
) {
    init {
        require(name.isNotBlank()) { "Round group name cannot be blank" }
        require(name.length <= 100) { "Round group name cannot exceed 100 characters" }
        require(description == null || description.length <= 200) { "Round group description cannot exceed 200 characters" }
        require(rounds in 1..99) { "Rounds must be between 1 and 99" }
        require(workPhases.isNotEmpty()) { "Round group must have at least one work phase" }
        require(workPhases.size <= 20) { "Round group cannot have more than 20 work phases" }
        require(restBetweenPhases in 0..300) { "Rest between phases must be between 0 and 300 seconds" }
        require(restBetweenRounds in 0..300) { "Rest between rounds must be between 0 and 300 seconds" }
        require(specialRestAfterGroup == null || specialRestAfterGroup in 5..600) { 
            "Special rest after group must be between 5 and 600 seconds" 
        }
    }
    
    /**
     * Calculate total work time for one round in this group
     */
    fun getTotalWorkTimePerRound(): Int {
        return workPhases.sumOf { it.durationSeconds }
    }
    
    /**
     * Calculate total rest time for one round in this group
     */
    fun getTotalRestTimePerRound(): Int {
        // Rest between phases (n-1 rests for n phases)
        return if (workPhases.size > 1) {
            restBetweenPhases * (workPhases.size - 1)
        } else {
            0
        }
    }
    
    /**
     * Calculate total time for this entire round group including all rounds and rests
     */
    fun getTotalGroupTime(): Int {
        val timePerRound = getTotalWorkTimePerRound() + getTotalRestTimePerRound()
        val totalRoundTime = timePerRound * rounds
        val totalRestBetweenRounds = if (rounds > 1) restBetweenRounds * (rounds - 1) else 0
        return totalRoundTime + totalRestBetweenRounds
    }
}

/**
 * Extended preset that supports complex workout structures with multiple round groups
 */
data class ComplexPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val roundGroups: List<RoundGroup>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long? = null
) {
    init {
        require(name.isNotBlank()) { "Complex preset name cannot be blank" }
        require(name.length <= 50) { "Complex preset name cannot exceed 50 characters" }
        require(description == null || description.length <= 200) { "Complex preset description cannot exceed 200 characters" }
        require(roundGroups.isNotEmpty()) { "Complex preset must have at least one round group" }
        require(roundGroups.size <= 10) { "Complex preset cannot have more than 10 round groups" }
    }
    
    /**
     * Calculate total workout time including all groups and rests
     */
    fun getTotalWorkoutTime(): Int {
        var totalTime = 0
        
        roundGroups.forEachIndexed { index, group ->
            totalTime += group.getTotalGroupTime()
            
            // Add special rest after group if it exists and it's not the last group
            if (index < roundGroups.size - 1 && group.specialRestAfterGroup != null) {
                totalTime += group.specialRestAfterGroup
            }
        }
        
        return totalTime
    }
    
    /**
     * Get total number of work intervals across all groups
     */
    fun getTotalWorkIntervals(): Int {
        return roundGroups.sumOf { group ->
            group.workPhases.size * group.rounds
        }
    }
    
    /**
     * Convert to a simple preset (uses first work phase from first group)
     * This is a fallback for compatibility
     */
    fun toSimplePreset(): Preset {
        val firstGroup = roundGroups.first()
        val firstPhase = firstGroup.workPhases.first()
        
        return Preset(
            id = UUID.randomUUID().toString(), // Generate new ID for simple preset reference
            name = name,
            workTimeSeconds = firstPhase.durationSeconds,
            restTimeSeconds = firstGroup.restBetweenPhases,
            totalRounds = roundGroups.sumOf { it.rounds },
            isUnlimited = false,
            noRest = firstGroup.restBetweenPhases == 0,
            exerciseName = firstPhase.name,
            description = description,
            createdAt = createdAt,
            lastUsed = lastUsed,
            isComplex = true,
            complexPresetId = id // Reference to this complex preset
        )
    }
    
    /**
     * Create a copy with updated lastUsed timestamp
     */
    fun markAsUsed(): ComplexPreset {
        return copy(lastUsed = System.currentTimeMillis())
    }
    
    /**
     * Get a summary of the workout structure
     */
    fun getStructureSummary(): String {
        return buildString {
            append("${roundGroups.size} group")
            if (roundGroups.size > 1) append("s")
            append(", ${getTotalWorkIntervals()} work intervals")
            append(", ${getTotalWorkoutTime() / 60}:${String.format("%02d", getTotalWorkoutTime() % 60)} total")
        }
    }
}

/**
 * Helper function to create a simple round group from basic timer config
 * Used for backwards compatibility
 */
fun TimerConfig.toRoundGroup(
    groupName: String = "Main Group",
    phaseName: String = "Work"
): RoundGroup {
    return RoundGroup(
        name = groupName,
        rounds = totalRounds,
        workPhases = listOf(
            WorkPhase(
                name = phaseName,
                durationSeconds = workTimeSeconds
            )
        ),
        restBetweenPhases = 0, // Single phase, no rest between phases
        restBetweenRounds = if (noRest) 0 else restTimeSeconds,
        specialRestAfterGroup = null
    )
}