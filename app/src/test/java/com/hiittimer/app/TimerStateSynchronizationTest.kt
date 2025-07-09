package com.hiittimer.app

import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.IntervalType
import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify timer state synchronization and button text logic
 */
class TimerStateSynchronizationTest {

    @Test
    fun `verify timer state properties are correctly defined`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // Test IDLE state properties
        val idleStatus = TimerStatus(
            state = TimerState.IDLE,
            timeRemainingSeconds = config.workTimeSeconds,
            config = config
        )
        
        assertTrue("IDLE: canStart should be true", idleStatus.canStart)
        assertFalse("IDLE: canPause should be false", idleStatus.canPause)
        assertFalse("IDLE: canResume should be false", idleStatus.canResume)
        assertTrue("IDLE: canReset should be true", idleStatus.canReset)
        
        // Test RUNNING state properties
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        
        assertFalse("RUNNING: canStart should be false", runningStatus.canStart)
        assertTrue("RUNNING: canPause should be true", runningStatus.canPause)
        assertFalse("RUNNING: canResume should be false", runningStatus.canResume)
        assertFalse("RUNNING: canReset should be false", runningStatus.canReset)
        
        // Test PAUSED state properties
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        
        assertFalse("PAUSED: canStart should be false", pausedStatus.canStart)
        assertFalse("PAUSED: canPause should be false", pausedStatus.canPause)
        assertTrue("PAUSED: canResume should be true", pausedStatus.canResume)
        assertTrue("PAUSED: canReset should be true", pausedStatus.canReset)
    }
    
    @Test
    fun `verify button text logic matches expected behavior`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // Test button text for IDLE state
        val idleStatus = TimerStatus(state = TimerState.IDLE, config = config)
        val idleButtonText = getButtonText(idleStatus)
        assertEquals("IDLE state should show Start button", "Start", idleButtonText)
        
        // Test button text for RUNNING state
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        val runningButtonText = getButtonText(runningStatus)
        assertEquals("RUNNING state should show Pause button", "Pause", runningButtonText)
        
        // Test button text for PAUSED state
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        val pausedButtonText = getButtonText(pausedStatus)
        assertEquals("PAUSED state should show Resume button", "Resume", pausedButtonText)
    }
    
    @Test
    fun `verify state transitions maintain correct button states`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // Start with IDLE
        val idleStatus = TimerStatus(state = TimerState.IDLE, config = config)
        assertEquals("Start", getButtonText(idleStatus))
        assertTrue("Should be able to start from IDLE", idleStatus.canStart)
        
        // Transition to RUNNING
        val runningStatus = idleStatus.copy(state = TimerState.RUNNING)
        assertEquals("Pause", getButtonText(runningStatus))
        assertTrue("Should be able to pause from RUNNING", runningStatus.canPause)
        
        // Transition to PAUSED
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertEquals("Resume", getButtonText(pausedStatus))
        assertTrue("Should be able to resume from PAUSED", pausedStatus.canResume)
        
        // Transition back to RUNNING
        val resumedStatus = pausedStatus.copy(state = TimerState.RUNNING)
        assertEquals("Pause", getButtonText(resumedStatus))
        assertTrue("Should be able to pause again after resume", resumedStatus.canPause)
    }
    
    @Test
    fun `verify reset button behavior matches requirements`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 5)
        
        // IDLE state - reset should be enabled
        val idleStatus = TimerStatus(state = TimerState.IDLE, config = config)
        assertTrue("Reset should be enabled when IDLE", idleStatus.canReset)
        
        // RUNNING state - reset should be disabled
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 25,
            currentRound = 1,
            config = config
        )
        assertFalse("Reset should be disabled when RUNNING", runningStatus.canReset)
        
        // PAUSED state - reset should be enabled
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertTrue("Reset should be enabled when PAUSED", pausedStatus.canReset)
        
        // FINISHED state - reset should be enabled
        val finishedStatus = TimerStatus(state = TimerState.FINISHED, config = config)
        assertTrue("Reset should be enabled when FINISHED", finishedStatus.canReset)
    }
    
    /**
     * Helper function that mimics the button text logic from TimerScreen.kt
     */
    private fun getButtonText(timerStatus: TimerStatus): String {
        return when {
            timerStatus.canStart -> "Start"
            timerStatus.canPause -> "Pause"
            timerStatus.canResume -> "Resume"
            else -> "Start"
        }
    }
}
