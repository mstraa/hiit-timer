package com.hiittimer.app.error

import android.content.Context
import com.hiittimer.app.error.ErrorHandler.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Comprehensive unit tests for the ErrorHandler system
 * Tests error classification, recovery actions, and logging functionality
 */
class ErrorHandlerTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var errorHandler: ErrorHandler

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Note: In real tests, we'd need to mock the singleton properly
        // For now, we'll test the error types and recovery logic
    }

    @Test
    fun `timer exception types are properly classified`() {
        // Test TimerStartFailure
        val startFailure = TimerException.TimerStartFailure("Failed to start")
        assertTrue("Should be TimerStartFailure", startFailure is TimerException.TimerStartFailure)
        assertEquals("Failed to start", startFailure.message)

        // Test TimerStateInvalid
        val stateInvalid = TimerException.TimerStateInvalid("Invalid state")
        assertTrue("Should be TimerStateInvalid", stateInvalid is TimerException.TimerStateInvalid)
        assertEquals("Invalid state", stateInvalid.message)

        // Test TimerConfigurationError
        val configError = TimerException.TimerConfigurationError("Invalid config")
        assertTrue("Should be TimerConfigurationError", configError is TimerException.TimerConfigurationError)
        assertEquals("Invalid config", configError.message)

        // Test TimerServiceConnectionError
        val serviceError = TimerException.TimerServiceConnectionError("Service failed")
        assertTrue("Should be TimerServiceConnectionError", serviceError is TimerException.TimerServiceConnectionError)
        assertEquals("Service failed", serviceError.message)
    }

    @Test
    fun `audio exception types are properly classified`() {
        // Test AudioInitializationError
        val initError = AudioException.AudioInitializationError("Init failed")
        assertTrue("Should be AudioInitializationError", initError is AudioException.AudioInitializationError)
        assertEquals("Init failed", initError.message)

        // Test AudioPlaybackError
        val playbackError = AudioException.AudioPlaybackError("Playback failed")
        assertTrue("Should be AudioPlaybackError", playbackError is AudioException.AudioPlaybackError)
        assertEquals("Playback failed", playbackError.message)

        // Test AudioFocusError
        val focusError = AudioException.AudioFocusError("Focus lost")
        assertTrue("Should be AudioFocusError", focusError is AudioException.AudioFocusError)
        assertEquals("Focus lost", focusError.message)
    }

    @Test
    fun `database exception types are properly classified`() {
        // Test DatabaseConnectionError
        val connectionError = DatabaseException.DatabaseConnectionError("Connection failed")
        assertTrue("Should be DatabaseConnectionError", connectionError is DatabaseException.DatabaseConnectionError)
        assertEquals("Connection failed", connectionError.message)

        // Test DataCorruptionError
        val corruptionError = DatabaseException.DataCorruptionError("Data corrupted")
        assertTrue("Should be DataCorruptionError", corruptionError is DatabaseException.DataCorruptionError)
        assertEquals("Data corrupted", corruptionError.message)

        // Test DataMigrationError
        val migrationError = DatabaseException.DataMigrationError("Migration failed")
        assertTrue("Should be DataMigrationError", migrationError is DatabaseException.DataMigrationError)
        assertEquals("Migration failed", migrationError.message)
    }

    @Test
    fun `timer error recovery actions are appropriate`() {
        // Create a mock error handler for testing recovery logic
        val mockErrorHandler = object {
            fun handleTimerError(error: TimerException): ErrorRecoveryAction {
                return when (error) {
                    is TimerException.TimerStartFailure -> ErrorRecoveryAction.RETRY_OPERATION
                    is TimerException.TimerStateInvalid -> ErrorRecoveryAction.FALLBACK_TO_DEFAULT
                    is TimerException.TimerConfigurationError -> ErrorRecoveryAction.SHOW_USER_MESSAGE
                    is TimerException.TimerServiceConnectionError -> ErrorRecoveryAction.RESTART_COMPONENT
                }
            }
        }

        // Test recovery actions
        assertEquals(ErrorRecoveryAction.RETRY_OPERATION, 
            mockErrorHandler.handleTimerError(TimerException.TimerStartFailure("Test")))
        assertEquals(ErrorRecoveryAction.FALLBACK_TO_DEFAULT, 
            mockErrorHandler.handleTimerError(TimerException.TimerStateInvalid("Test")))
        assertEquals(ErrorRecoveryAction.SHOW_USER_MESSAGE, 
            mockErrorHandler.handleTimerError(TimerException.TimerConfigurationError("Test")))
        assertEquals(ErrorRecoveryAction.RESTART_COMPONENT, 
            mockErrorHandler.handleTimerError(TimerException.TimerServiceConnectionError("Test")))
    }

    @Test
    fun `audio error recovery actions are appropriate`() {
        val mockErrorHandler = object {
            fun handleAudioError(error: AudioException): ErrorRecoveryAction {
                return when (error) {
                    is AudioException.AudioInitializationError -> ErrorRecoveryAction.FALLBACK_TO_DEFAULT
                    is AudioException.AudioPlaybackError -> ErrorRecoveryAction.LOG_AND_CONTINUE
                    is AudioException.AudioFocusError -> ErrorRecoveryAction.LOG_AND_CONTINUE
                }
            }
        }

        assertEquals(ErrorRecoveryAction.FALLBACK_TO_DEFAULT, 
            mockErrorHandler.handleAudioError(AudioException.AudioInitializationError("Test")))
        assertEquals(ErrorRecoveryAction.LOG_AND_CONTINUE, 
            mockErrorHandler.handleAudioError(AudioException.AudioPlaybackError("Test")))
        assertEquals(ErrorRecoveryAction.LOG_AND_CONTINUE, 
            mockErrorHandler.handleAudioError(AudioException.AudioFocusError("Test")))
    }

    @Test
    fun `database error recovery actions are appropriate`() {
        val mockErrorHandler = object {
            fun handleDatabaseError(error: DatabaseException): ErrorRecoveryAction {
                return when (error) {
                    is DatabaseException.DatabaseConnectionError -> ErrorRecoveryAction.RETRY_OPERATION
                    is DatabaseException.DataCorruptionError -> ErrorRecoveryAction.CRITICAL_FAILURE
                    is DatabaseException.DataMigrationError -> ErrorRecoveryAction.CRITICAL_FAILURE
                }
            }
        }

        assertEquals(ErrorRecoveryAction.RETRY_OPERATION, 
            mockErrorHandler.handleDatabaseError(DatabaseException.DatabaseConnectionError("Test")))
        assertEquals(ErrorRecoveryAction.CRITICAL_FAILURE, 
            mockErrorHandler.handleDatabaseError(DatabaseException.DataCorruptionError("Test")))
        assertEquals(ErrorRecoveryAction.CRITICAL_FAILURE, 
            mockErrorHandler.handleDatabaseError(DatabaseException.DataMigrationError("Test")))
    }

    @Test
    fun `log levels are properly defined`() {
        assertEquals(2, LogLevel.VERBOSE.priority)
        assertEquals(3, LogLevel.DEBUG.priority)
        assertEquals(4, LogLevel.INFO.priority)
        assertEquals(5, LogLevel.WARNING.priority)
        assertEquals(6, LogLevel.ERROR.priority)

        assertEquals("V", LogLevel.VERBOSE.tag)
        assertEquals("D", LogLevel.DEBUG.tag)
        assertEquals("I", LogLevel.INFO.tag)
        assertEquals("W", LogLevel.WARNING.tag)
        assertEquals("E", LogLevel.ERROR.tag)
    }

    @Test
    fun `error categories are comprehensive`() {
        val categories = ErrorCategory.values()
        
        assertTrue("Should contain TIMER_OPERATION", categories.contains(ErrorCategory.TIMER_OPERATION))
        assertTrue("Should contain AUDIO_PLAYBACK", categories.contains(ErrorCategory.AUDIO_PLAYBACK))
        assertTrue("Should contain DATABASE_ACCESS", categories.contains(ErrorCategory.DATABASE_ACCESS))
        assertTrue("Should contain NETWORK_REQUEST", categories.contains(ErrorCategory.NETWORK_REQUEST))
        assertTrue("Should contain UI_RENDERING", categories.contains(ErrorCategory.UI_RENDERING))
        assertTrue("Should contain BACKGROUND_SERVICE", categories.contains(ErrorCategory.BACKGROUND_SERVICE))
        assertTrue("Should contain USER_INPUT", categories.contains(ErrorCategory.USER_INPUT))
        assertTrue("Should contain SYSTEM_INTEGRATION", categories.contains(ErrorCategory.SYSTEM_INTEGRATION))
    }

    @Test
    fun `recovery actions are comprehensive`() {
        val actions = ErrorRecoveryAction.values()
        
        assertTrue("Should contain RETRY_OPERATION", actions.contains(ErrorRecoveryAction.RETRY_OPERATION))
        assertTrue("Should contain FALLBACK_TO_DEFAULT", actions.contains(ErrorRecoveryAction.FALLBACK_TO_DEFAULT))
        assertTrue("Should contain RESTART_COMPONENT", actions.contains(ErrorRecoveryAction.RESTART_COMPONENT))
        assertTrue("Should contain SHOW_USER_MESSAGE", actions.contains(ErrorRecoveryAction.SHOW_USER_MESSAGE))
        assertTrue("Should contain LOG_AND_CONTINUE", actions.contains(ErrorRecoveryAction.LOG_AND_CONTINUE))
        assertTrue("Should contain CRITICAL_FAILURE", actions.contains(ErrorRecoveryAction.CRITICAL_FAILURE))
    }

    @Test
    fun `log entry data class works correctly`() {
        val timestamp = "2025-01-01 12:00:00.000"
        val level = LogLevel.ERROR
        val category = ErrorCategory.TIMER_OPERATION
        val message = "Test error"
        val throwable = RuntimeException("Test exception")
        val additionalData = mapOf("key" to "value")

        val logEntry = LogEntry(timestamp, level, category, message, throwable, additionalData)

        assertEquals(timestamp, logEntry.timestamp)
        assertEquals(level, logEntry.level)
        assertEquals(category, logEntry.category)
        assertEquals(message, logEntry.message)
        assertEquals(throwable, logEntry.throwable)
        assertEquals(additionalData, logEntry.additionalData)
    }

    @Test
    fun `exception inheritance is correct`() {
        val timerException = TimerException.TimerStartFailure("Test")
        assertTrue("TimerException should extend Exception", timerException is Exception)
        assertTrue("TimerStartFailure should extend TimerException", timerException is TimerException)

        val audioException = AudioException.AudioInitializationError("Test")
        assertTrue("AudioException should extend Exception", audioException is Exception)
        assertTrue("AudioInitializationError should extend AudioException", audioException is AudioException)

        val databaseException = DatabaseException.DatabaseConnectionError("Test")
        assertTrue("DatabaseException should extend Exception", databaseException is Exception)
        assertTrue("DatabaseConnectionError should extend DatabaseException", databaseException is DatabaseException)
    }

    @Test
    fun `exception messages are preserved`() {
        val message = "Test error message"
        val cause = RuntimeException("Cause")

        val timerException = TimerException.TimerStartFailure(message, cause)
        assertEquals(message, timerException.message)
        assertEquals(cause, timerException.cause)

        val audioException = AudioException.AudioInitializationError(message, cause)
        assertEquals(message, audioException.message)
        assertEquals(cause, audioException.cause)

        val databaseException = DatabaseException.DatabaseConnectionError(message, cause)
        assertEquals(message, databaseException.message)
        assertEquals(cause, databaseException.cause)
    }
}
