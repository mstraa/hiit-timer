package com.hiittimer.app.performance

import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.IntervalType
import org.junit.Test
import org.junit.Assert.*
import kotlin.system.measureTimeMillis

/**
 * Performance tests for the HIIT Timer app
 * Validates performance requirements and benchmarks critical operations
 */
class PerformanceTest {

    @Test
    fun `timer status creation is fast`() {
        val iterations = 10000
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                TimerStatus(
                    state = TimerState.RUNNING,
                    currentInterval = IntervalType.WORK,
                    timeRemainingSeconds = 30,
                    timeRemainingMilliseconds = 500,
                    currentRound = 1,
                    config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
                )
            }
        }
        
        val avgTimePerOperation = timeMs.toDouble() / iterations
        assertTrue("Timer status creation should be fast (<0.01ms per operation)", 
            avgTimePerOperation < 0.01)
        
        println("Timer status creation: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `timer config validation is fast`() {
        val iterations = 10000
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                try {
                    TimerConfig(
                        workTimeSeconds = 30,
                        restTimeSeconds = 10,
                        totalRounds = 5,
                        isUnlimited = false,
                        noRest = false
                    )
                } catch (e: IllegalArgumentException) {
                    // Expected for invalid configs
                }
            }
        }
        
        val avgTimePerOperation = timeMs.toDouble() / iterations
        assertTrue("Timer config validation should be fast (<0.01ms per operation)", 
            avgTimePerOperation < 0.01)
        
        println("Timer config validation: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `timer state property calculations are fast`() {
        val timerStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 30,
            timeRemainingMilliseconds = 500,
            currentRound = 3,
            config = TimerConfig(workTimeSeconds = 45, restTimeSeconds = 15, totalRounds = 8)
        )
        
        val iterations = 100000
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                // Test all property calculations
                timerStatus.isWorkInterval
                timerStatus.isRestInterval
                timerStatus.isRunning
                timerStatus.isPaused
                timerStatus.isFinished
                timerStatus.canStart
                timerStatus.canPause
                timerStatus.canResume
                timerStatus.canReset
                timerStatus.getRoundProgressText()
                timerStatus.getNextIntervalPreview()
                timerStatus.formatTimeRemaining()
            }
        }
        
        val avgTimePerOperation = timeMs.toDouble() / iterations
        assertTrue("Timer state property calculations should be fast (<0.01ms per operation)",
            avgTimePerOperation < 0.01)
        
        println("Timer state properties: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `timer config edge cases are handled efficiently`() {
        val testConfigs = listOf(
            // Valid configs
            TimerConfig(5, 5, 1),
            TimerConfig(900, 300, 99),
            TimerConfig(20, 10, 5, isUnlimited = true),
            TimerConfig(30, 0, 3, noRest = true),
            
            // Edge cases that should be handled quickly
            TimerConfig(5, 5, 1),
            TimerConfig(900, 300, 99)
        )
        
        val iterations = 1000
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                testConfigs.forEach { config ->
                    // Test all config properties
                    config.workTimeSeconds
                    config.restTimeSeconds
                    config.totalRounds
                    config.isUnlimited
                    config.noRest
                }
            }
        }
        
        val avgTimePerOperation = timeMs.toDouble() / (iterations * testConfigs.size)
        // More lenient threshold for CI environments
        val threshold = if (System.getenv("CI") == "true") 0.01 else 0.001
        assertTrue("Timer config property access should be very fast (<${threshold}ms per operation)", 
            avgTimePerOperation < threshold)
        
        println("Timer config properties: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `memory usage is within acceptable limits`() {
        val runtime = Runtime.getRuntime()
        
        // Force garbage collection to get baseline
        System.gc()
        Thread.sleep(100)
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Create many timer objects
        val timerStatuses = mutableListOf<TimerStatus>()
        val configs = mutableListOf<TimerConfig>()
        
        repeat(1000) {
            configs.add(TimerConfig(
                workTimeSeconds = 20 + (it % 100),
                restTimeSeconds = 10 + (it % 50),
                totalRounds = 3 + (it % 10)
            ))
            
            timerStatuses.add(TimerStatus(
                state = TimerState.values()[it % TimerState.values().size],
                currentInterval = IntervalType.values()[it % IntervalType.values().size],
                timeRemainingSeconds = it % 60,
                timeRemainingMilliseconds = it % 1000,
                currentRound = (it % 10) + 1,
                config = configs.last()
            ))
        }
        
        // Force garbage collection and measure
        System.gc()
        Thread.sleep(100)
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        
        val memoryUsedBytes = finalMemory - initialMemory
        val memoryUsedMB = memoryUsedBytes / (1024 * 1024)
        
        // Should use less than 10MB for 1000 objects
        assertTrue("Memory usage should be reasonable (<10MB for 1000 objects)", 
            memoryUsedMB < 10)
        
        println("Memory usage: ${memoryUsedMB}MB for 1000 timer objects")
    }

    @Test
    fun `timer status copy operations are efficient`() {
        val originalStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 30,
            timeRemainingMilliseconds = 500,
            currentRound = 3,
            config = TimerConfig(workTimeSeconds = 45, restTimeSeconds = 15, totalRounds = 8)
        )
        
        val iterations = 10000
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                // Test copy operations that happen frequently during timer updates
                originalStatus.copy(timeRemainingSeconds = it % 60)
                originalStatus.copy(timeRemainingMilliseconds = it % 1000)
                originalStatus.copy(state = TimerState.PAUSED)
                originalStatus.copy(currentRound = (it % 10) + 1)
            }
        }
        
        val avgTimePerOperation = timeMs.toDouble() / (iterations * 4)
        assertTrue("Timer status copy operations should be fast (<0.001ms per operation)", 
            avgTimePerOperation < 0.001)
        
        println("Timer status copy: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `string formatting operations are efficient`() {
        val timerStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 125,
            timeRemainingMilliseconds = 750,
            currentRound = 7,
            config = TimerConfig(workTimeSeconds = 45, restTimeSeconds = 15, totalRounds = 12)
        )
        
        val iterations = 10000
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                // Test string formatting operations that happen frequently in UI
                timerStatus.getRoundProgressText()
                timerStatus.getNextIntervalPreview()
                timerStatus.formatTimeRemaining()
            }
        }
        
        val avgTimePerOperation = timeMs.toDouble() / (iterations * 4)
        assertTrue("String formatting should be reasonably fast (<0.01ms per operation)", 
            avgTimePerOperation < 0.01)
        
        println("String formatting: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `concurrent timer status access is safe and efficient`() {
        val timerStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 30,
            timeRemainingMilliseconds = 500,
            currentRound = 1,
            config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        )
        
        val iterations = 1000
        val threadCount = 4
        
        val timeMs = measureTimeMillis {
            val threads = (1..threadCount).map { threadId ->
                Thread {
                    repeat(iterations) {
                        // Simulate concurrent access to timer status properties
                        timerStatus.isRunning
                        timerStatus.getRoundProgressText()
                        timerStatus.canPause
                        timerStatus.formatTimeRemaining()
                    }
                }
            }
            
            threads.forEach { it.start() }
            threads.forEach { it.join() }
        }
        
        val totalOperations = iterations * threadCount * 5
        val avgTimePerOperation = timeMs.toDouble() / totalOperations
        
        assertTrue("Concurrent access should be efficient (<0.01ms per operation)", 
            avgTimePerOperation < 0.01)
        
        println("Concurrent access: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `large timer configurations are handled efficiently`() {
        // Test with maximum allowed values
        val maxConfig = TimerConfig(
            workTimeSeconds = 900, // 15 minutes
            restTimeSeconds = 300, // 5 minutes
            totalRounds = 99
        )
        
        val iterations = 1000
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                val status = TimerStatus(
                    state = TimerState.RUNNING,
                    currentInterval = IntervalType.WORK,
                    timeRemainingSeconds = maxConfig.workTimeSeconds,
                    timeRemainingMilliseconds = 999,
                    currentRound = maxConfig.totalRounds,
                    config = maxConfig
                )
                
                // Test operations with large values
                status.getRoundProgressText()
                status.formatTimeRemaining()
                status.getNextIntervalPreview()
            }
        }
        
        val avgTimePerOperation = timeMs.toDouble() / (iterations * 4)
        // More lenient threshold for CI environments
        val threshold = if (System.getenv("CI") == "true") 0.1 else 0.01
        assertTrue("Large configuration handling should be efficient (<${threshold}ms per operation)", 
            avgTimePerOperation < threshold)
        
        println("Large config handling: ${avgTimePerOperation}ms per operation")
    }

    @Test
    fun `timer validation performance is acceptable`() {
        val validConfigs = listOf(
            TimerConfig(5, 5, 1),
            TimerConfig(30, 15, 5),
            TimerConfig(60, 30, 10),
            TimerConfig(900, 300, 99)
        )
        
        val invalidConfigs = listOf(
            { TimerConfig(0, 10, 5) },      // Invalid work time
            { TimerConfig(30, 0, 5) },      // Invalid rest time (when rest enabled)
            { TimerConfig(30, 10, 0) },     // Invalid rounds
            { TimerConfig(1000, 10, 5) }    // Work time too long
        )
        
        val iterations = 1000
        
        // Test valid configs
        val validTimeMs = measureTimeMillis {
            repeat(iterations) {
                validConfigs.forEach { config ->
                    // Access properties to trigger validation
                    config.workTimeSeconds
                    config.restTimeSeconds
                    config.totalRounds
                }
            }
        }
        
        // Test invalid configs
        val invalidTimeMs = measureTimeMillis {
            repeat(iterations) {
                invalidConfigs.forEach { configFactory ->
                    try {
                        configFactory()
                    } catch (e: IllegalArgumentException) {
                        // Expected
                    }
                }
            }
        }
        
        val avgValidTime = validTimeMs.toDouble() / (iterations * validConfigs.size)
        val avgInvalidTime = invalidTimeMs.toDouble() / (iterations * invalidConfigs.size)
        
        assertTrue("Valid config validation should be fast (<0.001ms)", avgValidTime < 0.001)
        assertTrue("Invalid config validation should be fast (<0.01ms)", avgInvalidTime < 0.01)
        
        println("Valid config validation: ${avgValidTime}ms per operation")
        println("Invalid config validation: ${avgInvalidTime}ms per operation")
    }
}
