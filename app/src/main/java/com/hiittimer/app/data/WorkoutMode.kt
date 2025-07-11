package com.hiittimer.app.data

import kotlinx.serialization.Serializable

/**
 * Defines different workout execution modes
 */
@Serializable
enum class WorkoutMode {
    /**
     * Traditional time-based intervals (work/rest)
     */
    TIME_BASED,
    
    /**
     * Rep-based with rest between sets
     */
    REP_BASED,
    
    /**
     * As Many Rounds As Possible in given time
     */
    AMRAP,
    
    /**
     * Every Minute On The Minute
     */
    EMOM,
    
    /**
     * Tabata protocol (20s work, 10s rest)
     */
    TABATA,
    
    /**
     * For Reps - complete reps as fast as possible
     */
    FOR_TIME,
    
    /**
     * Static hold or stretch for duration
     */
    STATIC_HOLD
}

/**
 * Phase types in a workout
 */
@Serializable
enum class PhaseType {
    WARM_UP,
    STRENGTH,
    TECHNIQUE,
    MAIN_WOD,
    COOL_DOWN,
    CUSTOM
}

/**
 * Exercise categories for organization
 */
@Serializable
enum class ExerciseCategory {
    CARDIO,
    STRENGTH,
    OLYMPIC_LIFTING,
    GYMNASTICS,
    MOBILITY,
    CORE,
    PLYOMETRIC,
    FLEXIBILITY,
    CUSTOM
}