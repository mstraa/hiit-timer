package com.hiittimer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hiittimer.app.data.Preset
import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.ui.preset.PresetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentConfig: TimerConfig,
    onPresetSelected: (Preset) -> Unit,
    viewModel: PresetViewModel = viewModel()
) {
    val presets by viewModel.presets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var presetToEdit by remember { mutableStateOf<Preset?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }

    if (isOpen) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Workout Presets",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Create new preset button
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Preset")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset list
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (presets.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No presets created yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(presets) { preset ->
                                PresetCard(
                                    preset = preset,
                                    onUsePreset = { 
                                        coroutineScope.launch {
                                            viewModel.markPresetAsUsed(preset)
                                            onPresetSelected(preset)
                                        }
                                    },
                                    onEditPreset = { 
                                        presetToEdit = preset
                                        showEditDialog = true
                                    },
                                    onDeletePreset = { 
                                        presetToDelete = preset
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Create preset dialog
        if (showCreateDialog) {
            CreatePresetDialog(
                currentConfig = currentConfig,
                onDismiss = { showCreateDialog = false },
                onSave = { preset ->
                    coroutineScope.launch {
                        viewModel.savePreset(preset)
                        showCreateDialog = false
                    }
                }
            )
        }

        // Edit preset dialog
        if (showEditDialog && presetToEdit != null) {
            EditPresetDialog(
                preset = presetToEdit!!,
                onDismiss = { 
                    showEditDialog = false
                    presetToEdit = null
                },
                onSave = { updatedPreset ->
                    coroutineScope.launch {
                        viewModel.updatePreset(updatedPreset)
                        showEditDialog = false
                        presetToEdit = null
                    }
                }
            )
        }

        // Delete confirmation dialog
        if (showDeleteDialog && presetToDelete != null) {
            DeletePresetDialog(
                preset = presetToDelete,
                onDismiss = { 
                    showDeleteDialog = false
                    presetToDelete = null
                },
                onConfirm = {
                    coroutineScope.launch {
                        viewModel.deletePreset(presetToDelete!!.id)
                        showDeleteDialog = false
                        presetToDelete = null
                    }
                }
            )
        }
    }
}

@Composable
fun CreatePresetDialog(
    currentConfig: TimerConfig,
    onDismiss: () -> Unit,
    onSave: (Preset) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var workTime by remember { mutableIntStateOf(currentConfig.workTimeSeconds) }
    var restTime by remember { mutableIntStateOf(currentConfig.restTimeSeconds) }
    var rounds by remember { mutableIntStateOf(currentConfig.totalRounds) }
    var isUnlimited by remember { mutableStateOf(currentConfig.isUnlimited) }
    var noRest by remember { mutableStateOf(currentConfig.noRest) }
    var exerciseName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Preset") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Exercise Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        if (it.length <= 200) description = it 
                    },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${description.length}/200") },
                    minLines = 2,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = workTime.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value ->
                                if (value in 5..900) workTime = value
                            }
                        },
                        label = { Text("Work (s)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (!noRest) {
                        OutlinedTextField(
                            value = restTime.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { value ->
                                    if (value in 1..300) restTime = value
                                }
                            },
                            label = { Text("Rest (s)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                if (!isUnlimited) {
                    OutlinedTextField(
                        value = rounds.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value ->
                                if (value in 1..99) rounds = value
                            }
                        },
                        label = { Text("Rounds") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = noRest,
                            onCheckedChange = { noRest = it }
                        )
                        Text("No Rest")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isUnlimited,
                            onCheckedChange = { isUnlimited = it }
                        )
                        Text("Unlimited")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Preset(
                                name = name.trim(),
                                workTimeSeconds = workTime,
                                restTimeSeconds = restTime,
                                totalRounds = rounds,
                                isUnlimited = isUnlimited,
                                noRest = noRest,
                                exerciseName = exerciseName.trim().takeIf { it.isNotEmpty() },
                                description = description.trim().takeIf { it.isNotEmpty() }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
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
fun EditPresetDialog(
    preset: Preset,
    onDismiss: () -> Unit,
    onSave: (Preset) -> Unit
) {
    var name by remember { mutableStateOf(preset.name) }
    var workTime by remember { mutableIntStateOf(preset.workTimeSeconds) }
    var restTime by remember { mutableIntStateOf(preset.restTimeSeconds) }
    var rounds by remember { mutableIntStateOf(preset.totalRounds) }
    var isUnlimited by remember { mutableStateOf(preset.isUnlimited) }
    var noRest by remember { mutableStateOf(preset.noRest) }
    var exerciseName by remember { mutableStateOf(preset.exerciseName ?: "") }
    var description by remember { mutableStateOf(preset.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Preset") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Exercise Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        if (it.length <= 200) description = it 
                    },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${description.length}/200") },
                    minLines = 2,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = workTime.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value ->
                                if (value in 5..900) workTime = value
                            }
                        },
                        label = { Text("Work (s)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (!noRest) {
                        OutlinedTextField(
                            value = restTime.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { value ->
                                    if (value in 1..300) restTime = value
                                }
                            },
                            label = { Text("Rest (s)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                if (!isUnlimited) {
                    OutlinedTextField(
                        value = rounds.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { value ->
                                if (value in 1..99) rounds = value
                            }
                        },
                        label = { Text("Rounds") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = noRest,
                            onCheckedChange = { noRest = it }
                        )
                        Text("No Rest")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isUnlimited,
                            onCheckedChange = { isUnlimited = it }
                        )
                        Text("Unlimited")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            preset.copy(
                                name = name.trim(),
                                workTimeSeconds = workTime,
                                restTimeSeconds = restTime,
                                totalRounds = rounds,
                                isUnlimited = isUnlimited,
                                noRest = noRest,
                                exerciseName = exerciseName.trim().takeIf { it.isNotEmpty() },
                                description = description.trim().takeIf { it.isNotEmpty() }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}