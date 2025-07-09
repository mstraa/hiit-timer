package com.hiittimer.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hiittimer.app.ui.history.WorkoutAnalytics

/**
 * Analytics overview card (FR-012: Progress Analytics)
 */
@Composable
fun WorkoutAnalyticsCard(
    analytics: WorkoutAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Workout Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Completion rates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsMetricCard(
                    title = "Overall",
                    value = "${analytics.overallCompletionRate.toInt()}%",
                    subtitle = "Completion Rate",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )

                AnalyticsMetricCard(
                    title = "This Week",
                    value = "${analytics.weeklyCompletionRate.toInt()}%",
                    subtitle = "Completion Rate",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Workout counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsMetricCard(
                    title = "Completed",
                    value = "${analytics.totalWorkoutsCompleted}",
                    subtitle = "of ${analytics.totalWorkoutsAttempted} workouts",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
                
                AnalyticsMetricCard(
                    title = "Average",
                    value = analytics.getFormattedAverageDuration(),
                    subtitle = "Workout Duration",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual analytics metric card
 */
@Composable
fun AnalyticsMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Personal records section (FR-012: Highlight personal records)
 */
@Composable
fun PersonalRecordsCard(
    analytics: WorkoutAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Personal Records",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Longest session
            analytics.longestSession?.let { session ->
                PersonalRecordItem(
                    title = "Longest Session",
                    value = session.getFormattedDuration(),
                    subtitle = "${session.presetName} • ${session.completedRounds} rounds"
                )
            }
            
            // Most rounds completed
            analytics.mostRoundsSession?.let { session ->
                PersonalRecordItem(
                    title = "Most Rounds",
                    value = "${session.completedRounds} rounds",
                    subtitle = "${session.presetName} • ${session.getFormattedDuration()}"
                )
            }
            
            // Consistency metrics
            val consistency = analytics.consistencyMetrics
            PersonalRecordItem(
                title = "Current Streak",
                value = "${consistency.currentStreak} days",
                subtitle = "Longest: ${consistency.longestStreak} days"
            )
            
            PersonalRecordItem(
                title = "Weekly Average",
                value = "${consistency.averageWorkoutsPerWeek.toInt()} workouts",
                subtitle = "${consistency.totalActiveDays} active days total"
            )
        }
    }
}

/**
 * Individual personal record item
 */
@Composable
fun PersonalRecordItem(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Loading state for analytics
 */
@Composable
fun AnalyticsLoadingCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Loading analytics...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Empty state for analytics when no data is available
 */
@Composable
fun AnalyticsEmptyCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No Analytics Available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "Complete some workouts to see your progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
