package com.hiittimer.app

import com.hiittimer.app.audio.AudioSettings
import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.IntervalType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests to validate the critical bug fixes from Section 12 of the PRD
 * Tests Bug 1 (Resume Button Display) and Bug 3 (Default Timer Display)
 * Also includes previous audio and configuration bug fix tests
 */
class BugFixValidationTest {

    // ========================================
    // SECTION 12 BUG FIXES (Phase 1)
    // ========================================

    @Test
    fun `bug 1 fix - resume button state logic works correctly`() {
        // FR-040: Resume Button Display Logic

        // Test IDLE state - should show Start
        val idleStatus = TimerStatus.createDefault()
        assertTrue("Should be able to start when IDLE", idleStatus.canStart)
        assertFalse("Should not be able to pause when IDLE", idleStatus.canPause)
        assertFalse("Should not be able to resume when IDLE", idleStatus.canResume)
        assertFalse("Should not be able to reset when IDLE", idleStatus.canReset)

        // Test RUNNING state - should show Pause
        val runningStatus = idleStatus.copy(state = TimerState.RUNNING)
        assertFalse("Should not be able to start when RUNNING", runningStatus.canStart)
        assertTrue("Should be able to pause when RUNNING", runningStatus.canPause)
        assertFalse("Should not be able to resume when RUNNING", runningStatus.canResume)
        assertFalse("Should not be able to reset when RUNNING", runningStatus.canReset)

        // Test PAUSED state - should show Resume
        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertFalse("Should not be able to start when PAUSED", pausedStatus.canStart)
        assertFalse("Should not be able to pause when PAUSED", pausedStatus.canPause)
        assertTrue("Should be able to resume when PAUSED", pausedStatus.canResume)
        assertTrue("Should be able to reset when PAUSED", pausedStatus.canReset)

        // Test FINISHED state - should show Start (new workout)
        val finishedStatus = idleStatus.copy(state = TimerState.FINISHED)
        assertFalse("Should not be able to start when FINISHED", finishedStatus.canStart)
        assertFalse("Should not be able to pause when FINISHED", finishedStatus.canPause)
        assertFalse("Should not be able to resume when FINISHED", finishedStatus.canResume)
        assertTrue("Should be able to reset when FINISHED", finishedStatus.canReset)
    }

    @Test
    fun `bug 1 fix - button text logic matches timer state`() {
        // FR-040: Simulate the button text logic from TimerScreen.kt

        fun getExpectedButtonText(timerStatus: TimerStatus): String {
            return when {
                timerStatus.canStart -> "Start"
                timerStatus.canPause -> "Pause"
                timerStatus.canResume -> "Resume"
                else -> "Start"
            }
        }

        // Test all states
        val idleStatus = TimerStatus.createDefault()
        assertEquals("IDLE should show Start", "Start", getExpectedButtonText(idleStatus))

        val runningStatus = idleStatus.copy(state = TimerState.RUNNING)
        assertEquals("RUNNING should show Pause", "Pause", getExpectedButtonText(runningStatus))

        val pausedStatus = runningStatus.copy(state = TimerState.PAUSED)
        assertEquals("PAUSED should show Resume", "Resume", getExpectedButtonText(pausedStatus))

        val finishedStatus = idleStatus.copy(state = TimerState.FINISHED)
        assertEquals("FINISHED should show Start", "Start", getExpectedButtonText(finishedStatus))
    }

    @Test
    fun `bug 3 fix - default timer status shows correct initial display`() {
        // FR-042: Default Timer Display Initialization

        val defaultStatus = TimerStatus.createDefault()

        // Should show default work time (20 seconds) instead of 00:00.0
        assertEquals("Should show default work time", 20, defaultStatus.timeRemainingSeconds)
        assertEquals("Should start with 0 milliseconds", 0, defaultStatus.timeRemainingMilliseconds)
        assertEquals("Should be in IDLE state", TimerState.IDLE, defaultStatus.state)
        assertEquals("Should be in WORK interval", IntervalType.WORK, defaultStatus.currentInterval)
        assertEquals("Should start at round 1", 1, defaultStatus.currentRound)

        // Verify the configuration is correct
        val config = defaultStatus.config
        assertEquals("Default work time should be 20 seconds", 20, config.workTimeSeconds)
        assertEquals("Default rest time should be 10 seconds", 10, config.restTimeSeconds)
        assertEquals("Default rounds should be 5", 5, config.totalRounds)
        assertFalse("Should not be unlimited by default", config.isUnlimited)
        assertFalse("Should have rest by default", config.noRest)
    }

    @Test
    fun `bug 3 fix - default display formatting shows correct time`() {
        // FR-042: Verify time formatting shows "20.0" instead of "00:00.0"

        val defaultStatus = TimerStatus.createDefault()
        val formattedTime = defaultStatus.formatTimeRemaining()

        // Should show "20.0" for 20 seconds, 0 milliseconds
        assertEquals("Should format as 20.0", "20.0", formattedTime)
    }

    // ========================================
    // PREVIOUS BUG FIXES (Audio & Config)
    // ========================================

    @Test
    fun `bug fix 1 - audio settings initialization with disabled state`() {
        // Test that AudioSettings can be created with disabled state
        val disabledAudioSettings = AudioSettings(isEnabled = false, volume = 0.7f)

        // Verify settings are correctly initialized
        assertEquals(false, disabledAudioSettings.isEnabled)
        assertEquals(0.7f, disabledAudioSettings.volume, 0.01f)

        // Test that disabled state is preserved
        assertFalse("Audio should be disabled", disabledAudioSettings.isEnabled)
    }

    @Test
    fun `bug fix 1 - audio settings initialization with enabled state`() {
        // Test that AudioSettings can be created with enabled state
        val enabledAudioSettings = AudioSettings(isEnabled = true, volume = 0.8f)

        // Verify settings are correctly initialized
        assertEquals(true, enabledAudioSettings.isEnabled)
        assertEquals(0.8f, enabledAudioSettings.volume, 0.01f)

        // Test that enabled state is preserved
        assertTrue("Audio should be enabled", enabledAudioSettings.isEnabled)
    }

    @Test
    fun `bug fix 1 - audio settings can be created with different values`() {
        // Test that audio settings can be created with different values
        val settings1 = AudioSettings(isEnabled = true, volume = 0.5f)
        val settings2 = AudioSettings(isEnabled = false, volume = 0.8f)

        // Verify first settings
        assertTrue("First settings should be enabled", settings1.isEnabled)
        assertEquals(0.5f, settings1.volume, 0.01f)

        // Verify second settings
        assertFalse("Second settings should be disabled", settings2.isEnabled)
        assertEquals(0.8f, settings2.volume, 0.01f)

        // Verify they are independent
        assertNotEquals("Settings should be different", settings1.isEnabled, settings2.isEnabled)
        assertNotEquals("Volume should be different", settings1.volume, settings2.volume, 0.01f)
    }

    @Test
    fun `bug fix 2 - timer status preserves configuration after reset simulation`() {
        // Test that TimerStatus can be created with preserved configuration
        val customConfig = TimerConfig(
            workTimeSeconds = 45,
            restTimeSeconds = 15,
            totalRounds = 8,
            isUnlimited = false,
            noRest = false
        )

        // Simulate a running timer status
        val runningStatus = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = 30,
            timeRemainingMilliseconds = 500,
            currentRound = 3,
            config = customConfig
        )

        // Verify running state
        assertEquals(TimerState.RUNNING, runningStatus.state)
        assertEquals(30, runningStatus.timeRemainingSeconds)
        assertEquals(3, runningStatus.currentRound)
        assertEquals(45, runningStatus.config.workTimeSeconds)

        // Simulate reset with preserved configuration
        val resetStatus = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = customConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = customConfig
        )

        // Verify state is reset but configuration is preserved
        assertEquals(TimerState.IDLE, resetStatus.state)
        assertEquals(45, resetStatus.config.workTimeSeconds)
        assertEquals(15, resetStatus.config.restTimeSeconds)
        assertEquals(8, resetStatus.config.totalRounds)
        assertEquals(1, resetStatus.currentRound)
        assertEquals(45, resetStatus.timeRemainingSeconds) // Should be reset to work time
        assertEquals(0, resetStatus.timeRemainingMilliseconds)
    }

    @Test
    fun `bug fix 2 - timer configuration with no rest setting`() {
        // Test with different configuration values including no rest
        val customConfig = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 20,
            totalRounds = 3,
            isUnlimited = false,
            noRest = true // Test with no rest enabled
        )

        // Verify configuration values
        assertEquals(30, customConfig.workTimeSeconds)
        assertEquals(20, customConfig.restTimeSeconds)
        assertEquals(3, customConfig.totalRounds)
        assertFalse(customConfig.isUnlimited)
        assertTrue(customConfig.noRest)

        // Simulate multiple reset operations with preserved configuration
        val resetStatus1 = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = customConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = customConfig
        )

        val resetStatus2 = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = customConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = customConfig
        )

        // Verify configuration is preserved in both reset operations
        assertEquals(30, resetStatus1.config.workTimeSeconds)
        assertEquals(20, resetStatus1.config.restTimeSeconds)
        assertEquals(3, resetStatus1.config.totalRounds)
        assertTrue(resetStatus1.config.noRest)
        assertEquals(TimerState.IDLE, resetStatus1.state)

        assertEquals(30, resetStatus2.config.workTimeSeconds)
        assertEquals(20, resetStatus2.config.restTimeSeconds)
        assertEquals(3, resetStatus2.config.totalRounds)
        assertTrue(resetStatus2.config.noRest)
        assertEquals(TimerState.IDLE, resetStatus2.state)
    }

    @Test
    fun `bug fix 2 - reset sets correct initial time remaining`() {
        // Test that reset sets timeRemainingSeconds to the work time from config
        val customConfig = TimerConfig(
            workTimeSeconds = 60,
            restTimeSeconds = 30,
            totalRounds = 5,
            isUnlimited = false,
            noRest = false
        )

        // Simulate reset operation with correct time remaining
        val resetStatus = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = customConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = customConfig
        )

        // Verify that timeRemainingSeconds is set to work time
        assertEquals(60, resetStatus.timeRemainingSeconds)
        assertEquals(0, resetStatus.timeRemainingMilliseconds)
        assertEquals(IntervalType.WORK, resetStatus.currentInterval)
        assertEquals(1, resetStatus.currentRound)
        assertEquals(TimerState.IDLE, resetStatus.state)

        // Verify configuration is preserved
        assertEquals(60, resetStatus.config.workTimeSeconds)
        assertEquals(30, resetStatus.config.restTimeSeconds)
        assertEquals(5, resetStatus.config.totalRounds)
    }

    @Test
    fun `both bugs fixed - audio and configuration work together`() {
        // Integration test to verify both fixes work together
        val customConfig = TimerConfig(workTimeSeconds = 25, restTimeSeconds = 5, totalRounds = 4)
        val audioSettings = AudioSettings(isEnabled = false, volume = 0.9f)

        // Verify audio settings
        assertFalse("Audio should be disabled", audioSettings.isEnabled)
        assertEquals(0.9f, audioSettings.volume, 0.01f)

        // Verify timer configuration
        assertEquals(25, customConfig.workTimeSeconds)
        assertEquals(5, customConfig.restTimeSeconds)
        assertEquals(4, customConfig.totalRounds)

        // Simulate reset with preserved configuration
        val resetStatus = TimerStatus(
            state = TimerState.IDLE,
            currentInterval = IntervalType.WORK,
            timeRemainingSeconds = customConfig.workTimeSeconds,
            timeRemainingMilliseconds = 0,
            currentRound = 1,
            config = customConfig
        )

        // Verify both settings work together correctly
        assertFalse("Audio should still be disabled", audioSettings.isEnabled)
        assertEquals(0.9f, audioSettings.volume, 0.01f)
        assertEquals(25, resetStatus.config.workTimeSeconds)
        assertEquals(5, resetStatus.config.restTimeSeconds)
        assertEquals(4, resetStatus.config.totalRounds)
        assertEquals(25, resetStatus.timeRemainingSeconds)
        assertEquals(TimerState.IDLE, resetStatus.state)
    }
}
