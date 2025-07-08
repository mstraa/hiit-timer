package com.hiittimer.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hiittimer.app.data.WorkoutHistoryRepository
import com.hiittimer.app.data.WorkoutSession
import com.hiittimer.app.ui.components.*
import com.hiittimer.app.ui.utils.*

/**
 * Main workout history screen (FR-011: History Management, FR-012: Progress Analytics)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    workoutHistoryRepository: WorkoutHistoryRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { WorkoutHistoryViewModel(workoutHistoryRepository) }
    val uiState by viewModel.uiState.collectAsState()
    
    // UI state
    var showFilters by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<WorkoutSession?>(null) }
    
    // Responsive design values
    val adaptivePadding = getAdaptivePadding()
    val adaptiveSpacing = getAdaptiveSpacing()
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(adaptivePadding)
    ) {
        // Header with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = adaptiveSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(MinTouchTargetSize)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Workout History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = adaptiveSpacing)
            )
            
            // Filter toggle button
            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier.size(MinTouchTargetSize)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Toggle filters",
                    tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.searchSessions(it)
            },
            label = { Text("Search workouts...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = adaptiveSpacing),
            singleLine = true
        )
        
        // Filters (collapsible)
        if (showFilters) {
            WorkoutHistoryFilters(
                currentFilter = uiState.filter,
                onFilterChange = { filter -> viewModel.applyFilter(filter) },
                modifier = Modifier.padding(bottom = adaptiveSpacing)
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(adaptiveSpacing)
        ) {
            // Analytics section (FR-012: Progress Analytics)
            if (uiState.isAnalyticsLoading) {
                AnalyticsLoadingCard()
            } else {
                val analytics = uiState.analytics
                if (analytics != null) {
                    WorkoutAnalyticsCard(analytics = analytics)
                    PersonalRecordsCard(analytics = analytics)
                } else {
                    AnalyticsEmptyCard()
                }
            }
            
            // Session history section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Sessions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${uiState.filteredSessions.size} sessions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        WorkoutSessionList(
                            sessions = uiState.filteredSessions,
                            onDeleteSession = { session -> sessionToDelete = session },
                            modifier = Modifier.heightIn(max = 400.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, you might show a snackbar or toast
            // For now, we'll just clear the error after showing it
            viewModel.clearError()
        }
    }
    
    // Delete confirmation dialog
    DeleteSessionDialog(
        session = sessionToDelete,
        onConfirm = {
            sessionToDelete?.let { session ->
                viewModel.deleteSession(session.id)
            }
        },
        onDismiss = { sessionToDelete = null }
    )
}

/**
 * Factory function to create WorkoutHistoryScreen with repository
 */
@Composable
fun WorkoutHistoryScreen(
    onNavigateBack: () -> Unit,
    workoutHistoryRepository: WorkoutHistoryRepository
) {
    WorkoutHistoryScreen(
        workoutHistoryRepository = workoutHistoryRepository,
        onNavigateBack = onNavigateBack
    )
}
