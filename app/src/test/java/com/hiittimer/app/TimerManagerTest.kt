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
        assertEquals(false, config.noRest)
    }

    @Test
    fun `timer status has correct initial state`() {
        val status = TimerStatus()

        assertEquals(TimerState.IDLE, status.state)
        assertEquals(IntervalType.WORK, status.currentInterval)
        assertEquals(0, status.timeRemainingSeconds)
        assertEquals(0, status.timeRemainingMilliseconds)
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
            timeRemainingMilliseconds = 750,
            currentRound = 2,
            config = config
        )

        assertEquals("02:05.7", status.formatTimeRemaining())
        assertEquals("Round 2 of 3", status.getRoundProgressText())

        // Test unlimited rounds
        val unlimitedConfig = TimerConfig(workTimeSeconds = 20, restTimeSeconds = 10, isUnlimited = true)
        val unlimitedStatus = status.copy(config = unlimitedConfig, currentRound = 7)
        assertEquals("Round 7", unlimitedStatus.getRoundProgressText())

        // Test millisecond formatting for time >= 60 seconds
        val statusWithMs = TimerStatus(
            timeRemainingSeconds = 65,
            timeRemainingMilliseconds = 234,
            config = config
        )
        assertEquals("01:05.2", statusWithMs.formatTimeRemaining())

        // Test FR-019: Conditional formatting for time < 60 seconds
        val statusUnder60 = TimerStatus(
            timeRemainingSeconds = 45,
            timeRemainingMilliseconds = 300,
            config = config
        )
        assertEquals("45.3", statusUnder60.formatTimeRemaining())

        // Test edge case: exactly 60 seconds
        val statusExactly60 = TimerStatus(
            timeRemainingSeconds = 60,
            timeRemainingMilliseconds = 0,
            config = config
        )
        assertEquals("01:00.0", statusExactly60.formatTimeRemaining())

        // Test edge case: less than 10 seconds
        val statusUnder10 = TimerStatus(
            timeRemainingSeconds = 5,
            timeRemainingMilliseconds = 800,
            config = config
        )
        assertEquals("05.8", statusUnder10.formatTimeRemaining())
    }

    @Test
    fun `timer config with no rest works correctly`() {
        // Test "No Rest" mode configuration (FR-001)
        val noRestConfig = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 10, // This should be ignored when noRest = true
            totalRounds = 3,
            isUnlimited = false,
            noRest = true
        )

        assertEquals(30, noRestConfig.workTimeSeconds)
        assertEquals(10, noRestConfig.restTimeSeconds)
        assertEquals(3, noRestConfig.totalRounds)
        assertEquals(false, noRestConfig.isUnlimited)
        assertEquals(true, noRestConfig.noRest)

        // Test that rest time validation is skipped when noRest = true
        val noRestConfigWithInvalidRest = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 1, // This would normally be invalid, but should be allowed with noRest = true
            totalRounds = 3,
            isUnlimited = false,
            noRest = true
        )

        assertEquals(true, noRestConfigWithInvalidRest.noRest)
    }
}
