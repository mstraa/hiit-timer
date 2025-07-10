package com.hiittimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.ui.theme.HIITColors

/**
 * Refined visual feedback overlay (FR-024: Refined Visual Feedback)
 * This function is intentionally empty as continuous color changes were removed
 * Visual feedback is now handled by IntervalTransitionEffect only
 */
@Composable
fun VisualFeedbackOverlay(
    @Suppress("UNUSED_PARAMETER") timerStatus: TimerStatus,
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier
) {
    // FR-024: Intentionally empty - visual feedback moved to IntervalTransitionEffect
}

/**
 * Enhanced flash effects for interval transitions and BEGIN countdown (FR-024: Refined Visual Feedback)
 * Big flash at work interval start (green) and rest interval start (red)
 * Blue flash during BEGIN countdown
 * Clean, minimal visual feedback during countdown
 */
@Composable
fun IntervalTransitionEffect(
    timerStatus: TimerStatus,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Track interval changes for flash effects
    var previousInterval by remember { mutableStateOf(timerStatus.currentInterval) }
    var showIntervalFlash by remember { mutableStateOf(false) }

    // Track BEGIN countdown flash
    var showCountdownFlash by remember { mutableStateOf(false) }

    // Detect interval changes and trigger flash effect (FR-024)
    LaunchedEffect(timerStatus.currentInterval, timerStatus.state) {
        if (previousInterval != timerStatus.currentInterval && timerStatus.state == TimerState.RUNNING) {
            showIntervalFlash = true
            previousInterval = timerStatus.currentInterval
            // Big flash duration - longer and more prominent
            kotlinx.coroutines.delay(600) // Increased from 300ms for bigger flash effect
            showIntervalFlash = false
        }
    }

    // Handle BEGIN countdown blue flash
    LaunchedEffect(timerStatus.shouldFlashBlue) {
        if (timerStatus.shouldFlashBlue && timerStatus.state == TimerState.BEGIN) {
            showCountdownFlash = true
            kotlinx.coroutines.delay(400) // Shorter flash for countdown
            showCountdownFlash = false
        }
    }

    // Big flash animation with enhanced visibility
    val intervalFlashAlpha by animateFloatAsState(
        targetValue = if (showIntervalFlash) 0.8f else 0f, // Increased from 0.5f for bigger flash
        animationSpec = tween(
            durationMillis = 600, // Longer duration for more prominent flash
            easing = FastOutSlowInEasing
        ),
        label = "interval_flash_alpha"
    )

    // Countdown flash animation
    val countdownFlashAlpha by animateFloatAsState(
        targetValue = if (showCountdownFlash) 0.6f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "countdown_flash_alpha"
    )

    // Flash color based on state and interval type
    val flashColor = when {
        timerStatus.state == TimerState.BEGIN -> Color.Blue // Blue flash for countdown
        timerStatus.currentInterval == IntervalType.WORK -> if (isDarkTheme) HIITColors.WorkIndicatorDark else HIITColors.WorkIndicatorLight
        timerStatus.currentInterval == IntervalType.REST -> if (isDarkTheme) HIITColors.RestIndicatorDark else HIITColors.RestIndicatorLight
        else -> Color.Transparent
    }

    // Render flash effects
    val totalAlpha = maxOf(intervalFlashAlpha, countdownFlashAlpha)
    if (totalAlpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(flashColor.copy(alpha = totalAlpha))
                .pointerInput(Unit) {} // Prevent pointer events from affecting layout
        )
    }
}
