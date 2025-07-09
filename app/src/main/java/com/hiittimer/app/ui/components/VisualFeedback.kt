package com.hiittimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.ui.theme.HIITColors

/**
 * Refined visual feedback overlay (FR-024: Refined Visual Feedback)
 * Removed continuous color changes, keeping only flash effects at interval transitions
 * No background color changes during active timing for clean, minimal visual feedback
 */
@Composable
fun VisualFeedbackOverlay(
    timerStatus: TimerStatus,
    modifier: Modifier = Modifier
) {
    // FR-024: Remove continuous light color changes during timer operation
    // This component now only handles flash effects at interval transitions
    // No continuous background color changes or pulsing animations
}

/**
 * Enhanced flash effects for interval transitions (FR-024: Refined Visual Feedback)
 * Big flash at work interval start (green) and rest interval start (red)
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
    var showFlash by remember { mutableStateOf(false) }

    // Detect interval changes and trigger flash effect (FR-024)
    LaunchedEffect(timerStatus.currentInterval, timerStatus.state) {
        if (previousInterval != timerStatus.currentInterval && timerStatus.state == TimerState.RUNNING) {
            showFlash = true
            previousInterval = timerStatus.currentInterval
            // Big flash duration - longer and more prominent
            kotlinx.coroutines.delay(600) // Increased from 300ms for bigger flash effect
            showFlash = false
        }
    }

    // Big flash animation with enhanced visibility
    val flashAlpha by animateFloatAsState(
        targetValue = if (showFlash) 0.8f else 0f, // Increased from 0.5f for bigger flash
        animationSpec = tween(
            durationMillis = 600, // Longer duration for more prominent flash
            easing = FastOutSlowInEasing
        ),
        label = "flash_alpha"
    )

    // Flash color based on interval type (FR-024: Green for work, Red for rest)
    val flashColor = when (timerStatus.currentInterval) {
        IntervalType.WORK -> if (isDarkTheme) HIITColors.WorkIndicatorDark else HIITColors.WorkIndicatorLight
        IntervalType.REST -> if (isDarkTheme) HIITColors.RestIndicatorDark else HIITColors.RestIndicatorLight
    }

    // Render big flash effect only at interval transitions
    if (flashAlpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(flashColor.copy(alpha = flashAlpha))
        )
    }
}
