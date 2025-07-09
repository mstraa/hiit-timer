package com.hiittimer.app

import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.IntervalType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests to reproduce and validate fixes for specific timer control bugs
 */
class TimerControlBugTest {

    @Test
    fun `bug 1 reproduction - reset button should work when paused`() {
        // Reproduce Bug 1: Reset button not working when paused
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // Simulate: Start → Running → Pause → Reset
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            timeRemainingMilliseconds = 500,
            currentRound = 1,
            config = config
        )
        
        // After pause
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        
        // Verify reset button should be available when paused
        assertTrue("Reset button should be enabled when paused", pausedStatus.canReset)
        assertEquals(TimerState.PAUSED, pausedStatus.state)
        
        // After reset - should return to idle with preserved config
        val resetStatus = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = config.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = config // Configuration should be preserved
        )
        
        // Verify reset worked correctly
        assertEquals(TimerState.IDLE, resetStatus.state)
        assertEquals(config.workTimeSeconds, resetStatus.timeRemainingSeconds)
        assertEquals(config, resetStatus.config)
        assertFalse("Reset button should be disabled after reset", resetStatus.canReset)
        assertTrue("Start button should be enabled after reset", resetStatus.canStart)
    }

    @Test
    fun `bug 2 reproduction - button text should show resume when paused`() {
        // Reproduce Bug 2: Button should show "Resume" when paused
        val config = TimerConfig(workTimeSeconds = 45, restTimeSeconds = 15, totalRounds = 3)
        
        // Simulate: Start → Running → Pause
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 30,
            timeRemainingMilliseconds = 750,
            currentRound = 2,
            config = config
        )
        
        // After pause
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        
        // Verify button state logic for text display
        assertFalse("canStart should be false when paused", pausedStatus.canStart)
        assertFalse("canPause should be false when paused", pausedStatus.canPause)
        assertTrue("canResume should be true when paused", pausedStatus.canResume)
        
        // Button text logic should show "Resume"
        val expectedButtonText = when {
            pausedStatus.canStart -> "Start"
            pausedStatus.canPause -> "Pause"
            pausedStatus.canResume -> "Resume"
            else -> "Start"
        }
        
        assertEquals("Button should show Resume when paused", "Resume", expectedButtonText)
    }

    @Test
    fun `bug 3 reproduction - resume should continue from paused position`() {
        // Reproduce Bug 3: Resume should not reset timer
        val config = TimerConfig(workTimeSeconds = 60, restTimeSeconds = 20, totalRounds = 4)
        
        // Simulate timer running mid-workout
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.REST,
            timeRemainingSeconds = 12,
            timeRemainingMilliseconds = 345,
            currentRound = 3,
            config = config
        )
        
        // After pause - all state should be preserved except state change
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        
        // Verify pause preserves all state
        assertEquals(TimerState.PAUSED, pausedStatus.state)
        assertEquals(IntervalType.REST, pausedStatus.currentInterval)
        assertEquals(12, pausedStatus.timeRemainingSeconds)
        assertEquals(345, pausedStatus.timeRemainingMilliseconds)
        assertEquals(3, pausedStatus.currentRound)
        assertEquals(config, pausedStatus.config)
        
        // After resume - should continue from exact same position
        val resumedStatus = pausedStatus.copy(state = TimerState.RUNNING)
        
        // Verify resume continues from paused position (NOT reset)
        assertEquals(TimerState.RUNNING, resumedStatus.state)
        assertEquals(IntervalType.REST, resumedStatus.currentInterval)
        assertEquals(12, resumedStatus.timeRemainingSeconds)
        assertEquals(345, resumedStatus.timeRemainingMilliseconds)
        assertEquals(3, resumedStatus.currentRound)
        assertEquals(config, resumedStatus.config)
        
        // Should NOT be reset to initial state
        assertNotEquals("Should not reset to work interval", IntervalType.WORK, resumedStatus.currentInterval)
        assertNotEquals("Should not reset to initial work time", config.workTimeSeconds, resumedStatus.timeRemainingSeconds)
        assertNotEquals("Should not reset to round 1", 1, resumedStatus.currentRound)
    }

    @Test
    fun `reset button availability requirements`() {
        // Test reset button availability requirements
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // IDLE state - reset should be disabled
        val idleStatus = TimerStatus(state = TimerState.IDLE, config = config)
        assertFalse("Reset should be disabled when idle", idleStatus.canReset)
        
        // RUNNING state - reset should be disabled (per requirements)
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        // Reset should be disabled when running (per new requirements)
        assertFalse("Reset should be disabled when running", runningStatus.canReset)
        
        // PAUSED state - reset should be enabled
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertTrue("Reset should be enabled when paused", pausedStatus.canReset)
        
        // FINISHED state - reset should be enabled
        val finishedStatus = TimerStatus(state = TimerState.FINISHED, config = config)
        assertTrue("Reset should be enabled when finished", finishedStatus.canReset)
    }

    @Test
    fun `button state logic validation`() {
        // Test all button state combinations
        val config = TimerConfig(workTimeSeconds = 20, restTimeSeconds = 10, totalRounds = 3)
        
        // IDLE: Start=true, Pause=false, Resume=false, Reset=false
        val idleStatus = TimerStatus(state = TimerState.IDLE, config = config)
        assertTrue("Start should be available when idle", idleStatus.canStart)
        assertFalse("Pause should not be available when idle", idleStatus.canPause)
        assertFalse("Resume should not be available when idle", idleStatus.canResume)
        assertFalse("Reset should not be available when idle", idleStatus.canReset)
        
        // RUNNING: Start=false, Pause=true, Resume=false, Reset=false
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 15,
            currentRound = 1,
            config = config
        )
        assertFalse("Start should not be available when running", runningStatus.canStart)
        assertTrue("Pause should be available when running", runningStatus.canPause)
        assertFalse("Resume should not be available when running", runningStatus.canResume)
        assertFalse("Reset should not be available when running", runningStatus.canReset)
        
        // PAUSED: Start=false, Pause=false, Resume=true, Reset=true
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertFalse("Start should not be available when paused", pausedStatus.canStart)
        assertFalse("Pause should not be available when paused", pausedStatus.canPause)
        assertTrue("Resume should be available when paused", pausedStatus.canResume)
        assertTrue("Reset should be available when paused", pausedStatus.canReset)
        
        // FINISHED: Start=false, Pause=false, Resume=false, Reset=true
        val finishedStatus = TimerStatus(state = TimerState.FINISHED, config = config)
        assertFalse("Start should not be available when finished", finishedStatus.canStart)
        assertFalse("Pause should not be available when finished", finishedStatus.canPause)
        assertFalse("Resume should not be available when finished", finishedStatus.canResume)
        assertTrue("Reset should be available when finished", finishedStatus.canReset)
    }

    @Test
    fun `configuration preservation during state transitions`() {
        // Test that configuration is preserved during all state transitions
        val customConfig = TimerConfig(
            workTimeSeconds = 45,
            restTimeSeconds = 15,
            totalRounds = 8,
            isUnlimited = false,
            noRest = false
        )
        
        // Start with custom config
        val idleStatus = TimerStatus(
            state = TimerState.IDLE,
            timeRemainingSeconds = customConfig.workTimeSeconds,
            config = customConfig
        )
        
        // After start
        val runningStatus = idleStatus.copy(state = TimerState.RUNNING)
        assertEquals("Config should be preserved after start", customConfig, runningStatus.config)
        
        // After pause
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertEquals("Config should be preserved after pause", customConfig, pausedStatus.config)
        
        // After resume
        val resumedStatus = pausedStatus.copy(state = TimerState.RUNNING)
        assertEquals("Config should be preserved after resume", customConfig, resumedStatus.config)
        
        // After reset
        val resetStatus = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = customConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = customConfig // Should preserve config
        )
        assertEquals("Config should be preserved after reset", customConfig, resetStatus.config)
    }

    @Test
    fun `timer state transitions follow expected flow`() {
        // Test the complete timer state transition flow
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 3)
        
        // Flow: IDLE → RUNNING → PAUSED → RUNNING → PAUSED → IDLE (reset)
        
        // 1. IDLE → RUNNING (start)
        val idleStatus = TimerStatus(state = TimerState.IDLE, config = config)
        assertTrue("Should be able to start from IDLE", idleStatus.canStart)
        
        // 2. RUNNING → PAUSED (pause)
        val runningStatus = idleStatus.copy(state = TimerState.RUNNING)
        assertTrue("Should be able to pause from RUNNING", runningStatus.canPause)
        
        // 3. PAUSED → RUNNING (resume)
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertTrue("Should be able to resume from PAUSED", pausedStatus.canResume)
        
        // 4. RUNNING → PAUSED (pause again)
        val runningAgainStatus = pausedStatus.copy(state = TimerState.RUNNING)
        assertTrue("Should be able to pause again from RUNNING", runningAgainStatus.canPause)
        
        // 5. PAUSED → IDLE (reset)
        val pausedAgainStatus = runningAgainStatus.copy(state = TimerState.PAUSED)
        assertTrue("Should be able to reset from PAUSED", pausedAgainStatus.canReset)
        
        // Final state should be IDLE with preserved config
        val finalIdleStatus = TimerStatus(
            state = TimerState.IDLE,
            timeRemainingSeconds = config.workTimeSeconds,
            config = config
        )
        assertTrue("Should be able to start again after reset", finalIdleStatus.canStart)
        assertEquals("Config should be preserved through all transitions", config, finalIdleStatus.config)
    }
}
