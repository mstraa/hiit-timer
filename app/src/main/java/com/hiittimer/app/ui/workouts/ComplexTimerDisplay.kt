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
        // Phase info
        Text(
            text = state.currentPhaseName ?: "Workout",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Countdown display
        Text(
            text = state.countdownText ?: "Get Ready!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Large countdown number
        Text(
            text = state.displayTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Next exercise preview
        if (state.currentExerciseName != null) {
            Text(
                text = "Next: ${state.currentExerciseName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
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
        // Phase and exercise info
        if (state.currentPhaseName != null) {
            Text(
                text = state.currentPhaseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        if (state.currentExerciseName != null) {
            Text(
                text = state.currentExerciseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main timer display
        Text(
            text = state.displayTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = when {
                state.timerState == TimerState.PAUSED -> MaterialTheme.colorScheme.error
                state.exerciseMode == WorkoutMode.REP_BASED -> HIITColors.WorkIndicatorLight
                else -> MaterialTheme.colorScheme.onBackground
            },
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress info
        Text(
            text = state.progressText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            FloatingActionButton(
                onClick = onPlayPauseClick,
                containerColor = if (state.timerState == TimerState.PAUSED) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            ) {
                Icon(
                    imageVector = if (state.timerState == TimerState.PAUSED) {
                        Icons.Default.PlayArrow
                    } else {
                        Icons.Default.PlayArrow // Use PlayArrow for now, can be updated later
                    },
                    contentDescription = if (state.timerState == TimerState.PAUSED) "Resume" else "Pause"
                )
            }
            
            // Rep completed button (for rep-based exercises)
            if (state.needsRepInput) {
                FloatingActionButton(
                    onClick = onRepCompleted,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Rep Completed"
                    )
                }
            }
            
            // Skip button
            OutlinedButton(
                onClick = onSkipClick,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Skip Exercise"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Skip")
            }
        }
        
        // Exercise mode specific info
        when (state.exerciseMode) {
            WorkoutMode.AMRAP -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AMRAP - Rounds: ${state.amrapRounds}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
            WorkoutMode.REP_BASED, WorkoutMode.FOR_TIME -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap ✓ when rep completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
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