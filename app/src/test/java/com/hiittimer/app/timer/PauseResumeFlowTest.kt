package com.hiittimer.app.timer

import com.hiittimer.app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Integration test for pause-resume flow to debug the resume button issue
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PauseResumeFlowTest {

    private lateinit var timerManager: TimerManager
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        timerManager = TimerManager()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test complete pause resume flow with state validation`() = runTest {
        // Start with basic config
        val config = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 10,
            totalRounds = 3
        )
        
        // 1. Initial state should be STOPPED
        val initialStatus = timerManager.timerStatus.value
        assertEquals("Initial state should be STOPPED", TimerState.STOPPED, initialStatus.state)
        assertTrue("Should be able to start", initialStatus.canStart)
        assertFalse("Should not be able to pause", initialStatus.canPause)
        assertFalse("Should not be able to resume", initialStatus.canResume)
        
        // 2. Start timer (goes to BEGIN state)
        timerManager.start(config)
        delay(50) // Allow state to update
        
        val beginStatus = timerManager.timerStatus.value
        assertEquals("Should be in BEGIN state", TimerState.BEGIN, beginStatus.state)
        assertFalse("Should not be able to start", beginStatus.canStart)
        assertFalse("Should not be able to pause BEGIN", beginStatus.canPause)
        assertFalse("Should not be able to resume", beginStatus.canResume)
        
        // 3. Wait for transition to RUNNING (BEGIN countdown is 5 seconds by default)
        delay(5100) // Wait for BEGIN to complete
        
        val runningStatus = timerManager.timerStatus.value
        assertEquals("Should be in RUNNING state", TimerState.RUNNING, runningStatus.state)
        assertFalse("Should not be able to start", runningStatus.canStart)
        assertTrue("Should be able to pause", runningStatus.canPause)
        assertFalse("Should not be able to resume", runningStatus.canResume)
        
        // 4. Pause the timer
        timerManager.pause()
        delay(50) // Allow state to update
        
        val pausedStatus = timerManager.timerStatus.value
        assertEquals("Should be in PAUSED state", TimerState.PAUSED, pausedStatus.state)
        assertFalse("Should not be able to start", pausedStatus.canStart)
        assertFalse("Should not be able to pause", pausedStatus.canPause)
        assertTrue("CRITICAL: Should be able to resume", pausedStatus.canResume)
        
        // 5. Resume the timer
        timerManager.resume()
        delay(50) // Allow state to update
        
        val resumedStatus = timerManager.timerStatus.value
        assertEquals("Should be back in RUNNING state", TimerState.RUNNING, resumedStatus.state)
        assertFalse("Should not be able to start", resumedStatus.canStart)
        assertTrue("Should be able to pause again", resumedStatus.canPause)
        assertFalse("Should not be able to resume", resumedStatus.canResume)
        
        // Clean up
        timerManager.cleanup()
    }
    
    @Test
    fun `test pause resume state properties directly`() {
        val config = TimerConfig(workTimeSeconds = 30, restTimeSeconds = 10, totalRounds = 3)
        
        // Create a status in PAUSED state manually
        val pausedStatus = TimerStatus(
            state = TimerState.PAUSED,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 15,
            timeRemainingMilliseconds = 500,
            currentRound = 1,
            config = config
        )
        
        // Verify the canResume property works correctly
        assertTrue("canResume should be true for PAUSED state", pausedStatus.canResume)
        assertFalse("canStart should be false for PAUSED state", pausedStatus.canStart)
        assertFalse("canPause should be false for PAUSED state", pausedStatus.canPause)
        assertTrue("canReset should be true for PAUSED state", pausedStatus.canReset)
    }
    
    @Test
    fun `test state transitions do not skip PAUSED`() = runTest {
        val config = TimerConfig(workTimeSeconds = 5, restTimeSeconds = 5, totalRounds = 1)
        
        timerManager.start(config)
        
        // Wait for BEGIN to complete and enter RUNNING
        delay(5100)
        assertEquals(TimerState.RUNNING, timerManager.timerStatus.value.state)
        
        // Pause immediately
        timerManager.pause()
        delay(50)
        
        // Verify we're in PAUSED state and it persists
        assertEquals("Should remain in PAUSED", TimerState.PAUSED, timerManager.timerStatus.value.state)
        
        // Wait a bit more to ensure state doesn't change on its own
        delay(1000)
        assertEquals("Should still be PAUSED", TimerState.PAUSED, timerManager.timerStatus.value.state)
        assertTrue("Should still be able to resume", timerManager.timerStatus.value.canResume)
        
        timerManager.cleanup()
    }
}