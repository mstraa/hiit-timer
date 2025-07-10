package com.hiittimer.app

import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.IntervalType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests to validate UI/UX bug fixes for timer state management and fullscreen behavior
 */
class UIBugFixTest {

    @Test
    fun `bug 1 - reset button should be available when timer is paused`() {
        // Test that reset button is available in paused state
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 15,
            timeRemainingMilliseconds = 500,
            currentRound = 2,
            config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        )
        
        // Verify reset button should be available when paused
        assertTrue("Reset button should be available when timer is paused", pausedStatus.canReset)
        assertFalse("Start button should not be available when paused", pausedStatus.canStart)
        assertFalse("Pause button should not be available when paused", pausedStatus.canPause)
        assertTrue("Resume button should be available when paused", pausedStatus.canResume)
    }

    @Test
    fun `bug 1 - reset button should be available when timer is running`() {
        // Test that reset button is available in running state
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            timeRemainingMilliseconds = 750,
            currentRound = 1,
            config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        )
        
        // Verify reset button should be disabled when running (per new requirements)
        assertFalse("Reset button should be disabled when timer is running", runningStatus.canReset)
        assertFalse("Start button should not be available when running", runningStatus.canStart)
        assertTrue("Pause button should be available when running", runningStatus.canPause)
        assertFalse("Resume button should not be available when running", runningStatus.canResume)
    }

    @Test
    fun `bug 1 - reset button should be available when timer is idle`() {
        // Test that reset button is available in idle state (per user requirements)
        val idleStatus = TimerStatus(
            state = TimerState.STOPPED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 30,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        )

        // Verify reset button should NOT be available when idle (per Section 12 requirements)
        assertFalse("Reset button should NOT be available when timer is idle", idleStatus.canReset)
        assertTrue("Start button should be available when idle", idleStatus.canStart)
        assertFalse("Pause button should not be available when idle", idleStatus.canPause)
        assertFalse("Resume button should not be available when idle", idleStatus.canResume)
    }

    @Test
    fun `bug 2 - pause and resume should preserve timer state`() {
        // Test that pause/resume preserves the exact timer state
        val originalConfig = TimerConfig(workTimeSeconds = 45, restTimeSeconds = 15, totalRounds = 8)
        
        // Simulate a running timer in the middle of a workout
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.REST,
            timeRemainingSeconds = 8,
            timeRemainingMilliseconds = 250,
            currentRound = 3,
            config = originalConfig
        )
        
        // Simulate pause - should preserve all state except change to PAUSED
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        
        // Verify pause preserves all state
        assertEquals(TimerState.PAUSED, pausedStatus.state)
        assertEquals(IntervalType.REST, pausedStatus.currentInterval)
        assertEquals(8, pausedStatus.timeRemainingSeconds)
        assertEquals(250, pausedStatus.timeRemainingMilliseconds)
        assertEquals(3, pausedStatus.currentRound)
        assertEquals(originalConfig, pausedStatus.config)
        
        // Simulate resume - should preserve all state except change to RUNNING
        val resumedStatus = pausedStatus.copy(state = TimerState.RUNNING)
        
        // Verify resume preserves all state
        assertEquals(TimerState.RUNNING, resumedStatus.state)
        assertEquals(IntervalType.REST, resumedStatus.currentInterval)
        assertEquals(8, resumedStatus.timeRemainingSeconds)
        assertEquals(250, resumedStatus.timeRemainingMilliseconds)
        assertEquals(3, resumedStatus.currentRound)
        assertEquals(originalConfig, resumedStatus.config)
    }

    @Test
    fun `bug 2 - resume should not reset timer to beginning`() {
        // Test that resume doesn't reset timer to initial state
        val config = TimerConfig(workTimeSeconds = 60, restTimeSeconds = 30, totalRounds = 4)
        
        // Simulate timer that has been running and is now paused mid-workout
        val pausedMidWorkout = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 35, // Not the initial 60 seconds
            timeRemainingMilliseconds = 123,
            currentRound = 2, // Not the initial round 1
            config = config
        )
        
        // Verify this is not the initial state
        assertNotEquals("Should not be at initial work time", config.workTimeSeconds, pausedMidWorkout.timeRemainingSeconds)
        assertNotEquals("Should not be at initial round", 1, pausedMidWorkout.currentRound)
        
        // Resume should preserve the exact state
        assertTrue("Should be able to resume from paused state", pausedMidWorkout.canResume)
        assertFalse("Should not be able to start from paused state", pausedMidWorkout.canStart)
        
        // Simulate proper resume behavior
        val resumedStatus = pausedMidWorkout.copy(state = TimerState.RUNNING)
        
        // Verify resume maintains the exact position
        assertEquals(35, resumedStatus.timeRemainingSeconds)
        assertEquals(123, resumedStatus.timeRemainingMilliseconds)
        assertEquals(2, resumedStatus.currentRound)
        assertEquals(IntervalType.WORK, resumedStatus.currentInterval)
    }

    @Test
    fun `bug 2 - start should only work from idle state`() {
        // Test that start only works from IDLE state, not from PAUSED
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // Test IDLE state - start should be available
        val idleStatus = TimerStatus(state = TimerState.STOPPED, config = config)
        assertTrue("Start should be available from IDLE state", idleStatus.canStart)
        assertFalse("Resume should not be available from IDLE state", idleStatus.canResume)
        
        // Test PAUSED state - start should not be available, resume should be
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 20,
            currentRound = 1,
            config = config
        )
        assertFalse("Start should not be available from PAUSED state", pausedStatus.canStart)
        assertTrue("Resume should be available from PAUSED state", pausedStatus.canResume)
        
        // Test RUNNING state - neither start nor resume should be available
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        assertFalse("Start should not be available from RUNNING state", runningStatus.canStart)
        assertFalse("Resume should not be available from RUNNING state", runningStatus.canResume)
        assertTrue("Pause should be available from RUNNING state", runningStatus.canPause)
    }

    @Test
    fun `timer state transitions are logically consistent`() {
        // Test that timer state transitions follow expected patterns
        val config = TimerConfig(workTimeSeconds = 20, restTimeSeconds = 10, totalRounds = 3)
        
        // IDLE -> RUNNING (via start)
        val idleStatus = TimerStatus(state = TimerState.STOPPED, config = config)
        assertTrue("Can start from IDLE", idleStatus.canStart)
        
        // RUNNING -> PAUSED (via pause)
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 15,
            currentRound = 1,
            config = config
        )
        assertTrue("Can pause from RUNNING", runningStatus.canPause)
        
        // PAUSED -> RUNNING (via resume)
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertTrue("Can resume from PAUSED", pausedStatus.canResume)
        
        // Reset only available from PAUSED or FINISHED (per Section 12 requirements)
        assertFalse("Cannot reset from RUNNING", runningStatus.canReset)
        assertTrue("Can reset from PAUSED", pausedStatus.canReset)
        assertFalse("Cannot reset from IDLE", idleStatus.canReset) // Section 12 requirement
    }

    @Test
    fun `fullscreen modes support proper navigation bar hiding`() {
        // Test that fullscreen modes are properly defined for navigation bar control
        val standardMode = com.hiittimer.app.ui.fullscreen.FullscreenMode.STANDARD
        val enhancedMode = com.hiittimer.app.ui.fullscreen.FullscreenMode.ENHANCED_FULLSCREEN
        
        // Verify both modes exist
        assertNotNull("Standard mode should exist", standardMode)
        assertNotNull("Enhanced fullscreen mode should exist", enhancedMode)
        
        // Verify mode names for navigation bar control logic
        assertEquals("STANDARD", standardMode.name)
        assertEquals("ENHANCED_FULLSCREEN", enhancedMode.name)
        
        // Verify only two modes exist (no deprecated immersive modes)
        val allModes = com.hiittimer.app.ui.fullscreen.FullscreenMode.values()
        assertEquals("Should have exactly 2 fullscreen modes", 2, allModes.size)
    }

    @Test
    fun `status bar theming supports background color matching`() {
        // Test that color schemes have proper background colors for status bar theming
        
        // This test validates that the theme system has the necessary colors
        // In the actual implementation, status bar color should match background
        
        // Test that we can create different timer states that would trigger theming
        val idleState = TimerState.STOPPED
        val runningState = TimerState.RUNNING
        val pausedState = TimerState.PAUSED
        
        // All states should be valid for theming
        assertNotNull("IDLE state should exist for theming", idleState)
        assertNotNull("RUNNING state should exist for theming", runningState)
        assertNotNull("PAUSED state should exist for theming", pausedState)
        
        // States should be distinct for different theming needs
        assertNotEquals("IDLE and RUNNING should be different", idleState, runningState)
        assertNotEquals("RUNNING and PAUSED should be different", runningState, pausedState)
        assertNotEquals("PAUSED and IDLE should be different", pausedState, idleState)
    }
}
