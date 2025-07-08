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
 * Full-screen visual feedback overlay that provides animated color feedback
 * during work and rest intervals as specified in FR-004
 */
@Composable
fun VisualFeedbackOverlay(
    timerStatus: TimerStatus,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // Determine overlay color based on interval type and theme
    val overlayColor = when {
        timerStatus.currentInterval == IntervalType.WORK -> {
            if (isDarkTheme) HIITColors.WorkIndicatorDark else HIITColors.WorkIndicatorLight
        }
        timerStatus.currentInterval == IntervalType.REST -> {
            if (isDarkTheme) HIITColors.RestIndicatorDark else HIITColors.RestIndicatorLight
        }
        else -> Color.Transparent
    }
    
    // Animation for smooth transitions (300ms as per PRD)
    val animatedAlpha by animateFloatAsState(
        targetValue = if (timerStatus.state == TimerState.RUNNING) 0.25f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "overlay_alpha"
    )
    
    // Pulsing animation during active intervals
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    // Use pulsing effect only when timer is running
    val finalAlpha = if (timerStatus.state == TimerState.RUNNING) {
        pulseAlpha * animatedAlpha
    } else {
        animatedAlpha
    }
    
    // Render the overlay
    if (finalAlpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(overlayColor.copy(alpha = finalAlpha))
        )
    }
}

/**
 * Enhanced visual feedback that includes transition effects
 * for interval changes as specified in FR-004
 */
@Composable
fun IntervalTransitionEffect(
    timerStatus: TimerStatus,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // Track interval changes for transition effects
    var previousInterval by remember { mutableStateOf(timerStatus.currentInterval) }
    var showTransition by remember { mutableStateOf(false) }
    
    // Detect interval changes
    LaunchedEffect(timerStatus.currentInterval) {
        if (previousInterval != timerStatus.currentInterval && timerStatus.state == TimerState.RUNNING) {
            showTransition = true
            previousInterval = timerStatus.currentInterval
            // Hide transition after animation completes
            kotlinx.coroutines.delay(300)
            showTransition = false
        }
    }
    
    // Transition animation
    val transitionAlpha by animateFloatAsState(
        targetValue = if (showTransition) 0.5f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "transition_alpha"
    )
    
    // Color for transition effect
    val transitionColor = when (timerStatus.currentInterval) {
        IntervalType.WORK -> if (isDarkTheme) HIITColors.WorkIndicatorDark else HIITColors.WorkIndicatorLight
        IntervalType.REST -> if (isDarkTheme) HIITColors.RestIndicatorDark else HIITColors.RestIndicatorLight
    }
    
    // Render transition effect
    if (transitionAlpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(transitionColor.copy(alpha = transitionAlpha))
        )
    }
}
