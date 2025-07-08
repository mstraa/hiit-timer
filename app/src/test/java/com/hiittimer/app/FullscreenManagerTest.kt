package com.hiittimer.app

import com.hiittimer.app.ui.fullscreen.FullscreenMode
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FullscreenManager functionality (FR-019)
 */
class FullscreenManagerTest {

    @Test
    fun `fullscreen mode enum has correct values`() {
        // Test that all required fullscreen modes are available
        val modes = FullscreenMode.values()
        
        assertTrue("STANDARD mode should be available", modes.contains(FullscreenMode.STANDARD))
        assertTrue("IMMERSIVE mode should be available", modes.contains(FullscreenMode.IMMERSIVE))
        assertTrue("IMMERSIVE_STICKY mode should be available", modes.contains(FullscreenMode.IMMERSIVE_STICKY))
        
        assertEquals("Should have exactly 3 fullscreen modes", 3, modes.size)
    }

    @Test
    fun `fullscreen mode enum values are correctly named`() {
        // Test enum naming for clarity and consistency
        assertEquals("STANDARD", FullscreenMode.STANDARD.name)
        assertEquals("IMMERSIVE", FullscreenMode.IMMERSIVE.name)
        assertEquals("IMMERSIVE_STICKY", FullscreenMode.IMMERSIVE_STICKY.name)
    }

    @Test
    fun `fullscreen mode enum ordinal values are consistent`() {
        // Test ordinal values for potential switch statements
        assertEquals(0, FullscreenMode.STANDARD.ordinal)
        assertEquals(1, FullscreenMode.IMMERSIVE.ordinal)
        assertEquals(2, FullscreenMode.IMMERSIVE_STICKY.ordinal)
    }
}
