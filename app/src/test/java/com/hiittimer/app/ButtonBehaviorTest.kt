package com.hiittimer.app

import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.IntervalType
import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify the exact button behavior described in the user requirements
 */
class ButtonBehaviorTest {

    @Test
    fun `verify button behavior matches user requirements`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // Test STOPPED/IDLE state
        val stoppedStatus = TimerStatus(
            state = TimerState.IDLE,
            timeRemainingSeconds = config.workTimeSeconds,
            config = config
        )
        
        // When timer is STOPPED: Show "Start" button, reset disabled per Section 12
        assertTrue("Start button should be available when stopped", stoppedStatus.canStart)
        assertFalse("Pause button should not be available when stopped", stoppedStatus.canPause)
        assertFalse("Resume button should not be available when stopped", stoppedStatus.canResume)
        assertFalse("Reset button should be disabled when stopped", stoppedStatus.canReset) // Section 12 requirement
        
        // Test RUNNING state
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        
        // When timer is RUNNING: Show "Pause" button and DISABLE reset button
        assertFalse("Start button should not be available when running", runningStatus.canStart)
        assertTrue("Pause button should be available when running", runningStatus.canPause)
        assertFalse("Resume button should not be available when running", runningStatus.canResume)
        assertFalse("Reset button should be DISABLED when running", runningStatus.canReset)
        
        // Test PAUSED state
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        
        // When timer is PAUSED: Show "Resume" button and ENABLE reset button
        assertFalse("Start button should not be available when paused", pausedStatus.canStart)
        assertFalse("Pause button should not be available when paused", pausedStatus.canPause)
        assertTrue("Resume button should be available when paused", pausedStatus.canResume)
        assertTrue("Reset button should be ENABLED when paused", pausedStatus.canReset)
        
        // Test button text logic
        val startButtonText = when {
            stoppedStatus.canStart -> "Start"
            stoppedStatus.canPause -> "Pause"
            stoppedStatus.canResume -> "Resume"
            else -> "Start"
        }
        assertEquals("Button should show Start when stopped", "Start", startButtonText)
        
        val pauseButtonText = when {
            runningStatus.canStart -> "Start"
            runningStatus.canPause -> "Pause"
            runningStatus.canResume -> "Resume"
            else -> "Start"
        }
        assertEquals("Button should show Pause when running", "Pause", pauseButtonText)
        
        val resumeButtonText = when {
            pausedStatus.canStart -> "Start"
            pausedStatus.canPause -> "Pause"
            pausedStatus.canResume -> "Resume"
            else -> "Start"
        }
        assertEquals("Button should show Resume when paused", "Resume", resumeButtonText)
    }
    
    @Test
    fun `verify reset button behavior from different states`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // Test reset from PAUSED state
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 15,
            currentRound = 2,
            config = config
        )
        
        assertTrue("Reset should work from PAUSED state", pausedStatus.canReset)
        
        // After reset, should return to IDLE
        val afterResetStatus = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = config.workTimeSeconds,
            currentRound = 1,
            config = config
        )
        
        assertTrue("Should be able to start after reset", afterResetStatus.canStart)
        assertFalse("Should not be able to pause after reset", afterResetStatus.canPause)
        assertFalse("Should not be able to resume after reset", afterResetStatus.canResume)
        assertFalse("Reset button should be disabled after reset", afterResetStatus.canReset) // Section 12 requirement
    }
}
