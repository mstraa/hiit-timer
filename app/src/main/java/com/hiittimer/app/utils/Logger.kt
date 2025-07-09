package com.hiittimer.app.utils

import android.content.Context
import com.hiittimer.app.error.ErrorHandler

/**
 * Centralized logging utility for the HIIT Timer app
 * Provides structured logging with different levels and categories
 */
object Logger {
    
    private var errorHandler: ErrorHandler? = null
    private var isDebugMode = true // Default to true, can be overridden
    
    /**
     * Initialize the logger with context
     */
    fun initialize(context: Context) {
        errorHandler = ErrorHandler.getInstance(context)
    }
    
    /**
     * Set debug mode (for testing)
     */
    fun setDebugMode(debug: Boolean) {
        isDebugMode = debug
    }
    
    /**
     * Log verbose message
     */
    fun v(category: ErrorHandler.ErrorCategory, message: String, additionalData: Map<String, Any> = emptyMap()) {
        if (isDebugMode) {
            errorHandler?.log(ErrorHandler.LogLevel.VERBOSE, category, message, additionalData = additionalData)
        }
    }
    
    /**
     * Log debug message
     */
    fun d(category: ErrorHandler.ErrorCategory, message: String, additionalData: Map<String, Any> = emptyMap()) {
        if (isDebugMode) {
            errorHandler?.log(ErrorHandler.LogLevel.DEBUG, category, message, additionalData = additionalData)
        }
    }
    
    /**
     * Log info message
     */
    fun i(category: ErrorHandler.ErrorCategory, message: String, additionalData: Map<String, Any> = emptyMap()) {
        errorHandler?.log(ErrorHandler.LogLevel.INFO, category, message, additionalData = additionalData)
    }
    
    /**
     * Log warning message
     */
    fun w(category: ErrorHandler.ErrorCategory, message: String, throwable: Throwable? = null, additionalData: Map<String, Any> = emptyMap()) {
        errorHandler?.log(ErrorHandler.LogLevel.WARNING, category, message, throwable, additionalData)
    }
    
    /**
     * Log error message
     */
    fun e(category: ErrorHandler.ErrorCategory, message: String, throwable: Throwable? = null, additionalData: Map<String, Any> = emptyMap()) {
        errorHandler?.log(ErrorHandler.LogLevel.ERROR, category, message, throwable, additionalData)
    }
    
    /**
     * Timer-specific logging methods
     */
    object Timer {
        fun start(config: String) {
            i(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer started", mapOf("config" to config))
        }
        
        fun pause(timeRemaining: Long) {
            i(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer paused", mapOf("timeRemaining" to timeRemaining))
        }
        
        fun resume(timeRemaining: Long) {
            i(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer resumed", mapOf("timeRemaining" to timeRemaining))
        }
        
        fun reset() {
            i(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer reset")
        }
        
        fun complete(totalTime: Long, rounds: Int) {
            i(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Timer completed", 
                mapOf("totalTime" to totalTime, "rounds" to rounds))
        }
        
        fun error(message: String, throwable: Throwable? = null) {
            e(ErrorHandler.ErrorCategory.TIMER_OPERATION, message, throwable)
        }
    }
    
    /**
     * Audio-specific logging methods
     */
    object Audio {
        fun playSound(soundType: String) {
            d(ErrorHandler.ErrorCategory.AUDIO_PLAYBACK, "Playing sound", mapOf("type" to soundType))
        }
        
        fun volumeChanged(volume: Float) {
            d(ErrorHandler.ErrorCategory.AUDIO_PLAYBACK, "Volume changed", mapOf("volume" to volume))
        }
        
        fun audioFocusChanged(focusChange: Int) {
            d(ErrorHandler.ErrorCategory.AUDIO_PLAYBACK, "Audio focus changed", mapOf("focusChange" to focusChange))
        }
        
        fun error(message: String, throwable: Throwable? = null) {
            e(ErrorHandler.ErrorCategory.AUDIO_PLAYBACK, message, throwable)
        }
    }
    
    /**
     * Database-specific logging methods
     */
    object Database {
        fun query(table: String, operation: String) {
            d(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Database operation", 
                mapOf("table" to table, "operation" to operation))
        }
        
        fun migration(fromVersion: Int, toVersion: Int) {
            i(ErrorHandler.ErrorCategory.DATABASE_ACCESS, "Database migration", 
                mapOf("from" to fromVersion, "to" to toVersion))
        }
        
        fun error(message: String, throwable: Throwable? = null) {
            e(ErrorHandler.ErrorCategory.DATABASE_ACCESS, message, throwable)
        }
    }
    
    /**
     * UI-specific logging methods
     */
    object UI {
        fun screenNavigation(from: String, to: String) {
            d(ErrorHandler.ErrorCategory.UI_RENDERING, "Screen navigation", 
                mapOf("from" to from, "to" to to))
        }
        
        fun userAction(action: String, context: String) {
            d(ErrorHandler.ErrorCategory.USER_INPUT, "User action", 
                mapOf("action" to action, "context" to context))
        }
        
        fun renderingError(component: String, throwable: Throwable? = null) {
            e(ErrorHandler.ErrorCategory.UI_RENDERING, "Rendering error in $component", throwable)
        }
    }
    
    /**
     * Service-specific logging methods
     */
    object Service {
        fun started(serviceName: String) {
            i(ErrorHandler.ErrorCategory.BACKGROUND_SERVICE, "Service started", mapOf("service" to serviceName))
        }
        
        fun stopped(serviceName: String) {
            i(ErrorHandler.ErrorCategory.BACKGROUND_SERVICE, "Service stopped", mapOf("service" to serviceName))
        }
        
        fun connectionChanged(serviceName: String, connected: Boolean) {
            d(ErrorHandler.ErrorCategory.BACKGROUND_SERVICE, "Service connection changed", 
                mapOf("service" to serviceName, "connected" to connected))
        }
        
        fun error(serviceName: String, message: String, throwable: Throwable? = null) {
            e(ErrorHandler.ErrorCategory.BACKGROUND_SERVICE, "$serviceName: $message", throwable)
        }
    }
    
    /**
     * Performance logging methods
     */
    object Performance {
        fun measureTime(operation: String, timeMs: Long) {
            if (timeMs > 100) { // Only log operations taking more than 100ms
                w(ErrorHandler.ErrorCategory.SYSTEM_INTEGRATION, "Slow operation detected", 
                    additionalData = mapOf("operation" to operation, "timeMs" to timeMs))
            } else {
                d(ErrorHandler.ErrorCategory.SYSTEM_INTEGRATION, "Operation completed", 
                    additionalData = mapOf("operation" to operation, "timeMs" to timeMs))
            }
        }
        
        fun memoryUsage(usedMB: Long, totalMB: Long) {
            val usagePercent = (usedMB * 100) / totalMB
            if (usagePercent > 80) {
                w(ErrorHandler.ErrorCategory.SYSTEM_INTEGRATION, "High memory usage", 
                    additionalData = mapOf("usedMB" to usedMB, "totalMB" to totalMB, "percent" to usagePercent))
            } else {
                d(ErrorHandler.ErrorCategory.SYSTEM_INTEGRATION, "Memory usage", 
                    additionalData = mapOf("usedMB" to usedMB, "totalMB" to totalMB, "percent" to usagePercent))
            }
        }
        
        fun cpuUsage(percent: Double) {
            if (percent > 10.0) {
                w(ErrorHandler.ErrorCategory.SYSTEM_INTEGRATION, "High CPU usage", 
                    additionalData = mapOf("percent" to percent))
            } else {
                d(ErrorHandler.ErrorCategory.SYSTEM_INTEGRATION, "CPU usage", 
                    additionalData = mapOf("percent" to percent))
            }
        }
    }
    
    /**
     * Export logs for debugging
     */
    fun exportLogs(): String {
        return errorHandler?.exportLogs() ?: "Logger not initialized"
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        errorHandler?.clearLogs()
    }
    
    /**
     * Get error count for a category
     */
    fun getErrorCount(category: ErrorHandler.ErrorCategory, minutes: Int = 60): Int {
        return errorHandler?.getRecentErrorCount(category, minutes) ?: 0
    }
}
