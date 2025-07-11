package com.hiittimer.app.data

/**
 * Extended timer state that tracks complex workout progress
 * This includes current round group, work phase, and special rest periods
 */
data class ComplexTimerState(
    val baseState: TimerStatus,
    val complexPreset: ComplexPreset? = null,
    val currentGroupIndex: Int = 0,
    val currentRoundInGroup: Int = 1,
    val currentPhaseIndex: Int = 0,
    val isInSpecialRest: Boolean = false,
    val isInPhaseRest: Boolean = false,
    val isInRoundRest: Boolean = false
) {
    /**
     * Get the current round group, or null if not using complex preset
     */
    val currentGroup: RoundGroup?
        get() = complexPreset?.roundGroups?.getOrNull(currentGroupIndex)
    
    /**
     * Get the current work phase, or null if not in work phase
     */
    val currentPhase: WorkPhase?
        get() = if (!isInSpecialRest && !isInPhaseRest && !isInRoundRest) {
            currentGroup?.workPhases?.getOrNull(currentPhaseIndex)
        } else null
    
    /**
     * Check if this is the last phase in the current round
     */
    val isLastPhaseInRound: Boolean
        get() = currentGroup?.let { group ->
            currentPhaseIndex >= group.workPhases.size - 1
        } ?: false
    
    /**
     * Check if this is the last round in the current group
     */
    val isLastRoundInGroup: Boolean
        get() = currentGroup?.let { group ->
            currentRoundInGroup >= group.rounds
        } ?: false
    
    /**
     * Check if this is the last group in the workout
     */
    val isLastGroup: Boolean
        get() = complexPreset?.let { preset ->
            currentGroupIndex >= preset.roundGroups.size - 1
        } ?: false
    
    /**
     * Get display text for current activity
     */
    fun getCurrentActivityName(): String {
        return when {
            isInSpecialRest -> "Special Rest"
            isInPhaseRest -> "Rest"
            isInRoundRest -> "Round Rest"
            currentPhase != null -> currentPhase!!.name
            baseState.isWorkInterval -> "Work"
            else -> "Rest"
        }
    }
    
    /**
     * Get display text for current group and round
     */
    fun getGroupRoundText(): String {
        return currentGroup?.let { group ->
            "${group.name} - Round $currentRoundInGroup of ${group.rounds}"
        } ?: baseState.getRoundProgressText()
    }
    
    /**
     * Get next activity preview
     */
    fun getNextActivityPreview(): String? {
        if (baseState.state == TimerState.STOPPED || baseState.state == TimerState.FINISHED) {
            return null
        }
        
        if (complexPreset == null) {
            return baseState.getNextIntervalPreview()
        }
        
        return when {
            // Currently in special rest
            isInSpecialRest -> {
                val nextGroup = complexPreset.roundGroups.getOrNull(currentGroupIndex + 1)
                nextGroup?.let { 
                    "Next: ${it.name} - ${it.workPhases.first().name}"
                }
            }
            
            // Currently in round rest
            isInRoundRest -> {
                currentGroup?.let {
                    "Next: Round ${currentRoundInGroup + 1} - ${it.workPhases.first().name}"
                }
            }
            
            // Currently in phase rest
            isInPhaseRest -> {
                currentGroup?.workPhases?.getOrNull(currentPhaseIndex + 1)?.let {
                    "Next: ${it.name}"
                }
            }
            
            // Currently in work phase
            currentPhase != null -> {
                when {
                    // More phases in this round
                    !isLastPhaseInRound -> {
                        val restTime = currentGroup!!.restBetweenPhases
                        if (restTime > 0) {
                            "Next: Rest (${restTime}s)"
                        } else {
                            currentGroup!!.workPhases[currentPhaseIndex + 1].let {
                                "Next: ${it.name}"
                            }
                        }
                    }
                    
                    // Last phase but more rounds
                    !isLastRoundInGroup -> {
                        val restTime = currentGroup!!.restBetweenRounds
                        "Next: Round Rest (${restTime}s)"
                    }
                    
                    // Last round but more groups
                    !isLastGroup -> {
                        val specialRest = currentGroup!!.specialRestAfterGroup
                        if (specialRest != null) {
                            "Next: Special Rest (${specialRest}s)"
                        } else {
                            val nextGroup = complexPreset.roundGroups[currentGroupIndex + 1]
                            "Next: ${nextGroup.name} - ${nextGroup.workPhases.first().name}"
                        }
                    }
                    
                    // Last everything
                    else -> "Next: FINISHED"
                }
            }
            
            else -> null
        }
    }
    
    /**
     * Calculate total completed work intervals
     */
    fun getCompletedIntervals(): Int {
        if (complexPreset == null) return 0
        
        var total = 0
        
        // Count all completed groups
        for (i in 0 until currentGroupIndex) {
            val group = complexPreset.roundGroups[i]
            total += group.workPhases.size * group.rounds
        }
        
        // Count completed rounds in current group
        currentGroup?.let { group ->
            total += group.workPhases.size * (currentRoundInGroup - 1)
            
            // Count completed phases in current round
            if (!isInSpecialRest && !isInRoundRest) {
                total += currentPhaseIndex
                if (!isInPhaseRest && currentPhase != null) {
                    total += 1 // Include current phase if in work
                }
            }
        }
        
        return total
    }
    
    /**
     * Get total intervals in workout
     */
    fun getTotalIntervals(): Int {
        return complexPreset?.getTotalWorkIntervals() ?: baseState.config.totalRounds
    }
}