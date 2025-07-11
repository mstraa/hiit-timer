package com.hiittimer.app.ui.timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowForward
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

    // State for new UI components
    var showConfigModal by remember { mutableStateOf(false) }
    var showPresetModal by remember { mutableStateOf(false) }
    var showMenuPanel by remember { mutableStateOf(false) }

    // Background color based on theme
    val backgroundColor = MaterialTheme.colorScheme.background

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top bar
            TimerTopBar(
                timerState = timerStatus.state,
                onMenuClick = { showMenuPanel = true },
                onAudioToggle = { viewModel.toggleAudio() },
                audioSettings = audioSettings,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Main timer display - centered in the middle
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
            }

            // Bottom controls
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                if (workoutMode == UnifiedTimerManager.WorkoutMode.SIMPLE) {
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
                } else {
                    // Complex workout controls
                    ComplexWorkoutControls(
                        timerState = unifiedState.timerState,
                        onPlayPauseClick = {
                            when (unifiedState.timerState) {
                                TimerState.RUNNING -> viewModel.pauseTimer()
                                TimerState.PAUSED -> viewModel.resumeTimer()
                                else -> viewModel.startTimer()
                            }
                        },
                        onResetClick = { viewModel.resetTimer() },
                        onSkipClick = { viewModel.skipToNext() },
                        onRepCompleted = { viewModel.markRepCompleted() },
                        needsRepInput = unifiedState.needsRepInput
                    )
                }
            }
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Main timer display - exactly centered
        Text(
            text = unifiedState.displayTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        // Other elements positioned around the timer
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phase indicator pill (positioned above timer)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PhaseIndicatorPill(
                        timerState = unifiedState.timerState,
                        intervalType = unifiedState.intervalType,
                        statusText = unifiedState.statusText,
                        isLarge = true
                    )
                    
                    // Next phase info
                    NextPhaseInfo(state = unifiedState)
                    
                    Spacer(modifier = Modifier.height(120.dp)) // Space for timer
                }
            }
            
            // Progress info (positioned below timer)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = unifiedState.progressText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 80.dp)
                )
            }
        }
    }
}

@Composable
private fun TimerTopBar(
    timerState: TimerState,
    onMenuClick: () -> Unit,
    onAudioToggle: () -> Unit,
    audioSettings: com.hiittimer.app.audio.AudioSettings,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            
            // Complex workout button (icon)
            IconButton(
                onClick = onComplexWorkoutClick
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Workouts",
                    tint = MaterialTheme.colorScheme.onSurface
                )
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
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Resume",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onResetClick,
                        modifier = Modifier
                            .weight(0.5f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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

@Composable
private fun PhaseIndicatorPill(
    timerState: TimerState,
    intervalType: IntervalType,
    statusText: String,
    isLarge: Boolean = false
) {
    val (backgroundColor, textColor) = when {
        timerState == TimerState.BEGIN -> Pair(
            Color(0xFF1976D2).copy(alpha = 0.15f), // Subtle blue
            Color(0xFF1976D2)
        )
        intervalType == IntervalType.WORK -> Pair(
            Color(0xFF388E3C).copy(alpha = 0.15f), // Subtle green
            Color(0xFF388E3C)
        )
        intervalType == IntervalType.REST -> Pair(
            Color(0xFFD32F2F).copy(alpha = 0.15f), // Subtle red
            Color(0xFFD32F2F)
        )
        else -> Pair(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface
        )
    }
    
    Surface(
        shape = RoundedCornerShape(if (isLarge) 24.dp else 16.dp),
        color = backgroundColor,
        modifier = Modifier.padding(
            horizontal = if (isLarge) 16.dp else 8.dp,
            vertical = if (isLarge) 8.dp else 0.dp
        )
    ) {
        Text(
            text = statusText,
            style = if (isLarge) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = if (isLarge) 32.dp else 16.dp, 
                vertical = if (isLarge) 16.dp else 8.dp
            )
        )
    }
}

@Composable
private fun NextPhaseInfo(state: UnifiedTimerState) {
    // Get next phase information with time
    val nextPhaseInfo = when {
        state.timerState == TimerState.BEGIN -> {
            if (state.currentExerciseName != null) {
                "${state.currentExerciseName} - 00:30" // Default work time, should be dynamic
            } else {
                "Work - 00:30" // Default work time
            }
        }
        state.intervalType == IntervalType.WORK -> "Rest - 00:15" // Default rest time
        state.intervalType == IntervalType.REST -> {
            if (state.currentExerciseName != null) {
                "${state.currentExerciseName} - 00:30"
            } else {
                "Work - 00:30"
            }
        }
        else -> null
    }
    
    if (nextPhaseInfo != null) {
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = nextPhaseInfo,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ComplexWorkoutControls(
    timerState: TimerState,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onSkipClick: () -> Unit,
    onRepCompleted: () -> Unit,
    needsRepInput: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (timerState) {
            TimerState.STOPPED, TimerState.FINISHED -> {
                Button(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (timerState == TimerState.FINISHED) "New Workout" else "Start",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TimerState.BEGIN, TimerState.RUNNING -> {
                // Pause button
                Button(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Pause",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Skip button
                OutlinedButton(
                    onClick = onSkipClick,
                    modifier = Modifier
                        .weight(0.3f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Skip",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Rep completed button (if needed)
                if (needsRepInput) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRepCompleted,
                        modifier = Modifier
                            .weight(0.3f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Rep Done",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            TimerState.PAUSED -> {
                // Resume button
                Button(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .weight(0.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Resume",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Reset button
                OutlinedButton(
                    onClick = onResetClick,
                    modifier = Modifier
                        .weight(0.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}