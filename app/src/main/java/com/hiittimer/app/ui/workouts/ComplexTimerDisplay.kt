package com.hiittimer.app.ui.workouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.WorkoutMode
import com.hiittimer.app.timer.UnifiedTimerState
import com.hiittimer.app.ui.theme.HIITColors

/**
 * Display component for complex workouts
 */
@Composable
fun ComplexTimerDisplay(
    state: UnifiedTimerState,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onSkipClick: () -> Unit,
    onRepCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (state.timerState) {
            TimerState.BEGIN -> {
                BeginCountdownDisplay(state = state)
            }
            TimerState.RUNNING, TimerState.PAUSED -> {
                WorkoutActiveDisplay(
                    state = state,
                    onPlayPauseClick = onPlayPauseClick,
                    onResetClick = onResetClick,
                    onSkipClick = onSkipClick,
                    onRepCompleted = onRepCompleted
                )
            }
            TimerState.FINISHED -> {
                WorkoutFinishedDisplay(
                    state = state,
                    onResetClick = onResetClick
                )
            }
            else -> {
                WorkoutIdleDisplay(
                    state = state,
                    onPlayPauseClick = onPlayPauseClick
                )
            }
        }
    }
}

@Composable
private fun BeginCountdownDisplay(
    state: UnifiedTimerState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase indicator pill showing "Begin"
        PhaseIndicatorPill(
            timerState = state.timerState,
            intervalType = state.intervalType,
            statusText = "Begin",
            isLarge = true
        )
        
        // Next phase info
        NextPhaseInfo(state = state)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Large countdown number
        Text(
            text = state.displayTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WorkoutActiveDisplay(
    state: UnifiedTimerState,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onSkipClick: () -> Unit,
    onRepCompleted: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase indicator pill showing Work/Rest
        PhaseIndicatorPill(
            timerState = state.timerState,
            intervalType = state.intervalType,
            statusText = when (state.intervalType) {
                IntervalType.WORK -> "Work"
                IntervalType.REST -> "Rest"
                else -> state.statusText
            },
            isLarge = true
        )
        
        // Next phase info
        NextPhaseInfo(state = state)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Exercise name (only if available)
        if (state.currentExerciseName != null) {
            Text(
                text = state.currentExerciseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Main timer display (larger)
        Text(
            text = state.displayTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        // Progress info (phase or round)
        Text(
            text = state.progressText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 16.dp)
        )
        
        // Exercise mode specific info (compact)
        when (state.exerciseMode) {
            WorkoutMode.AMRAP -> {
                Text(
                    text = "AMRAP - Rounds: ${state.amrapRounds}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            WorkoutMode.REP_BASED, WorkoutMode.FOR_TIME -> {
                if (state.needsRepInput) {
                    Text(
                        text = "Tap ✓ when rep completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun WorkoutFinishedDisplay(
    state: UnifiedTimerState,
    onResetClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 80.sp
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Workout Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Great job finishing your workout!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onResetClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
        ) {
            Text(
                text = "New Workout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WorkoutIdleDisplay(
    state: UnifiedTimerState,
    onPlayPauseClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.currentPhaseName != null) {
            Text(
                text = state.currentPhaseName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Text(
            text = "Ready to start!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        FloatingActionButton(
            onClick = onPlayPauseClick,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start Workout",
                modifier = Modifier.size(32.dp)
            )
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