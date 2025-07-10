package com.hiittimer.app.ui.fullscreen

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Fullscreen mode states for different app contexts (FR-019, FR-023)
 * Updated to maintain status bar visibility per FR-023 requirements
 */
enum class FullscreenMode {
    STANDARD,           // Normal UI with system bars visible
    ENHANCED_FULLSCREEN // Enhanced fullscreen with status bar visible and unified background (FR-023)
}

/**
 * Manager for controlling system UI visibility and fullscreen modes (FR-019, FR-023)
 * Enhanced to support unified background with persistent status bar (FR-023)
 * Handles Android version compatibility and gesture navigation support
 */
class FullscreenManager(private val activity: Activity) {

    private var currentMode = FullscreenMode.STANDARD

    /**
     * Set fullscreen mode based on app state (FR-023: Enhanced fullscreen experience)
     */
    fun setFullscreenMode(mode: FullscreenMode) {
        if (currentMode == mode) return

        currentMode = mode

        when (mode) {
            FullscreenMode.STANDARD -> showSystemUI()
            FullscreenMode.ENHANCED_FULLSCREEN -> setEnhancedFullscreen()
        }
    }
    
    /**
     * Show system UI (standard mode for settings/configuration)
     */
    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Use WindowInsetsController
            activity.window.insetsController?.let { controller ->
                controller.show(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 and below - Use deprecated flags
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }

    /**
     * Set enhanced fullscreen mode (FR-023: Enhanced fullscreen experience)
     * - Status bar remains visible at all times
     * - Navigation bar remains visible to prevent UI shifts
     * - Unified background color between status bar area and content
     * - Edge-to-edge content with proper insets handling
     * - No color transitions during fullscreen mode
     */
    private fun setEnhancedFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Keep both status bar and navigation bar visible
            activity.window.insetsController?.let { controller ->
                controller.show(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 and below - Keep both status bar and navigation bar visible
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Removed SYSTEM_UI_FLAG_HIDE_NAVIGATION and SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // to keep navigation bar visible and prevent UI shifts
            )
        }

        // Enable edge-to-edge display with proper insets handling
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }
    

    
    /**
     * Get current fullscreen mode
     */
    fun getCurrentMode(): FullscreenMode = currentMode
}

/**
 * Composable function to manage fullscreen mode based on timer state (FR-019, FR-023)
 * Updated for enhanced fullscreen experience with persistent status bar
 */
@Composable
fun FullscreenController(
    timerState: com.hiittimer.app.data.TimerState,
    isSettingsOpen: Boolean = false,
    isConfigOpen: Boolean = false
) {
    val view = LocalView.current
    val activity = view.context as? Activity

    val fullscreenManager = remember(activity) {
        activity?.let { FullscreenManager(it) }
    }

    LaunchedEffect(timerState, isSettingsOpen, isConfigOpen) {
        fullscreenManager?.let { manager ->
            val mode = when {
                // Settings or configuration open - show system UI
                isSettingsOpen || isConfigOpen -> FullscreenMode.STANDARD

                // Active timer or paused timer - enhanced fullscreen with status bar visible (FR-023)
                timerState == com.hiittimer.app.data.TimerState.RUNNING ||
                timerState == com.hiittimer.app.data.TimerState.PAUSED -> FullscreenMode.ENHANCED_FULLSCREEN

                // Idle or finished - standard mode
                else -> FullscreenMode.STANDARD
            }

            manager.setFullscreenMode(mode)
        }
    }
}
