package com.hiittimer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hiittimer.app.data.WorkoutSession
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component for displaying a single workout session (FR-011: History Management)
 */
@Composable
fun WorkoutSessionCard(
    session: WorkoutSession,
    onDeleteSession: (WorkoutSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row with completion status and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Completion status icon (FR-011: Visual indicators)
                    Icon(
                        imageVector = if (session.isCompleted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = if (session.isCompleted) "Completed" else "Incomplete",
                        tint = if (session.isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = session.presetName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { onDeleteSession(session) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete session",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Exercise name (if available)
            session.exerciseName?.let { exerciseName ->
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Workout summary
            Text(
                text = session.getWorkoutSummary(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            // Completion status and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.getCompletionStatusText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = session.getFormattedDuration(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Date
            Text(
                text = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    .format(Date(session.startTime)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * List component for displaying workout sessions (FR-011: History Management)
 */
@Composable
fun WorkoutSessionList(
    sessions: List<WorkoutSession>,
    onDeleteSession: (WorkoutSession) -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No workout sessions found"
) {
    if (sessions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Start a workout to see your history here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(sessions) { session ->
                WorkoutSessionCard(
                    session = session,
                    onDeleteSession = onDeleteSession
                )
            }
        }
    }
}

/**
 * Filter controls for workout history (FR-011: Filter by date range, preset, completion)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryFilters(
    currentFilter: com.hiittimer.app.data.WorkoutHistoryFilter,
    onFilterChange: (com.hiittimer.app.data.WorkoutHistoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Date filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.hiittimer.app.data.DateFilter.values().forEach { dateFilter ->
                    val isSelected = currentFilter.dateFilter == dateFilter
                    Button(
                        onClick = {
                            onFilterChange(currentFilter.copy(dateFilter = dateFilter))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = when (dateFilter) {
                                com.hiittimer.app.data.DateFilter.LAST_7_DAYS -> "7 Days"
                                com.hiittimer.app.data.DateFilter.LAST_30_DAYS -> "30 Days"
                                com.hiittimer.app.data.DateFilter.LAST_3_MONTHS -> "3 Months"
                                com.hiittimer.app.data.DateFilter.ALL_TIME -> "All Time"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Completion filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.hiittimer.app.data.CompletionFilter.values().forEach { completionFilter ->
                    val isSelected = currentFilter.completionFilter == completionFilter
                    Button(
                        onClick = {
                            onFilterChange(currentFilter.copy(completionFilter = completionFilter))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = when (completionFilter) {
                                com.hiittimer.app.data.CompletionFilter.ALL -> "All"
                                com.hiittimer.app.data.CompletionFilter.COMPLETED_ONLY -> "Completed"
                                com.hiittimer.app.data.CompletionFilter.INCOMPLETE_ONLY -> "Incomplete"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog for session deletion
 */
@Composable
fun DeleteSessionDialog(
    session: WorkoutSession?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (session != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Delete Workout Session",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this workout session? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
