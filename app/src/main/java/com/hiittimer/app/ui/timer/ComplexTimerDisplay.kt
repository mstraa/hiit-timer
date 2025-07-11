package com.hiittimer.app.ui.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiittimer.app.data.WorkoutMode
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.timer.UnifiedTimerState

/**
 * Timer display component for complex workouts
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
    val backgroundColor by animateColorAsState(
        targetValue = state.backgroundColor,
        label = "backgroundColor"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section - Phase and exercise info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            // Phase progress
            if (state.totalPhases > 0) {
                PhaseProgressIndicator(
                    currentPhase = state.currentPhaseIndex + 1,
                    totalPhases = state.totalPhases,
                    phaseName = state.currentPhaseName ?: ""
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Exercise name
            if (state.currentExerciseName != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = state.currentExerciseName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Status text
            Text(
                text = state.statusText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Middle section - Timer display
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Countdown overlay
                state.countdownText != null -> {
                    Text(
                        text = state.countdownText,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Rep-based display
                state.needsRepInput -> {
                    RepCounterDisplay(
                        displayText = state.displayTime,
                        onRepCompleted = onRepCompleted,
                        exerciseMode = state.exerciseMode
                    )
                }
                
                // Time-based display
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.displayTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 80.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = backgroundColor
                        )
                        
                        // AMRAP rounds
                        if (state.exerciseMode == WorkoutMode.AMRAP && state.amrapRounds > 0) {
                            Text(
                                text = "Round ${state.amrapRounds + 1}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom section - Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            // Progress text
            Text(
                text = state.progressText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset button
                if (state.canReset) {
                    OutlinedIconButton(
                        onClick = onResetClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Play/Pause button
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(80.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = backgroundColor
                    )
                ) {
                    Icon(
                        imageVector = when {
                            state.canStart -> Icons.Default.PlayArrow
                            state.canPause -> Icons.Default.PlayArrow
                            state.canResume -> Icons.Default.PlayArrow
                            else -> Icons.Default.Check
                        },
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Skip button (for complex workouts)
                if (state.isRunning && state.exerciseMode != null) {
                    OutlinedIconButton(
                        onClick = onSkipClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Skip",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhaseProgressIndicator(
    currentPhase: Int,
    totalPhases: Int,
    phaseName: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(totalPhases) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < currentPhase) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Phase $currentPhase: $phaseName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RepCounterDisplay(
    displayText: String,
    onRepCompleted: () -> Unit,
    exerciseMode: WorkoutMode?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 64.sp
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        FilledTonalButton(
            onClick = onRepCompleted,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (exerciseMode == WorkoutMode.FOR_TIME) "Complete" else "Rep Done",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        if (exerciseMode == WorkoutMode.REP_BASED) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap when you complete each rep",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}