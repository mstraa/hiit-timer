package com.hiittimer.app.timer

import com.hiittimer.app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Test the new BEGIN state functionality in TimerManager
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BeginStateTest {
    
    private lateinit var timerManager: TimerManager
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        timerManager = TimerManager()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        timerManager.cleanup()
    }
    
    @Test
    fun `timer starts in BEGIN state with countdown`() {
        val config = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 10,
            totalRounds = 3,
            countdownDurationSeconds = 5
        )
        
        // Start timer
        timerManager.start(config)
        
        // Should be in BEGIN state
        val status = timerManager.timerStatus.value
        assertEquals(TimerState.BEGIN, status.state)
        assertEquals(5, status.timeRemainingSeconds)
        assertEquals(0, status.timeRemainingMilliseconds)
        assertEquals("Start in 5", status.countdownText)
        assertFalse(status.shouldFlashBlue)
    }
    
    @Test
    fun `BEGIN state transitions to RUNNING after countdown`() = runTest {
        val config = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 10,
            totalRounds = 3,
            countdownDurationSeconds = 3 // Minimum valid countdown for testing
        )
        
        // Start timer
        timerManager.start(config)
        
        // Should be in BEGIN state
        assertEquals(TimerState.BEGIN, timerManager.timerStatus.value.state)
        
        // Advance time to complete countdown
        advanceTimeBy(3100) // 3.1 seconds to complete 3-second countdown
        
        // Should now be in RUNNING state with work interval
        val status = timerManager.timerStatus.value
        assertEquals(TimerState.RUNNING, status.state)
        assertEquals(IntervalType.WORK, status.currentInterval)
        assertEquals(30, status.timeRemainingSeconds)
        assertNull(status.countdownText)
        assertFalse(status.shouldFlashBlue)
    }
    
    @Test
    fun `BEGIN state cannot be paused`() {
        val config = TimerConfig(countdownDurationSeconds = 5)
        
        // Start timer (should be in BEGIN state)
        timerManager.start(config)
        assertEquals(TimerState.BEGIN, timerManager.timerStatus.value.state)
        
        // Try to pause - should have no effect
        timerManager.pause()
        
        // Should still be in BEGIN state
        assertEquals(TimerState.BEGIN, timerManager.timerStatus.value.state)
    }
    
    @Test
    fun `reset from any state returns to IDLE with config values`() {
        val config = TimerConfig(
            workTimeSeconds = 45,
            restTimeSeconds = 15,
            totalRounds = 4,
            countdownDurationSeconds = 3
        )
        
        // Start timer
        timerManager.start(config)
        assertEquals(TimerState.BEGIN, timerManager.timerStatus.value.state)
        
        // Reset
        timerManager.reset()
        
        // Should be back to IDLE with config values
        val status = timerManager.timerStatus.value
        assertEquals(TimerState.STOPPED, status.state)
        assertEquals(45, status.timeRemainingSeconds) // Should show work time
        assertEquals(0, status.timeRemainingMilliseconds)
        assertEquals(1, status.currentRound)
        assertEquals(IntervalType.WORK, status.currentInterval)
        assertNull(status.countdownText)
        assertFalse(status.shouldFlashBlue)
    }
    
    @Test
    fun `timer config includes countdown duration`() {
        val config = TimerConfig(
            workTimeSeconds = 20,
            restTimeSeconds = 10,
            totalRounds = 5,
            countdownDurationSeconds = 7
        )
        
        assertEquals(20, config.workTimeSeconds)
        assertEquals(10, config.restTimeSeconds)
        assertEquals(5, config.totalRounds)
        assertEquals(7, config.countdownDurationSeconds)
    }
    
    @Test
    fun `countdown duration validation works`() {
        // Valid countdown duration
        val validConfig = TimerConfig(countdownDurationSeconds = 5)
        assertEquals(5, validConfig.countdownDurationSeconds)
        
        // Invalid countdown duration should throw
        try {
            TimerConfig(countdownDurationSeconds = 2) // Below minimum of 3
            fail("Should have thrown exception for invalid countdown duration")
        } catch (e: IllegalArgumentException) {
            assertTrue("Exception message should mention countdown duration",
                e.message?.contains("Countdown duration") == true)
        }

        try {
            TimerConfig(countdownDurationSeconds = 15) // Above maximum of 10
            fail("Should have thrown exception for invalid countdown duration")
        } catch (e: IllegalArgumentException) {
            assertTrue("Exception message should mention countdown duration",
                e.message?.contains("Countdown duration") == true)
        }
    }
    
    @Test
    fun `timer status helper methods work with BEGIN state`() {
        val config = TimerConfig(countdownDurationSeconds = 5)
        timerManager.start(config)
        
        val status = timerManager.timerStatus.value
        assertTrue(status.isBegin)
        assertFalse(status.isRunning)
        assertFalse(status.isPaused)
        assertFalse(status.isFinished)
        assertFalse(status.canStart) // Cannot start when already in BEGIN state
        assertFalse(status.canPause)
        assertFalse(status.canResume)
        assertTrue(status.canReset) // Can reset during BEGIN state
    }
}
