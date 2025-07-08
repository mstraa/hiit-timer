package com.hiittimer.app.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive design utilities for FR-015: Responsive Design
 * Provides adaptive layouts for different screen sizes and orientations
 */

/**
 * Screen size categories based on width
 */
enum class ScreenSize {
    COMPACT,    // < 600dp (phones in portrait)
    MEDIUM,     // 600dp - 840dp (tablets in portrait, phones in landscape)
    EXPANDED    // > 840dp (tablets in landscape, large screens)
}

/**
 * Get current screen size category
 */
@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    
    return when {
        screenWidthDp < 600.dp -> ScreenSize.COMPACT
        screenWidthDp < 840.dp -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

/**
 * Check if device is in landscape orientation
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

/**
 * Get adaptive padding based on screen size
 */
@Composable
fun getAdaptivePadding(): Dp {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> 16.dp
        ScreenSize.MEDIUM -> 24.dp
        ScreenSize.EXPANDED -> 32.dp
    }
}

/**
 * Get adaptive timer font size based on screen size
 */
@Composable
fun getAdaptiveTimerFontSize(): androidx.compose.ui.unit.TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    
    return when {
        screenWidthDp < 360 -> 60.sp // Small phones
        screenWidthDp < 480 -> 72.sp // Normal phones
        screenWidthDp < 600 -> 84.sp // Large phones
        else -> 96.sp // Tablets
    }
}

/**
 * Get minimum touch target size (FR-015: Minimum 48dp touch targets)
 */
val MinTouchTargetSize = 48.dp

/**
 * Get adaptive button height ensuring minimum touch target
 */
@Composable
fun getAdaptiveButtonHeight(): Dp {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> MinTouchTargetSize
        ScreenSize.MEDIUM -> 56.dp
        ScreenSize.EXPANDED -> 64.dp
    }
}

/**
 * Get adaptive spacing based on screen size
 */
@Composable
fun getAdaptiveSpacing(): Dp {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> 8.dp
        ScreenSize.MEDIUM -> 12.dp
        ScreenSize.EXPANDED -> 16.dp
    }
}

/**
 * Get system bar insets for edge-to-edge support
 */
@Composable
fun getSystemBarInsets(): androidx.compose.foundation.layout.PaddingValues {
    val density = LocalDensity.current
    val insets = WindowInsets.systemBars
    
    return androidx.compose.foundation.layout.PaddingValues(
        top = with(density) { insets.getTop(density).toDp() },
        bottom = with(density) { insets.getBottom(density).toDp() },
        start = with(density) { insets.getLeft(density, androidx.compose.ui.unit.LayoutDirection.Ltr).toDp() },
        end = with(density) { insets.getRight(density, androidx.compose.ui.unit.LayoutDirection.Ltr).toDp() }
    )
}

/**
 * Calculate adaptive layout weights for landscape mode
 */
@Composable
fun getLandscapeLayoutWeights(): Pair<Float, Float> {
    return if (isLandscape()) {
        Pair(0.6f, 0.4f) // Timer area gets 60%, controls get 40%
    } else {
        Pair(1f, 0f) // Portrait mode uses normal column layout
    }
}
