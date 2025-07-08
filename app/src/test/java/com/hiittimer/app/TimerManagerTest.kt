package com.hiittimer.app

import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Timer data classes and basic functionality
 */
class TimerManagerTest {

    @Test
    fun `timer config creates with correct default values`() {
        val config = TimerConfig()

        assertEquals(20, config.workTimeSeconds)
        assertEquals(10, config.restTimeSeconds)
        assertEquals(5, config.totalRounds)
        assertEquals(false, config.isUnlimited)
    }

    @Test
    fun `timer status has correct initial state`() {
        val status = TimerStatus()

        assertEquals(TimerState.IDLE, status.state)
        assertEquals(IntervalType.WORK, status.currentInterval)
        assertEquals(0, status.timeRemainingSeconds)
        assertEquals(1, status.currentRound)
        assertTrue(status.canStart)
        assertFalse(status.canPause)
        assertFalse(status.canResume)
        assertFalse(status.canReset)
    }

    @Test
    fun `timer status properties work correctly`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 15, totalRounds = 3)
        val status = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.REST,
            timeRemainingSeconds = 75,
            currentRound = 2,
            config = config
        )

        assertEquals(TimerState.RUNNING, status.state)
        assertEquals(IntervalType.REST, status.currentInterval)
        assertTrue(status.isRestInterval)
        assertFalse(status.isWorkInterval)
        assertTrue(status.isRunning)
        assertFalse(status.isPaused)
        assertFalse(status.canStart)
        assertTrue(status.canPause)
        assertFalse(status.canResume)
        assertTrue(status.canReset)
    }

    @Test
    fun `timer config validation works correctly`() {
        // Valid config
        val validConfig = TimerConfig(workTimeSeconds = 20, restTimeSeconds = 10, totalRounds = 5)
        assertEquals(20, validConfig.workTimeSeconds)
        assertEquals(10, validConfig.restTimeSeconds)
        assertEquals(5, validConfig.totalRounds)
        
        // Invalid work time (too low)
        try {
            TimerConfig(workTimeSeconds = 4, restTimeSeconds = 10, totalRounds = 5)
            fail("Should have thrown exception for invalid work time")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
        
        // Invalid work time (too high)
        try {
            TimerConfig(workTimeSeconds = 901, restTimeSeconds = 10, totalRounds = 5)
            fail("Should have thrown exception for invalid work time")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
        
        // Invalid rest time (too low)
        try {
            TimerConfig(workTimeSeconds = 20, restTimeSeconds = 4, totalRounds = 5)
            fail("Should have thrown exception for invalid rest time")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
        
        // Invalid rest time (too high)
        try {
            TimerConfig(workTimeSeconds = 20, restTimeSeconds = 301, totalRounds = 5)
            fail("Should have thrown exception for invalid rest time")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun `timer status formatting works correctly`() {
        val config = TimerConfig(workTimeSeconds = 125, restTimeSeconds = 10, totalRounds = 3)
        val status = com.hiittimer.app.data.TimerStatus(
            timeRemainingSeconds = 125,
            currentRound = 2,
            config = config
        )
        
        assertEquals("02:05", status.formatTimeRemaining())
        assertEquals("Round 2 of 3", status.getRoundProgressText())
        
        // Test unlimited rounds
        val unlimitedConfig = TimerConfig(workTimeSeconds = 20, restTimeSeconds = 10, isUnlimited = true)
        val unlimitedStatus = status.copy(config = unlimitedConfig, currentRound = 7)
        assertEquals("Round 7", unlimitedStatus.getRoundProgressText())
    }
}
