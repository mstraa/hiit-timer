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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiittimer.app.R
import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.data.Preset
import com.hiittimer.app.ui.components.*
import com.hiittimer.app.ui.theme.HIITColors
import com.hiittimer.app.timer.UnifiedTimerManager
import com.hiittimer.app.timer.UnifiedTimerState
import com.hiittimer.app.ui.workouts.ComplexTimerDisplay

/**
 * Timer screen that supports both simple and complex workouts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: UnifiedTimerViewModel,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToWorkouts: () -> Unit = {}
) {
    val timerStatus by viewModel.timerStatus.collectAsState()
    val unifiedState by viewModel.unifiedTimerState.collectAsState()
    val workoutMode by viewModel.workoutMode.collectAsState()
    val audioSettings by viewModel.audioSettings.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    // State for new UI components
    var showAudioModal by remember { mutableStateOf(false) }
    var showConfigModal by remember { mutableStateOf(false) }
    var showPresetModal by remember { mutableStateOf(false) }
    var showMenuPanel by remember { mutableStateOf(false) }

    // Background color based on state
    val backgroundColor = when {
        isDarkTheme -> Color.Black
        timerStatus.currentInterval == IntervalType.REST -> Color(0x20F44336)
        timerStatus.currentInterval == IntervalType.WORK -> Color(0x204CAF50)
        else -> MaterialTheme.colorScheme.background
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Visual feedback overlay
        VisualFeedbackOverlay(
            timerStatus = timerStatus
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            TimerTopBar(
                timerState = timerStatus.state,
                onMenuClick = { showMenuPanel = true },
                onAudioToggle = { showAudioModal = true },
                audioSettings = audioSettings
            )

            // Main timer display - use ComplexTimerDisplay for complex workouts
            when (workoutMode) {
                UnifiedTimerManager.WorkoutMode.COMPLEX -> {
                    ComplexTimerDisplay(
                        state = unifiedState,
                        onPlayPauseClick = {
                            when (unifiedState.timerState) {
                                TimerState.RUNNING -> viewModel.pauseTimer()
                                TimerState.PAUSED -> viewModel.resumeTimer()
                                else -> viewModel.startTimer()
                            }
                        },
                        onResetClick = { viewModel.resetTimer() },
                        onSkipClick = { viewModel.skipToNext() },
                        onRepCompleted = { viewModel.markRepCompleted() }
                    )
                }
                UnifiedTimerManager.WorkoutMode.SIMPLE -> {
                    if (unifiedState.timerState == TimerState.BEGIN) {
                        // Use ComplexTimerDisplay for BEGIN phase to get the nice countdown UI
                        ComplexTimerDisplay(
                            state = unifiedState,
                            onPlayPauseClick = { viewModel.pauseTimer() },
                            onResetClick = { viewModel.resetTimer() },
                            onSkipClick = { /* No skip for simple workouts */ },
                            onRepCompleted = { /* No reps for simple workouts */ }
                        )
                    } else {
                        SimpleTimerDisplay(
                            unifiedState = unifiedState
                        )
                    }
                }
            }

            // Bottom controls
            TimerBottomControls(
                timerState = timerStatus.state,
                onConfigClick = { showConfigModal = true },
                onPresetClick = { showPresetModal = true },
                onStartClick = { viewModel.startTimer() },
                onPauseClick = { viewModel.pauseTimer() },
                onResumeClick = { viewModel.resumeTimer() },
                onResetClick = { viewModel.resetTimer() },
                onComplexWorkoutClick = onNavigateToWorkouts
            )
        }

        // Interval transition effect
        IntervalTransitionEffect(timerStatus = timerStatus)

        // Menu panel
        HamburgerMenuPanel(
            isOpen = showMenuPanel,
            onDismiss = { showMenuPanel = false },
            viewModel = viewModel,
            onNavigateToHistory = onNavigateToHistory
        )
    }

    // Modals
    if (showConfigModal) {
        TimerConfigModal(
            isOpen = showConfigModal,
            onClose = { showConfigModal = false },
            config = timerStatus.config,
            onConfigUpdate = { newConfig ->
                viewModel.updateConfig(
                    workTimeSeconds = newConfig.workTimeSeconds,
                    restTimeSeconds = newConfig.restTimeSeconds,
                    totalRounds = newConfig.totalRounds,
                    isUnlimited = newConfig.isUnlimited,
                    noRest = newConfig.noRest
                )
            },
            onResetTimer = {
                viewModel.resetTimer()
            }
        )
    }

    if (showPresetModal) {
        PresetModal(
            isOpen = showPresetModal,
            onClose = { showPresetModal = false },
            currentConfig = timerStatus.config,
            onPresetSelected = { preset ->
                viewModel.startTimerWithPreset(preset)
                showPresetModal = false
            }
        )
    }
}

@Composable
private fun SimpleTimerDisplay(
    unifiedState: UnifiedTimerState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Status text
        Text(
            text = unifiedState.statusText,
            style = MaterialTheme.typography.headlineMedium,
            color = when (unifiedState.intervalType) {
                IntervalType.WORK -> HIITColors.WorkIndicatorLight
                IntervalType.REST -> HIITColors.RestIndicatorLight
                else -> MaterialTheme.colorScheme.onBackground
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Main timer display
        Text(
            text = unifiedState.displayTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = when (unifiedState.intervalType) {
                IntervalType.WORK -> HIITColors.WorkIndicatorLight
                IntervalType.REST -> HIITColors.RestIndicatorLight
                else -> MaterialTheme.colorScheme.onBackground
            },
            textAlign = TextAlign.Center
        )

        // Progress info
        Text(
            text = unifiedState.progressText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun TimerTopBar(
    timerState: TimerState,
    onMenuClick: () -> Unit,
    onAudioToggle: () -> Unit,
    audioSettings: com.hiittimer.app.audio.AudioSettings
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HamburgerMenuButton(onClick = onMenuClick)
        AudioToggleButton(
            audioSettings = audioSettings,
            onToggleAudio = onAudioToggle
        )
    }
}

@Composable
private fun TimerBottomControls(
    timerState: TimerState,
    onConfigClick: () -> Unit,
    onPresetClick: () -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onResetClick: () -> Unit,
    onComplexWorkoutClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Config and preset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimerConfigButton(onClick = onConfigClick)
            PresetButton(onClick = onPresetClick)
            
            // Complex workout button
            OutlinedButton(
                onClick = onComplexWorkoutClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 120.dp)
            ) {
                Text("Workouts")
            }
        }

        // Main control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            when (timerState) {
                TimerState.STOPPED -> {
                    Button(
                        onClick = onStartClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TimerState.BEGIN, TimerState.RUNNING -> {
                    Button(
                        onClick = onPauseClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Pause",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TimerState.PAUSED -> {
                    Button(
                        onClick = onResumeClick,
                        modifier = Modifier
                            .weight(0.5f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Resume",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onResetClick,
                        modifier = Modifier
                            .weight(0.5f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                TimerState.FINISHED -> {
                    Button(
                        onClick = onResetClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "New Workout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {} // Handle IDLE state if needed
            }
        }
    }
}