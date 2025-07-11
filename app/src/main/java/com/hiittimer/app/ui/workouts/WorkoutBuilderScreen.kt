package com.hiittimer.app.ui.workouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hiittimer.app.data.*
import com.hiittimer.app.ui.timer.UnifiedTimerViewModel
import java.util.UUID

/**
 * Screen for building custom complex workouts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutBuilderScreen(
    navController: NavController,
    viewModel: UnifiedTimerViewModel,
    initialWorkoutName: String = "New Workout",
    existingWorkout: ComplexWorkout? = null
) {
    var workoutName by remember { mutableStateOf(existingWorkout?.name ?: initialWorkoutName) }
    var workoutDescription by remember { mutableStateOf(existingWorkout?.description ?: "") }
    var difficulty by remember { mutableStateOf(existingWorkout?.difficulty ?: WorkoutDifficulty.INTERMEDIATE) }
    var phases by remember { mutableStateOf(existingWorkout?.phases ?: emptyList()) }
    var showAddPhaseDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingWorkout != null) "Edit Workout" else "Create Workout") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Validate and save workout
                            val workout = ComplexWorkout(
                                id = existingWorkout?.id ?: UUID.randomUUID().toString(),
                                name = workoutName,
                                description = workoutDescription,
                                phases = phases,
                                difficulty = difficulty
                            )
                            
                            val validation = workout.validate()
                            if (validation.isValid) {
                                viewModel.saveComplexWorkout(workout)
                                navController.navigateUp()
                            } else {
                                validationError = validation.errorMessage ?: "Invalid workout"
                                showValidationDialog = true
                            }
                        },
                        enabled = workoutName.isNotBlank() && phases.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workout details
            item {
                WorkoutDetailsCard(
                    name = workoutName,
                    onNameChange = { workoutName = it },
                    description = workoutDescription,
                    onDescriptionChange = { workoutDescription = it },
                    difficulty = difficulty,
                    onDifficultyChange = { difficulty = it }
                )
            }
            
            // Phases
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Phases",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    FilledTonalButton(
                        onClick = { showAddPhaseDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Phase")
                    }
                }
            }
            
            if (phases.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "No phases added yet. Add phases to build your workout.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(phases, key = { _, phase -> phase.id }) { index, phase ->
                    PhaseBuilderCard(
                        phase = phase,
                        phaseNumber = index + 1,
                        onUpdate = { updatedPhase ->
                            phases = phases.toMutableList().apply {
                                set(index, updatedPhase)
                            }
                        },
                        onDelete = {
                            phases = phases.filterNot { it.id == phase.id }
                        },
                        onMoveUp = if (index > 0) {
                            {
                                phases = phases.toMutableList().apply {
                                    val temp = this[index]
                                    this[index] = this[index - 1]
                                    this[index - 1] = temp
                                }
                            }
                        } else null,
                        onMoveDown = if (index < phases.size - 1) {
                            {
                                phases = phases.toMutableList().apply {
                                    val temp = this[index]
                                    this[index] = this[index + 1]
                                    this[index + 1] = temp
                                }
                            }
                        } else null
                    )
                }
            }
            
            // Workout summary
            if (phases.isNotEmpty()) {
                item {
                    WorkoutSummaryCard(phases = phases)
                }
            }
        }
    }
    
    if (showAddPhaseDialog) {
        AddPhaseDialog(
            onDismiss = { showAddPhaseDialog = false },
            onAdd = { phase ->
                phases = phases + phase
                showAddPhaseDialog = false
            }
        )
    }
    
    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = { showValidationDialog = false },
            title = { Text("Invalid Workout") },
            text = { Text(validationError) },
            confirmButton = {
                TextButton(onClick = { showValidationDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun WorkoutDetailsCard(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    difficulty: WorkoutDifficulty,
    onDifficultyChange: (WorkoutDifficulty) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Workout Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            
            Column {
                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkoutDifficulty.values().forEach { level ->
                        @OptIn(ExperimentalMaterial3Api::class)
                        FilterChip(
                            selected = difficulty == level,
                            onClick = { onDifficultyChange(level) },
                            label = { Text(difficultyToString(level)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhaseBuilderCard(
    phase: WorkoutPhase,
    phaseNumber: Int,
    onUpdate: (WorkoutPhase) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Phase header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Phase $phaseNumber: ${phase.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${phase.exercises.size} exercises • ${formatDuration(phase.getTotalDuration())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    onMoveUp?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                        }
                    }
                    onMoveDown?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete phase")
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Phase settings
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = phase.rounds.toString(),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { rounds ->
                                    if (rounds > 0) {
                                        onUpdate(phase.copy(rounds = rounds))
                                    }
                                }
                            },
                            label = { Text("Rounds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (phase.rounds > 1) {
                            OutlinedTextField(
                                value = phase.restBetweenRounds.toString(),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { rest ->
                                        if (rest >= 0) {
                                            onUpdate(phase.copy(restBetweenRounds = rest))
                                        }
                                    }
                                },
                                label = { Text("Rest (s)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Exercises
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    phase.exercises.forEachIndexed { index, exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onUpdate = { updatedExercise ->
                                val updatedExercises = phase.exercises.toMutableList()
                                updatedExercises[index] = updatedExercise
                                onUpdate(phase.copy(exercises = updatedExercises))
                            },
                            onDelete = {
                                onUpdate(phase.copy(
                                    exercises = phase.exercises.filterNot { it.id == exercise.id }
                                ))
                            }
                        )
                    }
                    
                    FilledTonalButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Exercise")
                    }
                }
            }
        }
    }
    
    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onAdd = { exercise ->
                onUpdate(phase.copy(exercises = phase.exercises + exercise))
                showAddExerciseDialog = false
            }
        )
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onUpdate: (Exercise) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = exercise.getDisplayText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun WorkoutSummaryCard(phases: List<WorkoutPhase>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Workout Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = phases.size.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Phases",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = phases.sumOf { it.exercises.size }.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalSeconds = phases.sumOf { it.getTotalDuration() }
                    Text(
                        text = "${(totalSeconds + 59) / 60}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Minutes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun AddPhaseDialog(
    onDismiss: () -> Unit,
    onAdd: (WorkoutPhase) -> Unit
) {
    var phaseName by remember { mutableStateOf("") }
    var phaseType by remember { mutableStateOf(PhaseType.CUSTOM) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Phase") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = phaseName,
                    onValueChange = { phaseName = it },
                    label = { Text("Phase Name") },
                    singleLine = true
                )
                
                Column {
                    Text(
                        text = "Phase Type",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    PhaseType.values().forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { phaseType = type }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = phaseType == type,
                                onClick = { phaseType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(phaseTypeToString(type))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (phaseName.isNotBlank()) {
                        onAdd(WorkoutPhase(
                            name = phaseName,
                            type = phaseType,
                            exercises = emptyList()
                        ))
                    }
                },
                enabled = phaseName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onAdd: (Exercise) -> Unit
) {
    var exerciseName by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(WorkoutMode.TIME_BASED) }
    var duration by remember { mutableStateOf("30") }
    var reps by remember { mutableStateOf("10") }
    var sets by remember { mutableStateOf("3") }
    var rest by remember { mutableStateOf("10") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Exercise Name") },
                    singleLine = true
                )
                
                // Exercise mode selection
                Text("Exercise Mode", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkoutMode.values().take(3).forEach { workoutMode ->
                        @OptIn(ExperimentalMaterial3Api::class)
                        FilterChip(
                            selected = mode == workoutMode,
                            onClick = { mode = workoutMode },
                            label = {
                                Text(
                                    when (workoutMode) {
                                        WorkoutMode.TIME_BASED -> "Time"
                                        WorkoutMode.REP_BASED -> "Reps"
                                        WorkoutMode.AMRAP -> "AMRAP"
                                        else -> ""
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Mode-specific inputs
                when (mode) {
                    WorkoutMode.TIME_BASED -> {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (seconds)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    WorkoutMode.REP_BASED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = reps,
                                onValueChange = { reps = it },
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sets,
                                onValueChange = { sets = it },
                                label = { Text("Sets") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    WorkoutMode.AMRAP -> {
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Target Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    else -> {}
                }
                
                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text("Rest (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (exerciseName.isNotBlank()) {
                        val exercise = when (mode) {
                            WorkoutMode.TIME_BASED -> Exercise(
                                name = exerciseName,
                                mode = mode,
                                durationSeconds = duration.toIntOrNull() ?: 30,
                                restSeconds = rest.toIntOrNull() ?: 10
                            )
                            WorkoutMode.REP_BASED -> Exercise(
                                name = exerciseName,
                                mode = mode,
                                targetReps = reps.toIntOrNull() ?: 10,
                                sets = sets.toIntOrNull() ?: 3,
                                restSeconds = rest.toIntOrNull() ?: 10
                            )
                            WorkoutMode.AMRAP -> Exercise(
                                name = exerciseName,
                                mode = mode,
                                targetReps = reps.toIntOrNull() ?: 10,
                                restSeconds = rest.toIntOrNull() ?: 0
                            )
                            else -> Exercise(name = exerciseName)
                        }
                        onAdd(exercise)
                    }
                },
                enabled = exerciseName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

