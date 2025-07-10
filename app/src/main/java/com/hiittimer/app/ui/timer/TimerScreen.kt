package com.hiittimer.app.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import com.hiittimer.app.ui.components.HamburgerMenuButton
import com.hiittimer.app.ui.components.HamburgerMenuPanel
import com.hiittimer.app.ui.components.IntervalTransitionEffect
import com.hiittimer.app.ui.components.TimerConfigButton
import com.hiittimer.app.ui.components.TimerConfigModal
import com.hiittimer.app.ui.components.VisualFeedbackOverlay
import com.hiittimer.app.ui.fullscreen.FullscreenController
import com.hiittimer.app.ui.theme.HIITColors
import com.hiittimer.app.ui.utils.*
import com.hiittimer.app.utils.Logger
import com.hiittimer.app.error.ErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel(),
    onNavigateToHistory: () -> Unit = {}
) {
    val timerStatus by viewModel.timerStatus.collectAsState()
    val audioSettings by viewModel.audioSettings.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    // State for new UI components (FR-016: Settings UI Reorganization)
    var isHamburgerMenuOpen by remember { mutableStateOf(false) }
    var isTimerConfigOpen by remember { mutableStateOf(false) }

    // Fullscreen mode controller (FR-019: True Fullscreen Mode)
    FullscreenController(
        timerState = timerStatus.state,
        isSettingsOpen = isHamburgerMenuOpen,
        isConfigOpen = isTimerConfigOpen
    )

    // Responsive design values (FR-015: Responsive Design)
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content area with padding for gesture navigation
        Box(
            modifier = Modifier
                .fillMaxSize()
                // FR-019: Edge-to-edge content with appropriate padding for gesture areas
                .padding(horizontal = 16.dp) // Minimum margins for gesture navigation
        ) {
            // Adaptive layout based on orientation (FR-015: Landscape support)
            if (isLandscapeMode) {
                LandscapeTimerLayout(
                    timerStatus = timerStatus,
                    intervalColor = intervalColor,
                    adaptivePadding = adaptivePadding,
                    adaptiveSpacing = adaptiveSpacing,
                    adaptiveButtonHeight = adaptiveButtonHeight,
                    adaptiveTimerFontSize = adaptiveTimerFontSize,
                    viewModel = viewModel,
                    onOpenHamburgerMenu = { isHamburgerMenuOpen = true },
                    onOpenTimerConfig = { isTimerConfigOpen = true }
                )
            } else {
                PortraitTimerLayout(
                    timerStatus = timerStatus,
                    intervalColor = intervalColor,
                    adaptivePadding = adaptivePadding,
                    adaptiveSpacing = adaptiveSpacing,
                    adaptiveButtonHeight = adaptiveButtonHeight,
                    adaptiveTimerFontSize = adaptiveTimerFontSize,
                    viewModel = viewModel,
                    onOpenHamburgerMenu = { isHamburgerMenuOpen = true },
                    onOpenTimerConfig = { isTimerConfigOpen = true }
                )
            }
        }

        // Visual feedback overlays (FR-004: Full-screen visual indicators)
        // These are positioned outside the content padding to cover the entire screen
        VisualFeedbackOverlay(
            timerStatus = timerStatus,
            modifier = Modifier.fillMaxSize()
        )

        // Interval transition effects - full screen without padding
        IntervalTransitionEffect(
            timerStatus = timerStatus,
            modifier = Modifier.fillMaxSize()
        )

        // Hamburger menu panel (FR-016: Settings UI Reorganization)
        // Positioned with content padding to respect gesture areas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            HamburgerMenuPanel(
                isOpen = isHamburgerMenuOpen,
                onClose = { isHamburgerMenuOpen = false },
                audioSettings = audioSettings,
                themePreference = themePreference,
                onToggleAudio = { viewModel.toggleAudio() },
                onVolumeChange = { volume -> viewModel.setAudioVolume(volume) },
                onThemeChange = { preference -> viewModel.setThemePreference(preference) },
                onNavigateToHistory = onNavigateToHistory
            )

            // Timer configuration modal (FR-016: Settings UI Reorganization)
            TimerConfigModal(
                isOpen = isTimerConfigOpen,
                onClose = { isTimerConfigOpen = false },
                config = timerStatus.config,
                onConfigUpdate = { config ->
                    viewModel.updateConfigAndReset(
                        workTimeSeconds = config.workTimeSeconds,
                        restTimeSeconds = config.restTimeSeconds,
                        totalRounds = config.totalRounds,
                        isUnlimited = config.isUnlimited,
                        noRest = config.noRest,
                        countdownDurationSeconds = config.countdownDurationSeconds
                    )
                },
                onResetTimer = { viewModel.resetTimer() }
            )
        }
    }
}

/**
 * Portrait layout for timer screen (FR-015: Portrait orientation support)
 */
@Composable
private fun PortraitTimerLayout(
    timerStatus: TimerStatus,
    intervalColor: Color,
    adaptivePadding: Dp,
    adaptiveSpacing: Dp,
    adaptiveButtonHeight: Dp,
    adaptiveTimerFontSize: androidx.compose.ui.unit.TextUnit,
    viewModel: TimerViewModel,
    onOpenHamburgerMenu: () -> Unit,
    onOpenTimerConfig: () -> Unit
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
            adaptiveSpacing = adaptiveSpacing,
            onOpenHamburgerMenu = onOpenHamburgerMenu,
            onOpenTimerConfig = onOpenTimerConfig
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
    intervalColor: Color,
    adaptivePadding: Dp,
    adaptiveSpacing: Dp,
    adaptiveButtonHeight: Dp,
    adaptiveTimerFontSize: androidx.compose.ui.unit.TextUnit,
    viewModel: TimerViewModel,
    onOpenHamburgerMenu: () -> Unit,
    onOpenTimerConfig: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = adaptivePadding)
    ) {
        // Header (compact in landscape)
        TimerHeader(
            adaptiveSpacing = adaptiveSpacing,
            onOpenHamburgerMenu = onOpenHamburgerMenu,
            onOpenTimerConfig = onOpenTimerConfig,
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
    adaptiveSpacing: Dp,
    onOpenHamburgerMenu: () -> Unit,
    onOpenTimerConfig: () -> Unit,
    isCompact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) // Match main content background
            .padding(vertical = if (isCompact) adaptiveSpacing / 2 else adaptiveSpacing),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(adaptiveSpacing)
        ) {
            // Hamburger menu button (FR-016: Settings UI Reorganization)
            HamburgerMenuButton(
                onClick = onOpenHamburgerMenu,
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

        // Timer configuration button (FR-016: Settings UI Reorganization)
        TimerConfigButton(
            onClick = onOpenTimerConfig,
            modifier = Modifier.size(MinTouchTargetSize)
        )
    }
}

/**
 * Format time with styled deciseconds (smaller font size for decimal portion)
 */
@Composable
private fun formatTimeWithStyledDeciseconds(
    timerStatus: TimerStatus,
    mainFontSize: androidx.compose.ui.unit.TextUnit
): AnnotatedString {
    val timeString = timerStatus.formatTimeRemaining()
    val decisecondFontSize = mainFontSize * 0.5f // Half the size for deciseconds

    return buildAnnotatedString {
        // Find the decimal point to split main time from deciseconds
        val decimalIndex = timeString.indexOf('.')

        if (decimalIndex != -1) {
            // Main time portion (everything before the decimal)
            withStyle(
                style = SpanStyle(
                    fontSize = mainFontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            ) {
                append(timeString.substring(0, decimalIndex))
            }

            // Decisecond portion (decimal point and digit)
            withStyle(
                style = SpanStyle(
                    fontSize = decisecondFontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            ) {
                append(timeString.substring(decimalIndex))
            }
        } else {
            // Fallback: if no decimal point found, use normal styling
            withStyle(
                style = SpanStyle(
                    fontSize = mainFontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            ) {
                append(timeString)
            }
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
        // Interval type indicator with special handling for BEGIN countdown
        Card(
            modifier = Modifier.padding(bottom = adaptiveSpacing * 2),
            colors = CardDefaults.cardColors(
                containerColor = if (timerStatus.isBegin) {
                    Color.Blue.copy(alpha = 0.1f)
                } else {
                    intervalColor.copy(alpha = 0.1f)
                }
            )
        ) {
            Text(
                text = when {
                    timerStatus.isBegin -> timerStatus.getCountdownDisplayText() ?: "BEGIN"
                    timerStatus.isWorkInterval -> stringResource(R.string.work)
                    else -> stringResource(R.string.rest)
                },
                modifier = Modifier.padding(horizontal = adaptiveSpacing * 2, vertical = adaptiveSpacing),
                style = MaterialTheme.typography.headlineMedium,
                color = if (timerStatus.isBegin) {
                    Color.Blue
                } else {
                    intervalColor
                },
                fontWeight = FontWeight.Bold
            )
        }

        // Large timer display with adaptive font size and smaller deciseconds
        Text(
            text = formatTimeWithStyledDeciseconds(timerStatus, adaptiveTimerFontSize),
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

        // Next interval preview (FR-005: Always visible during running)
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
        // Horizontal layout for portrait mode (FR-016: 80%/20% width split)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = adaptiveSpacing),
            horizontalArrangement = Arrangement.spacedBy(adaptiveSpacing)
        ) {
            // Start button occupying 80% of control area width (FR-016)
            TimerButton(
                timerStatus = timerStatus,
                adaptiveButtonHeight = adaptiveButtonHeight,
                viewModel = viewModel,
                isPrimary = true,
                modifier = Modifier.weight(0.8f)
            )

            // Reset button occupying 20% of control area width (FR-016)
            TimerButton(
                timerStatus = timerStatus,
                adaptiveButtonHeight = adaptiveButtonHeight,
                viewModel = viewModel,
                isPrimary = false,
                modifier = Modifier.weight(0.2f)
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
                Logger.d(ErrorHandler.ErrorCategory.USER_INPUT, "Primary button clicked - State: ${timerStatus.state}, canStart: ${timerStatus.canStart}, canPause: ${timerStatus.canPause}, canResume: ${timerStatus.canResume}")
                when {
                    timerStatus.canStart -> {
                        Logger.d(ErrorHandler.ErrorCategory.USER_INPUT, "Starting timer")
                        viewModel.startTimer()
                    }
                    timerStatus.canPause -> {
                        Logger.d(ErrorHandler.ErrorCategory.USER_INPUT, "Pausing timer")
                        viewModel.pauseTimer()
                    }
                    timerStatus.canResume -> {
                        Logger.d(ErrorHandler.ErrorCategory.USER_INPUT, "Resuming timer")
                        viewModel.resumeTimer()
                    }
                    else -> {
                        Logger.w(ErrorHandler.ErrorCategory.USER_INPUT, "Primary button clicked but no valid action available")
                    }
                }
            },
            enabled = timerStatus.canStart || timerStatus.canPause || timerStatus.canResume,
            modifier = modifier
                .heightIn(min = adaptiveButtonHeight)
                .widthIn(min = MinTouchTargetSize * 2),
            shape = RoundedCornerShape(adaptiveButtonHeight / 2)
        ) {
            val buttonText = when {
                timerStatus.canStart -> stringResource(R.string.start)
                timerStatus.canPause -> stringResource(R.string.pause)
                timerStatus.canResume -> stringResource(R.string.resume)
                else -> stringResource(R.string.start)
            }
            // Debug logging for button text and state transitions
            LaunchedEffect(timerStatus.state, timerStatus.canStart, timerStatus.canPause, timerStatus.canResume) {
                Logger.d(ErrorHandler.ErrorCategory.USER_INPUT, "Timer button state: ${timerStatus.state}, canStart=${timerStatus.canStart}, canPause=${timerStatus.canPause}, canResume=${timerStatus.canResume}, text='$buttonText'")
            }
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        // Reset button with icon (FR-020: Reset Button UI Enhancement)
        OutlinedButton(
            onClick = {
                Logger.d(ErrorHandler.ErrorCategory.USER_INPUT, "Reset button clicked - State: ${timerStatus.state}, canReset: ${timerStatus.canReset}")
                viewModel.resetTimer()
            },
            enabled = timerStatus.canReset,
            modifier = modifier
                .heightIn(min = adaptiveButtonHeight)
                .widthIn(min = MinTouchTargetSize * 2),
            shape = RoundedCornerShape(adaptiveButtonHeight / 2)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset timer", // FR-020: Accessibility support
                modifier = Modifier.size(24.dp), // FR-020: Minimum 24dp icon size
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
