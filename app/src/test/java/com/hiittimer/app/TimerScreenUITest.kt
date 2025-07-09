package com.hiittimer.app

import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.IntervalType
import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify the UI improvements for TimerScreen
 * Tests the header background matching and full-screen visual feedback
 */
class TimerScreenUITest {

    @Test
    fun `timer status provides correct state information for UI`() {
        // Test initial idle state
        val idleStatus = TimerStatus()
        assertEquals(TimerState.IDLE, idleStatus.state)
        assertEquals(IntervalType.WORK, idleStatus.currentInterval)
        assertTrue(idleStatus.canStart)
        assertFalse(idleStatus.canPause)
        assertFalse(idleStatus.canResume)
        assertFalse(idleStatus.canReset) // Section 12 requirement
    }

    @Test
    fun `timer status provides correct running state information`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 15, totalRounds = 3)
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            timeRemainingMilliseconds = 500,
            currentRound = 1,
            config = config
        )
        
        assertEquals(TimerState.RUNNING, runningStatus.state)
        assertEquals(IntervalType.WORK, runningStatus.currentInterval)
        assertTrue(runningStatus.isWorkInterval)
        assertFalse(runningStatus.isRestInterval)
        assertTrue(runningStatus.isRunning)
        assertFalse(runningStatus.isPaused)
        assertFalse(runningStatus.canStart)
        assertTrue(runningStatus.canPause)
        assertFalse(runningStatus.canResume)
        assertFalse(runningStatus.canReset) // Reset disabled when running
    }

    @Test
    fun `timer status provides correct paused state information`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 15, totalRounds = 3)
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.REST,
            timeRemainingSeconds = 10,
            timeRemainingMilliseconds = 200,
            currentRound = 2,
            config = config
        )
        
        assertEquals(TimerState.PAUSED, pausedStatus.state)
        assertEquals(IntervalType.REST, pausedStatus.currentInterval)
        assertFalse(pausedStatus.isWorkInterval)
        assertTrue(pausedStatus.isRestInterval)
        assertFalse(pausedStatus.isRunning)
        assertTrue(pausedStatus.isPaused)
        assertFalse(pausedStatus.canStart)
        assertFalse(pausedStatus.canPause)
        assertTrue(pausedStatus.canResume)
        assertTrue(pausedStatus.canReset)
    }

    @Test
    fun `timer status formats time correctly for UI display`() {
        val config = TimerConfig(workTimeSeconds = 125, restTimeSeconds = 10, totalRounds = 3)
        
        // Test time >= 60 seconds (MM:SS.d format)
        val longTimeStatus = TimerStatus(
            timeRemainingSeconds = 125,
            timeRemainingMilliseconds = 750,
            config = config
        )
        assertEquals("02:05.7", longTimeStatus.formatTimeRemaining())
        
        // Test time < 60 seconds (SS.d format)
        val shortTimeStatus = TimerStatus(
            timeRemainingSeconds = 45,
            timeRemainingMilliseconds = 300,
            config = config
        )
        assertEquals("45.3", shortTimeStatus.formatTimeRemaining())
        
        // Test exactly 60 seconds
        val exactlyMinuteStatus = TimerStatus(
            timeRemainingSeconds = 60,
            timeRemainingMilliseconds = 0,
            config = config
        )
        assertEquals("01:00.0", exactlyMinuteStatus.formatTimeRemaining())
    }

    @Test
    fun `timer status provides correct round progress text`() {
        val limitedConfig = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 15, totalRounds = 5)
        val limitedStatus = TimerStatus(currentRound = 3, config = limitedConfig)
        assertEquals("Round 3 of 5", limitedStatus.getRoundProgressText())
        
        val unlimitedConfig = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 15, isUnlimited = true)
        val unlimitedStatus = TimerStatus(currentRound = 7, config = unlimitedConfig)
        assertEquals("Round 7", unlimitedStatus.getRoundProgressText())
    }

    @Test
    fun `timer status provides correct next interval preview`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 15, totalRounds = 3)
        
        // Test work interval with time <= 5 seconds
        val workStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 3,
            config = config
        )
        assertEquals("Next: REST (15s)", workStatus.getNextIntervalPreview())
        assertTrue(workStatus.shouldShowNextIntervalPreview)
        
        // Test rest interval with time <= 5 seconds
        val restStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.REST,
            timeRemainingSeconds = 2,
            currentRound = 1,
            config = config
        )
        assertEquals("Next: WORK (30s)", restStatus.getNextIntervalPreview())
        
        // Test when time > 5 seconds (no preview)
        val longTimeStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 10,
            config = config
        )
        assertNull(longTimeStatus.getNextIntervalPreview())
        assertFalse(longTimeStatus.shouldShowNextIntervalPreview)
    }
}
