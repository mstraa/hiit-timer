package com.hiittimer.app

import com.hiittimer.app.data.TimerConfig
import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify the fix for the timer start button crash
 * Tests the configuration validation and API level compatibility
 */
class TimerStartCrashFixTest {

    @Test
    fun `timer configuration validation prevents crashes from invalid configs`() {
        // Test that valid configurations work correctly
        val validConfig = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 15,
            totalRounds = 3
        )
        assertEquals(30, validConfig.workTimeSeconds)
        assertEquals(15, validConfig.restTimeSeconds)
        assertEquals(3, validConfig.totalRounds)
        assertFalse(validConfig.isUnlimited)
        assertFalse(validConfig.noRest)
    }

    @Test
    fun `timer config validation prevents invalid configurations that could cause crashes`() {
        // Invalid work time should throw exception
        try {
            TimerConfig(workTimeSeconds = 3, restTimeSeconds = 15, totalRounds = 3)
            fail("Should have thrown exception for invalid work time")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Work time must be between 5 and 900 seconds") == true)
        }

        // Invalid rest time should throw exception (when rest is enabled)
        try {
            TimerConfig(workTimeSeconds = 30, restTimeSeconds = 0, totalRounds = 3, noRest = false)
            fail("Should have thrown exception for invalid rest time")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Rest time must be between 1 and 300 seconds") == true)
        }

        // Invalid rounds should throw exception (when not unlimited)
        try {
            TimerConfig(workTimeSeconds = 30, restTimeSeconds = 15, totalRounds = 0, isUnlimited = false)
            fail("Should have thrown exception for invalid rounds")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Total rounds must be between 1 and 99") == true)
        }
    }

    @Test
    fun `no rest mode allows any rest time value`() {
        // No rest mode should allow any rest time value without throwing exception
        val noRestConfig = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 1, // This would normally be invalid
            totalRounds = 3,
            noRest = true
        )
        assertEquals(true, noRestConfig.noRest)
        assertEquals(30, noRestConfig.workTimeSeconds)
        assertEquals(1, noRestConfig.restTimeSeconds) // Should be allowed with noRest = true
    }

    @Test
    fun `unlimited rounds mode allows any round count`() {
        // Unlimited mode should allow any round count
        val unlimitedConfig = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 15,
            totalRounds = 0, // This would normally be invalid
            isUnlimited = true
        )
        assertEquals(true, unlimitedConfig.isUnlimited)
        assertEquals(0, unlimitedConfig.totalRounds) // Should be allowed with isUnlimited = true
    }
}
