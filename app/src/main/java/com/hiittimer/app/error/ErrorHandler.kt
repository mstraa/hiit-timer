package com.hiittimer.app.error

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized error handling system for the HIIT Timer app
 * Provides structured logging, error recovery, and user-friendly error messages
 */
class ErrorHandler private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "HIITTimer_ErrorHandler"
        private const val MAX_LOG_ENTRIES = 1000
        
        @Volatile
        private var INSTANCE: ErrorHandler? = null
        
        fun getInstance(context: Context): ErrorHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ErrorHandler(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val logEntries = mutableListOf<LogEntry>()
    
    /**
     * Log levels for structured logging
     */
    enum class LogLevel(val priority: Int, val tag: String) {
        VERBOSE(Log.VERBOSE, "V"),
        DEBUG(Log.DEBUG, "D"),
        INFO(Log.INFO, "I"),
        WARNING(Log.WARN, "W"),
        ERROR(Log.ERROR, "E")
    }
    
    /**
     * Error categories for better classification
     */
    enum class ErrorCategory {
        TIMER_OPERATION,
        AUDIO_PLAYBACK,
        DATABASE_ACCESS,
        NETWORK_REQUEST,
        UI_RENDERING,
        BACKGROUND_SERVICE,
        USER_INPUT,
        SYSTEM_INTEGRATION
    }
    
    /**
     * Error recovery actions
     */
    enum class ErrorRecoveryAction {
        RETRY_OPERATION,
        FALLBACK_TO_DEFAULT,
        RESTART_COMPONENT,
        SHOW_USER_MESSAGE,
        LOG_AND_CONTINUE,
        CRITICAL_FAILURE
    }
    
    /**
     * Log entry data class
     */
    data class LogEntry(
        val timestamp: String,
        val level: LogLevel,
        val category: ErrorCategory,
        val message: String,
        val throwable: Throwable? = null,
        val additionalData: Map<String, Any> = emptyMap()
    )
    
    /**
     * Timer-specific exception types
     */
    sealed class TimerException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class TimerStartFailure(message: String, cause: Throwable? = null) : TimerException(message, cause)
        class TimerStateInvalid(message: String) : TimerException(message)
        class TimerConfigurationError(message: String) : TimerException(message)
        class TimerServiceConnectionError(message: String, cause: Throwable? = null) : TimerException(message, cause)
    }
    
    /**
     * Audio-specific exception types
     */
    sealed class AudioException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class AudioInitializationError(message: String, cause: Throwable? = null) : AudioException(message, cause)
        class AudioPlaybackError(message: String, cause: Throwable? = null) : AudioException(message, cause)
        class AudioFocusError(message: String) : AudioException(message)
    }
    
    /**
     * Database-specific exception types
     */
    sealed class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class DatabaseConnectionError(message: String, cause: Throwable? = null) : DatabaseException(message, cause)
        class DataCorruptionError(message: String, cause: Throwable? = null) : DatabaseException(message, cause)
        class DataMigrationError(message: String, cause: Throwable? = null) : DatabaseException(message, cause)
    }
    
    /**
     * Log a message with specified level and category
     */
    fun log(
        level: LogLevel,
        category: ErrorCategory,
        message: String,
        throwable: Throwable? = null,
        additionalData: Map<String, Any> = emptyMap()
    ) {
        val timestamp = dateFormat.format(Date())
        val logEntry = LogEntry(timestamp, level, category, message, throwable, additionalData)
        
        // Add to internal log
        synchronized(logEntries) {
            logEntries.add(logEntry)
            if (logEntries.size > MAX_LOG_ENTRIES) {
                logEntries.removeAt(0)
            }
        }
        
        // Log to Android system
        val logMessage = buildLogMessage(logEntry)
        when (level) {
            LogLevel.VERBOSE -> Log.v(TAG, logMessage, throwable)
            LogLevel.DEBUG -> Log.d(TAG, logMessage, throwable)
            LogLevel.INFO -> Log.i(TAG, logMessage, throwable)
            LogLevel.WARNING -> Log.w(TAG, logMessage, throwable)
            LogLevel.ERROR -> Log.e(TAG, logMessage, throwable)
        }
    }
    
    /**
     * Handle timer-related errors
     */
    fun handleTimerError(error: TimerException): ErrorRecoveryAction {
        return when (error) {
            is TimerException.TimerStartFailure -> {
                log(LogLevel.ERROR, ErrorCategory.TIMER_OPERATION, 
                    "Timer start failure: ${error.message}", error)
                ErrorRecoveryAction.RETRY_OPERATION
            }
            is TimerException.TimerStateInvalid -> {
                log(LogLevel.WARNING, ErrorCategory.TIMER_OPERATION, 
                    "Invalid timer state: ${error.message}", error)
                ErrorRecoveryAction.FALLBACK_TO_DEFAULT
            }
            is TimerException.TimerConfigurationError -> {
                log(LogLevel.ERROR, ErrorCategory.TIMER_OPERATION, 
                    "Timer configuration error: ${error.message}", error)
                ErrorRecoveryAction.SHOW_USER_MESSAGE
            }
            is TimerException.TimerServiceConnectionError -> {
                log(LogLevel.ERROR, ErrorCategory.BACKGROUND_SERVICE, 
                    "Service connection error: ${error.message}", error)
                ErrorRecoveryAction.RESTART_COMPONENT
            }
        }
    }
    
    /**
     * Handle audio-related errors
     */
    fun handleAudioError(error: AudioException): ErrorRecoveryAction {
        return when (error) {
            is AudioException.AudioInitializationError -> {
                log(LogLevel.ERROR, ErrorCategory.AUDIO_PLAYBACK, 
                    "Audio initialization failed: ${error.message}", error)
                ErrorRecoveryAction.FALLBACK_TO_DEFAULT
            }
            is AudioException.AudioPlaybackError -> {
                log(LogLevel.WARNING, ErrorCategory.AUDIO_PLAYBACK, 
                    "Audio playback error: ${error.message}", error)
                ErrorRecoveryAction.LOG_AND_CONTINUE
            }
            is AudioException.AudioFocusError -> {
                log(LogLevel.INFO, ErrorCategory.AUDIO_PLAYBACK, 
                    "Audio focus error: ${error.message}", error)
                ErrorRecoveryAction.LOG_AND_CONTINUE
            }
        }
    }
    
    /**
     * Handle database-related errors
     */
    fun handleDatabaseError(error: DatabaseException): ErrorRecoveryAction {
        return when (error) {
            is DatabaseException.DatabaseConnectionError -> {
                log(LogLevel.ERROR, ErrorCategory.DATABASE_ACCESS, 
                    "Database connection error: ${error.message}", error)
                ErrorRecoveryAction.RETRY_OPERATION
            }
            is DatabaseException.DataCorruptionError -> {
                log(LogLevel.ERROR, ErrorCategory.DATABASE_ACCESS, 
                    "Data corruption detected: ${error.message}", error)
                ErrorRecoveryAction.CRITICAL_FAILURE
            }
            is DatabaseException.DataMigrationError -> {
                log(LogLevel.ERROR, ErrorCategory.DATABASE_ACCESS, 
                    "Data migration failed: ${error.message}", error)
                ErrorRecoveryAction.CRITICAL_FAILURE
            }
        }
    }
    
    /**
     * Create a coroutine exception handler
     */
    fun createCoroutineExceptionHandler(
        category: ErrorCategory,
        scope: CoroutineScope
    ): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, exception ->
            log(LogLevel.ERROR, category, "Coroutine exception occurred", exception)
            
            // Attempt recovery based on exception type
            scope.launch {
                when (exception) {
                    is TimerException -> handleTimerError(exception)
                    is AudioException -> handleAudioError(exception)
                    is DatabaseException -> handleDatabaseError(exception)
                    else -> {
                        log(LogLevel.ERROR, ErrorCategory.SYSTEM_INTEGRATION, 
                            "Unhandled exception: ${exception.message}", exception)
                        ErrorRecoveryAction.LOG_AND_CONTINUE
                    }
                }
            }
        }
    }
    
    /**
     * Get user-friendly error message
     */
    fun getUserFriendlyMessage(error: Throwable): String {
        return when (error) {
            is TimerException.TimerStartFailure -> "Unable to start timer. Please try again."
            is TimerException.TimerConfigurationError -> "Invalid timer settings. Please check your configuration."
            is AudioException.AudioInitializationError -> "Audio system unavailable. Timer will work without sound."
            is AudioException.AudioPlaybackError -> "Unable to play audio cues."
            is DatabaseException.DatabaseConnectionError -> "Unable to save data. Please try again."
            is DatabaseException.DataCorruptionError -> "Data corruption detected. App restart required."
            else -> "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Export logs for debugging
     */
    fun exportLogs(): String {
        synchronized(logEntries) {
            return logEntries.joinToString("\n") { entry ->
                buildLogMessage(entry)
            }
        }
    }
    
    /**
     * Clear logs
     */
    fun clearLogs() {
        synchronized(logEntries) {
            logEntries.clear()
        }
    }
    
    /**
     * Get recent error count by category
     */
    fun getRecentErrorCount(category: ErrorCategory, minutes: Int = 60): Int {
        val cutoffTime = System.currentTimeMillis() - (minutes * 60 * 1000)
        synchronized(logEntries) {
            return logEntries.count { entry ->
                entry.category == category && 
                entry.level == LogLevel.ERROR &&
                dateFormat.parse(entry.timestamp)?.time ?: 0 > cutoffTime
            }
        }
    }
    
    private fun buildLogMessage(entry: LogEntry): String {
        val builder = StringBuilder()
        builder.append("[${entry.level.tag}]")
        builder.append("[${entry.category.name}]")
        builder.append(" ${entry.message}")
        
        if (entry.additionalData.isNotEmpty()) {
            builder.append(" | Data: ${entry.additionalData}")
        }
        
        if (entry.throwable != null) {
            val sw = StringWriter()
            entry.throwable.printStackTrace(PrintWriter(sw))
            builder.append("\n${sw}")
        }
        
        return builder.toString()
    }
}
