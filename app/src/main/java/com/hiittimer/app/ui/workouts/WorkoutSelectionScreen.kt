package com.hiittimer.app.ui.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.hiittimer.app.data.*
import com.hiittimer.app.ui.timer.UnifiedTimerViewModel

/**
 * Screen for selecting and starting complex workouts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSelectionScreen(
    navController: NavController,
    viewModel: UnifiedTimerViewModel
) {
    val workouts by viewModel.complexWorkouts.collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workouts") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Workout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search workouts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            // Category filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }
                items(ExerciseCategory.values().size) { index ->
                    val category = ExerciseCategory.values()[index]
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { 
                            selectedCategory = if (selectedCategory == category) null else category 
                        },
                        label = { Text(categoryToString(category)) }
                    )
                }
            }
            
            // Workout list
            val filteredWorkouts = workouts.filter { workout ->
                (searchQuery.isBlank() || workout.name.contains(searchQuery, ignoreCase = true)) &&
                (selectedCategory == null || workout.getExerciseCategories().contains(selectedCategory))
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredWorkouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onClick = {
                            // Start the workout
                            viewModel.startComplexWorkoutDirect(workout)
                            navController.navigateUp()
                        },
                        onEditClick = {
                            // Navigate to workout editor
                            navController.navigate("workout_editor/${workout.id}")
                        },
                        onDeleteClick = {
                            viewModel.deleteComplexWorkout(workout.id)
                        }
                    )
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateWorkoutDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { workoutName ->
                // Navigate to workout builder
                navController.navigate("workout_builder?name=$workoutName")
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun WorkoutCard(
    workout: ComplexWorkout,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (workout.description.isNotBlank()) {
                        Text(
                            text = workout.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                showMenu = false
                                // TODO: Implement share functionality
                            },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Workout stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WorkoutStatChip(
                    icon = Icons.Default.DateRange,
                    text = "${workout.getTotalDurationMinutes()} min"
                )
                WorkoutStatChip(
                    icon = Icons.Default.List,
                    text = "${workout.phases.size} phases"
                )
                WorkoutStatChip(
                    icon = Icons.Default.Star,
                    text = difficultyToString(workout.difficulty)
                )
            }
            
            // Tags
            if (workout.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    workout.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    if (workout.tags.size > 3) {
                        Text(
                            text = "+${workout.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun CreateWorkoutDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Workout") },
        text = {
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text("Workout Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(workoutName) },
                enabled = workoutName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun categoryToString(category: ExerciseCategory): String {
    return when (category) {
        ExerciseCategory.CARDIO -> "Cardio"
        ExerciseCategory.STRENGTH -> "Strength"
        ExerciseCategory.OLYMPIC_LIFTING -> "Olympic"
        ExerciseCategory.GYMNASTICS -> "Gymnastics"
        ExerciseCategory.MOBILITY -> "Mobility"
        ExerciseCategory.CORE -> "Core"
        ExerciseCategory.PLYOMETRIC -> "Plyometric"
        ExerciseCategory.FLEXIBILITY -> "Flexibility"
        ExerciseCategory.CUSTOM -> "Custom"
    }
}

fun difficultyToString(difficulty: WorkoutDifficulty): String {
    return when (difficulty) {
        WorkoutDifficulty.BEGINNER -> "Beginner"
        WorkoutDifficulty.INTERMEDIATE -> "Intermediate"
        WorkoutDifficulty.ADVANCED -> "Advanced"
        WorkoutDifficulty.ELITE -> "Elite"
    }
}