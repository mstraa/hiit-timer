package com.hiittimer.app.data

/**
 * Extended timer configuration that supports complex presets
 * This wraps the basic TimerConfig and adds complex preset support
 */
data class ComplexTimerConfig(
    val baseConfig: TimerConfig,
    val complexPreset: ComplexPreset? = null,
    val useComplexMode: Boolean = false
) {
    /**
     * Create from a complex preset
     */
    constructor(complexPreset: ComplexPreset) : this(
        baseConfig = complexPreset.toSimplePreset().toTimerConfig(),
        complexPreset = complexPreset,
        useComplexMode = true
    )
    
    /**
     * Create from a simple preset
     */
    constructor(preset: Preset) : this(
        baseConfig = preset.toTimerConfig(),
        complexPreset = null,
        useComplexMode = false
    )
    
    /**
     * Get the effective configuration to use
     * Returns the base config if not in complex mode
     */
    fun getEffectiveConfig(): TimerConfig {
        return baseConfig
    }
    
    /**
     * Convert complex workout to a sequence of timer segments
     * This helps the timer logic process complex workouts
     */
    fun toTimerSegments(): List<TimerSegment> {
        if (!useComplexMode || complexPreset == null) {
            // Simple mode - create segments from base config
            return buildList {
                for (round in 1..baseConfig.totalRounds) {
                    add(TimerSegment(
                        type = SegmentType.WORK,
                        duration = baseConfig.workTimeSeconds,
                        name = "Work",
                        round = round,
                        totalRounds = baseConfig.totalRounds
                    ))
                    
                    if (!baseConfig.noRest && round < baseConfig.totalRounds) {
                        add(TimerSegment(
                            type = SegmentType.REST,
                            duration = baseConfig.restTimeSeconds,
                            name = "Rest",
                            round = round,
                            totalRounds = baseConfig.totalRounds
                        ))
                    }
                }
            }
        }
        
        // Complex mode - create segments from round groups
        return buildList {
            complexPreset.roundGroups.forEachIndexed { groupIndex, group ->
                for (round in 1..group.rounds) {
                    // Add work phases
                    group.workPhases.forEachIndexed { phaseIndex, phase ->
                        add(TimerSegment(
                            type = SegmentType.WORK,
                            duration = phase.durationSeconds,
                            name = phase.name,
                            description = phase.description,
                            groupName = group.name,
                            groupIndex = groupIndex,
                            round = round,
                            totalRounds = group.rounds,
                            phaseIndex = phaseIndex,
                            totalPhases = group.workPhases.size
                        ))
                        
                        // Add rest between phases (if not last phase)
                        if (phaseIndex < group.workPhases.size - 1 && group.restBetweenPhases > 0) {
                            add(TimerSegment(
                                type = SegmentType.PHASE_REST,
                                duration = group.restBetweenPhases,
                                name = "Rest",
                                groupName = group.name,
                                groupIndex = groupIndex,
                                round = round,
                                totalRounds = group.rounds
                            ))
                        }
                    }
                    
                    // Add rest between rounds (if not last round)
                    if (round < group.rounds && group.restBetweenRounds > 0) {
                        add(TimerSegment(
                            type = SegmentType.ROUND_REST,
                            duration = group.restBetweenRounds,
                            name = "Round Rest",
                            groupName = group.name,
                            groupIndex = groupIndex,
                            round = round,
                            totalRounds = group.rounds
                        ))
                    }
                }
                
                // Add special rest after group (if not last group and has special rest)
                if (groupIndex < complexPreset.roundGroups.size - 1 && group.specialRestAfterGroup != null) {
                    add(TimerSegment(
                        type = SegmentType.SPECIAL_REST,
                        duration = group.specialRestAfterGroup,
                        name = "Special Rest",
                        description = "Rest before ${complexPreset.roundGroups[groupIndex + 1].name}",
                        groupName = group.name,
                        groupIndex = groupIndex
                    ))
                }
            }
        }
    }
}

/**
 * Types of timer segments
 */
enum class SegmentType {
    WORK,           // Work interval
    REST,           // Standard rest (simple mode)
    PHASE_REST,     // Rest between phases
    ROUND_REST,     // Rest between rounds
    SPECIAL_REST    // Special rest between groups
}

/**
 * Represents a single timer segment (work or rest period)
 */
data class TimerSegment(
    val type: SegmentType,
    val duration: Int,
    val name: String,
    val description: String? = null,
    val groupName: String? = null,
    val groupIndex: Int? = null,
    val round: Int? = null,
    val totalRounds: Int? = null,
    val phaseIndex: Int? = null,
    val totalPhases: Int? = null
) {
    /**
     * Check if this segment should skip the "Next: REST" preview
     * according to the requirements
     */
    fun shouldSkipNextRestPreview(nextSegment: TimerSegment?): Boolean {
        if (type != SegmentType.WORK) return false
        if (nextSegment == null) return true
        
        return when (nextSegment.type) {
            SegmentType.SPECIAL_REST -> true
            SegmentType.REST, SegmentType.PHASE_REST, SegmentType.ROUND_REST -> false
            SegmentType.WORK -> true // No rest between
        }
    }
}