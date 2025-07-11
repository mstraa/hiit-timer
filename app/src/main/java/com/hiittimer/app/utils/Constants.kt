package com.hiittimer.app.utils

/**
 * Global constants used throughout the app
 */
object Constants {
    
    // Time constants
    const val MILLISECONDS_PER_SECOND = 1000L
    const val SECONDS_PER_MINUTE = 60
    const val MINUTES_PER_HOUR = 60
    const val HOURS_PER_DAY = 24
    const val MILLISECONDS_PER_DAY = MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY
    
    // Workout session constants
    const val WORKOUT_COMPLETION_THRESHOLD_PERCENT = 70
    const val RECENT_WORKOUTS_DAYS_7 = 7
    const val RECENT_WORKOUTS_DAYS_30 = 30
    const val MONTHLY_WORKOUTS_DAYS = 30
    const val QUARTERLY_WORKOUTS_DAYS = 90
    
    // Timer constants
    const val TIMER_UPDATE_INTERVAL_MS = 100L
    const val TIMER_ACCURACY_THRESHOLD_MS = 50L
    const val NOTIFICATION_DELAY_AFTER_FINISH_MS = 5000L
    
    // Preset constants
    const val MAX_PRESETS_COUNT = 50
    const val RECENT_PRESETS_COUNT = 5
    
    // UI constants
    const val ANIMATION_DURATION_MS = 300
    const val FLASH_DURATION_MS = 100
    const val COUNTDOWN_BEEP_START_SECONDS = 3
}