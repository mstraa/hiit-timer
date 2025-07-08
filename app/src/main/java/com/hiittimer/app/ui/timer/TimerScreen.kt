package com.hiittimer.app.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hiittimer.app.R
import com.hiittimer.app.audio.AudioSettings
import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.ui.components.AudioToggleButton
import com.hiittimer.app.ui.components.IntervalTransitionEffect
import com.hiittimer.app.ui.components.VisualFeedbackOverlay
import com.hiittimer.app.ui.theme.HIITColors
import com.hiittimer.app.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel(),
    onNavigateToConfig: () -> Unit = {}
) {
    val timerStatus by viewModel.timerStatus.collectAsState()
    val audioSettings by viewModel.audioSettings.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    // Responsive design values (FR-015: Responsive Design)
    val screenSize = getScreenSize()
    val isLandscapeMode = isLandscape()
    val adaptivePadding = getAdaptivePadding()
    val adaptiveSpacing = getAdaptiveSpacing()
    val adaptiveButtonHeight = getAdaptiveButtonHeight()
    val adaptiveTimerFontSize = getAdaptiveTimerFontSize()

    // Color scheme based on interval type and theme
    val intervalColor = when {
        timerStatus.isWorkInterval -> if (isDarkTheme) HIITColors.WorkIndicatorDark else HIITColors.WorkIndicatorLight
        else -> if (isDarkTheme) HIITColors.RestIndicatorDark else HIITColors.RestIndicatorLight
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Adaptive layout based on orientation (FR-015: Landscape support)
        if (isLandscapeMode) {
            LandscapeTimerLayout(
                timerStatus = timerStatus,
                audioSettings = audioSettings,
                intervalColor = intervalColor,
                adaptivePadding = adaptivePadding,
                adaptiveSpacing = adaptiveSpacing,
                adaptiveButtonHeight = adaptiveButtonHeight,
                adaptiveTimerFontSize = adaptiveTimerFontSize,
                viewModel = viewModel,
                onNavigateToConfig = onNavigateToConfig
            )
        } else {
            PortraitTimerLayout(
                timerStatus = timerStatus,
                audioSettings = audioSettings,
                intervalColor = intervalColor,
                adaptivePadding = adaptivePadding,
                adaptiveSpacing = adaptiveSpacing,
                adaptiveButtonHeight = adaptiveButtonHeight,
                adaptiveTimerFontSize = adaptiveTimerFontSize,
                viewModel = viewModel,
                onNavigateToConfig = onNavigateToConfig
            )
        }

        
        // Visual feedback overlays (FR-004: Full-screen visual indicators)
        VisualFeedbackOverlay(
            timerStatus = timerStatus,
            modifier = Modifier.fillMaxSize()
        )

        // Interval transition effects
        IntervalTransitionEffect(
            timerStatus = timerStatus,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Portrait layout for timer screen (FR-015: Portrait orientation support)
 */
@Composable
private fun PortraitTimerLayout(
    timerStatus: TimerStatus,
    audioSettings: AudioSettings,
    intervalColor: Color,
    adaptivePadding: Dp,
    adaptiveSpacing: Dp,
    adaptiveButtonHeight: Dp,
    adaptiveTimerFontSize: androidx.compose.ui.unit.TextUnit,
    viewModel: TimerViewModel,
    onNavigateToConfig: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = adaptivePadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with settings and audio controls
        TimerHeader(
            audioSettings = audioSettings,
            adaptiveSpacing = adaptiveSpacing,
            viewModel = viewModel,
            onNavigateToConfig = onNavigateToConfig
        )

        Spacer(modifier = Modifier.height(adaptiveSpacing * 2))

        // Main timer display area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TimerDisplay(
                timerStatus = timerStatus,
                intervalColor = intervalColor,
                adaptiveSpacing = adaptiveSpacing,
                adaptiveTimerFontSize = adaptiveTimerFontSize
            )
        }

        // Control buttons
        TimerControls(
            timerStatus = timerStatus,
            adaptiveSpacing = adaptiveSpacing,
            adaptiveButtonHeight = adaptiveButtonHeight,
            viewModel = viewModel
        )
    }
}

/**
 * Landscape layout for timer screen (FR-015: Landscape orientation support)
 */
@Composable
private fun LandscapeTimerLayout(
    timerStatus: TimerStatus,
    audioSettings: AudioSettings,
    intervalColor: Color,
    adaptivePadding: Dp,
    adaptiveSpacing: Dp,
    adaptiveButtonHeight: Dp,
    adaptiveTimerFontSize: androidx.compose.ui.unit.TextUnit,
    viewModel: TimerViewModel,
    onNavigateToConfig: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = adaptivePadding)
    ) {
        // Header (compact in landscape)
        TimerHeader(
            audioSettings = audioSettings,
            adaptiveSpacing = adaptiveSpacing,
            viewModel = viewModel,
            onNavigateToConfig = onNavigateToConfig,
            isCompact = true
        )

        Spacer(modifier = Modifier.height(height = adaptiveSpacing))

        // Main content in row layout for landscape
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(adaptiveSpacing * 2)
        ) {
            // Timer display (60% width)
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                TimerDisplay(
                    timerStatus = timerStatus,
                    intervalColor = intervalColor,
                    adaptiveSpacing = adaptiveSpacing,
                    adaptiveTimerFontSize = adaptiveTimerFontSize
                )
            }

            // Controls (40% width)
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                TimerControls(
                    timerStatus = timerStatus,
                    adaptiveSpacing = adaptiveSpacing,
                    adaptiveButtonHeight = adaptiveButtonHeight,
                    viewModel = viewModel,
                    isVertical = true
                )
            }
        }
    }
}

/**
 * Timer header component with adaptive layout
 */
@Composable
private fun TimerHeader(
    audioSettings: AudioSettings,
    adaptiveSpacing: Dp,
    viewModel: TimerViewModel,
    onNavigateToConfig: () -> Unit,
    isCompact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) adaptiveSpacing / 2 else adaptiveSpacing),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(adaptiveSpacing)
        ) {
            TextButton(
                onClick = onNavigateToConfig,
                modifier = Modifier.heightIn(min = MinTouchTargetSize)
            ) {
                Text("Settings")
            }

            // Audio toggle button (FR-007: Audio Controls)
            AudioToggleButton(
                audioSettings = audioSettings,
                onToggleAudio = { viewModel.toggleAudio() },
                modifier = Modifier.size(MinTouchTargetSize)
            )
        }

        if (!isCompact) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(
            onClick = { /* TODO: Presets */ },
            modifier = Modifier.heightIn(min = MinTouchTargetSize)
        ) {
            Text("Presets")
        }
    }
}

/**
 * Timer display component with adaptive sizing
 */
@Composable
private fun TimerDisplay(
    timerStatus: TimerStatus,
    intervalColor: Color,
    adaptiveSpacing: Dp,
    adaptiveTimerFontSize: androidx.compose.ui.unit.TextUnit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Interval type indicator
        Card(
            modifier = Modifier.padding(bottom = adaptiveSpacing * 2),
            colors = CardDefaults.cardColors(containerColor = intervalColor.copy(alpha = 0.1f))
        ) {
            Text(
                text = if (timerStatus.isWorkInterval) {
                    stringResource(R.string.work)
                } else {
                    stringResource(R.string.rest)
                },
                modifier = Modifier.padding(horizontal = adaptiveSpacing * 2, vertical = adaptiveSpacing),
                style = MaterialTheme.typography.headlineMedium,
                color = intervalColor,
                fontWeight = FontWeight.Bold
            )
        }

        // Large timer display with adaptive font size
        Text(
            text = timerStatus.formatTimeRemaining(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = adaptiveTimerFontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(adaptiveSpacing * 2))

        // Round progress
        Text(
            text = timerStatus.getRoundProgressText(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        // Next interval preview (FR-005: 5 seconds before transition)
        timerStatus.getNextIntervalPreview()?.let { preview: String ->
            Spacer(modifier = Modifier.height(height = adaptiveSpacing))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = preview,
                    modifier = Modifier.padding(horizontal = adaptiveSpacing * 2, vertical = adaptiveSpacing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Timer controls component with adaptive layout
 */
@Composable
private fun TimerControls(
    timerStatus: TimerStatus,
    adaptiveSpacing: Dp,
    adaptiveButtonHeight: Dp,
    viewModel: TimerViewModel,
    isVertical: Boolean = false
) {
    if (isVertical) {
        // Vertical layout for landscape mode
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(adaptiveSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerButton(
                timerStatus = timerStatus,
                adaptiveButtonHeight = adaptiveButtonHeight,
                viewModel = viewModel,
                isPrimary = true
            )

            TimerButton(
                timerStatus = timerStatus,
                adaptiveButtonHeight = adaptiveButtonHeight,
                viewModel = viewModel,
                isPrimary = false
            )
        }
    } else {
        // Horizontal layout for portrait mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = adaptiveSpacing),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimerButton(
                timerStatus = timerStatus,
                adaptiveButtonHeight = adaptiveButtonHeight,
                viewModel = viewModel,
                isPrimary = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(width = adaptiveSpacing))

            TimerButton(
                timerStatus = timerStatus,
                adaptiveButtonHeight = adaptiveButtonHeight,
                viewModel = viewModel,
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual timer button component
 */
@Composable
private fun TimerButton(
    timerStatus: TimerStatus,
    adaptiveButtonHeight: Dp,
    viewModel: TimerViewModel,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPrimary) {
        // Start/Pause/Resume button
        Button(
            onClick = {
                when {
                    timerStatus.canStart -> viewModel.startTimer()
                    timerStatus.canPause -> viewModel.pauseTimer()
                    timerStatus.canResume -> viewModel.resumeTimer()
                }
            },
            enabled = timerStatus.canStart || timerStatus.canPause || timerStatus.canResume,
            modifier = modifier
                .heightIn(min = adaptiveButtonHeight)
                .widthIn(min = MinTouchTargetSize * 2),
            shape = RoundedCornerShape(adaptiveButtonHeight / 2)
        ) {
            Text(
                text = when {
                    timerStatus.canStart -> stringResource(R.string.start)
                    timerStatus.canPause -> stringResource(R.string.pause)
                    timerStatus.canResume -> stringResource(R.string.resume)
                    else -> stringResource(R.string.start)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        // Reset button
        OutlinedButton(
            onClick = { viewModel.resetTimer() },
            enabled = timerStatus.canReset,
            modifier = modifier
                .heightIn(min = adaptiveButtonHeight)
                .widthIn(min = MinTouchTargetSize * 2),
            shape = RoundedCornerShape(adaptiveButtonHeight / 2)
        ) {
            Text(
                text = stringResource(R.string.reset),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
