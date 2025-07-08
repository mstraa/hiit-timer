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
 * Fullscreen mode states for different app contexts (FR-019)
 */
enum class FullscreenMode {
    STANDARD,           // Normal UI with system bars visible
    IMMERSIVE,          // Full immersive mode with hidden system UI (active timer)
    IMMERSIVE_STICKY    // Immersive with swipe-to-reveal capability (paused timer)
}

/**
 * Manager for controlling system UI visibility and fullscreen modes (FR-019)
 * Handles Android version compatibility and gesture navigation support
 */
class FullscreenManager(private val activity: Activity) {
    
    private var currentMode = FullscreenMode.STANDARD
    
    /**
     * Set fullscreen mode based on app state
     */
    fun setFullscreenMode(mode: FullscreenMode) {
        if (currentMode == mode) return
        
        currentMode = mode
        
        when (mode) {
            FullscreenMode.STANDARD -> showSystemUI()
            FullscreenMode.IMMERSIVE -> hideSystemUIImmersive()
            FullscreenMode.IMMERSIVE_STICKY -> hideSystemUIImmersiveSticky()
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
     * Hide system UI in immersive mode (active timer)
     */
    private fun hideSystemUIImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Use WindowInsetsController
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 and below - Use deprecated flags
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }
    
    /**
     * Hide system UI in immersive sticky mode (paused timer with swipe-to-reveal)
     */
    private fun hideSystemUIImmersiveSticky() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Use WindowInsetsController
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 and below - Use deprecated flags
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }
    
    /**
     * Get current fullscreen mode
     */
    fun getCurrentMode(): FullscreenMode = currentMode
}

/**
 * Composable function to manage fullscreen mode based on timer state (FR-019)
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
                
                // Active timer - full immersive mode
                timerState == com.hiittimer.app.data.TimerState.RUNNING -> FullscreenMode.IMMERSIVE
                
                // Paused timer - immersive sticky (swipe-to-reveal)
                timerState == com.hiittimer.app.data.TimerState.PAUSED -> FullscreenMode.IMMERSIVE_STICKY
                
                // Idle or finished - standard mode
                else -> FullscreenMode.STANDARD
            }
            
            manager.setFullscreenMode(mode)
        }
    }
}
