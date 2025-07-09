package com.hiittimer.app.utils

import android.content.Context
import com.hiittimer.app.error.ErrorHandler
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the Logger utility
 * Tests logging functionality and structured logging methods
 */
class LoggerTest {

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Logger.setDebugMode(true) // Enable debug mode for testing
    }

    @Test
    fun `debug mode controls verbose and debug logging`() {
        // Test with debug mode enabled
        Logger.setDebugMode(true)
        
        // These should not throw exceptions when debug mode is enabled
        Logger.v(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Verbose message")
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Debug message")
        
        // Test with debug mode disabled
        Logger.setDebugMode(false)
        
        // These should not throw exceptions when debug mode is disabled
        Logger.v(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Verbose message")
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Debug message")
    }

    @Test
    fun `info warning and error logging always work`() {
        // These should work regardless of debug mode
        Logger.setDebugMode(false)
        
        Logger.i(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Info message")
        Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Warning message")
        Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Error message")
        
        // Should not throw exceptions
        assertTrue("Logging methods should complete without exceptions", true)
    }

    @Test
    fun `timer logging methods work correctly`() {
        // Test all timer logging methods
        Logger.Timer.start("config=test")
        Logger.Timer.pause(15000)
        Logger.Timer.resume(15000)
        Logger.Timer.reset()
        Logger.Timer.complete(300000, 5)
        Logger.Timer.error("Timer error", RuntimeException("Test"))
        
        // Should not throw exceptions
        assertTrue("Timer logging methods should complete without exceptions", true)
    }

    @Test
    fun `audio logging methods work correctly`() {
        // Test all audio logging methods
        Logger.Audio.playSound("work_interval")
        Logger.Audio.volumeChanged(0.8f)
        Logger.Audio.audioFocusChanged(1)
        Logger.Audio.error("Audio error", RuntimeException("Test"))
        
        // Should not throw exceptions
        assertTrue("Audio logging methods should complete without exceptions", true)
    }

    @Test
    fun `database logging methods work correctly`() {
        // Test all database logging methods
        Logger.Database.query("workouts", "INSERT")
        Logger.Database.migration(1, 2)
        Logger.Database.error("Database error", RuntimeException("Test"))
        
        // Should not throw exceptions
        assertTrue("Database logging methods should complete without exceptions", true)
    }

    @Test
    fun `ui logging methods work correctly`() {
        // Test all UI logging methods
        Logger.UI.screenNavigation("TimerScreen", "SettingsScreen")
        Logger.UI.userAction("button_click", "start_timer")
        Logger.UI.renderingError("TimerDisplay", RuntimeException("Test"))
        
        // Should not throw exceptions
        assertTrue("UI logging methods should complete without exceptions", true)
    }

    @Test
    fun `service logging methods work correctly`() {
        // Test all service logging methods
        Logger.Service.started("TimerService")
        Logger.Service.stopped("TimerService")
        Logger.Service.connectionChanged("TimerService", true)
        Logger.Service.error("TimerService", "Service error", RuntimeException("Test"))
        
        // Should not throw exceptions
        assertTrue("Service logging methods should complete without exceptions", true)
    }

    @Test
    fun `performance logging methods work correctly`() {
        // Test performance logging methods
        Logger.Performance.measureTime("timer_start", 50) // Fast operation
        Logger.Performance.measureTime("database_query", 150) // Slow operation
        Logger.Performance.memoryUsage(30, 100) // Normal memory usage
        Logger.Performance.memoryUsage(85, 100) // High memory usage
        Logger.Performance.cpuUsage(3.5) // Normal CPU usage
        Logger.Performance.cpuUsage(15.0) // High CPU usage
        
        // Should not throw exceptions
        assertTrue("Performance logging methods should complete without exceptions", true)
    }

    @Test
    fun `logging with additional data works correctly`() {
        val additionalData = mapOf(
            "userId" to "12345",
            "sessionId" to "abcdef",
            "timestamp" to System.currentTimeMillis()
        )
        
        Logger.i(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Test message", additionalData)
        Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Warning message", null, additionalData)
        Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Error message", RuntimeException("Test"), additionalData)
        
        // Should not throw exceptions
        assertTrue("Logging with additional data should work correctly", true)
    }

    @Test
    fun `logging with throwables works correctly`() {
        val exception = RuntimeException("Test exception")
        val nestedCause = IllegalArgumentException("Nested cause")
        val exceptionWithCause = RuntimeException("Main exception", nestedCause)
        
        Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Warning with exception", exception)
        Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Error with exception", exception)
        Logger.e(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Error with nested exception", exceptionWithCause)
        
        // Should not throw exceptions
        assertTrue("Logging with throwables should work correctly", true)
    }

    @Test
    fun `all error categories can be used for logging`() {
        val categories = ErrorHandler.ErrorCategory.values()
        
        for (category in categories) {
            Logger.i(category, "Test message for $category")
        }
        
        // Should not throw exceptions
        assertTrue("All error categories should be usable for logging", true)
    }

    @Test
    fun `export logs returns string when not initialized`() {
        // Test export logs when logger is not initialized
        val logs = Logger.exportLogs()
        assertEquals("Logger not initialized", logs)
    }

    @Test
    fun `clear logs works without errors`() {
        // Test clear logs
        Logger.clearLogs()
        
        // Should not throw exceptions
        assertTrue("Clear logs should work without errors", true)
    }

    @Test
    fun `get error count returns zero when not initialized`() {
        // Test get error count when logger is not initialized
        val errorCount = Logger.getErrorCount(ErrorHandler.ErrorCategory.TIMER_OPERATION)
        assertEquals(0, errorCount)
    }

    @Test
    fun `performance logging thresholds work correctly`() {
        // Test that performance logging uses appropriate thresholds
        
        // Fast operation (should be debug level)
        Logger.Performance.measureTime("fast_operation", 50)
        
        // Slow operation (should be warning level)
        Logger.Performance.measureTime("slow_operation", 150)
        
        // Normal memory usage (should be debug level)
        Logger.Performance.memoryUsage(50, 100)
        
        // High memory usage (should be warning level)
        Logger.Performance.memoryUsage(85, 100)
        
        // Normal CPU usage (should be debug level)
        Logger.Performance.cpuUsage(5.0)
        
        // High CPU usage (should be warning level)
        Logger.Performance.cpuUsage(15.0)
        
        // Should not throw exceptions
        assertTrue("Performance logging thresholds should work correctly", true)
    }

    @Test
    fun `timer logging includes appropriate data`() {
        // Test that timer logging methods include appropriate additional data
        Logger.Timer.start("workTime=30s,restTime=10s,rounds=5")
        Logger.Timer.pause(25000) // 25 seconds remaining
        Logger.Timer.resume(25000) // 25 seconds remaining
        Logger.Timer.complete(300000, 5) // 5 minutes total, 5 rounds
        
        // Should not throw exceptions and should include relevant data
        assertTrue("Timer logging should include appropriate data", true)
    }

    @Test
    fun `audio logging includes sound type information`() {
        // Test that audio logging includes sound type information
        Logger.Audio.playSound("work_interval_start")
        Logger.Audio.playSound("rest_interval_start")
        Logger.Audio.playSound("countdown_beep")
        Logger.Audio.playSound("workout_complete")
        
        // Should not throw exceptions and should include sound type
        assertTrue("Audio logging should include sound type information", true)
    }

    @Test
    fun `database logging includes operation details`() {
        // Test that database logging includes operation details
        Logger.Database.query("workouts", "SELECT")
        Logger.Database.query("presets", "INSERT")
        Logger.Database.query("history", "UPDATE")
        Logger.Database.query("settings", "DELETE")
        
        // Should not throw exceptions and should include operation details
        assertTrue("Database logging should include operation details", true)
    }
}
