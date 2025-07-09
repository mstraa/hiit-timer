package com.hiittimer.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hiittimer.app.data.Preset
import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.ui.components.PresetList
import com.hiittimer.app.ui.components.RecentPresetsSection
import com.hiittimer.app.ui.presets.PresetUiState
import com.hiittimer.app.ui.presets.PresetViewModel
import com.hiittimer.app.ui.utils.*

/**
 * Cog/gear icon button for timer configuration (FR-016)
 */
@Composable
fun TimerConfigButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Timer configuration",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Validate timer configuration input
 */
private fun isValidConfiguration(
    workTime: String,
    restTime: String,
    rounds: String,
    isUnlimited: Boolean,
    noRest: Boolean
): Boolean {
    val workTimeInt = workTime.toIntOrNull()
    val restTimeInt = restTime.toIntOrNull()
    val roundsInt = rounds.toIntOrNull()

    return workTimeInt != null && workTimeInt in 5..900 &&
            (noRest || (restTimeInt != null && restTimeInt in 5..300)) &&
            (isUnlimited || (roundsInt != null && roundsInt in 1..99))
}

/**
 * Timer configuration modal with semi-transparent background (FR-016)
 * Contains work time, rest time, rounds, and preset management (FR-008)
 */
@Composable
fun TimerConfigModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    config: TimerConfig,
    onConfigUpdate: (TimerConfig) -> Unit,
    modifier: Modifier = Modifier,
    presetViewModel: PresetViewModel = viewModel()
) {
    // Modal with fade animation (300ms duration as per FR-016)
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            // Semi-transparent background overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                // Modal content
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f)
                        .clickable(enabled = false) { }, // Prevent clicks from passing through
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    TimerConfigContent(
                        config = config,
                        onConfigUpdate = onConfigUpdate,
                        onClose = onClose,
                        presetViewModel = presetViewModel
                    )
                }
            }
        }
    }
}

/**
 * Content of the timer configuration modal with preset management (FR-008)
 */
@Composable
private fun TimerConfigContent(
    config: TimerConfig,
    onConfigUpdate: (TimerConfig) -> Unit,
    onClose: () -> Unit,
    presetViewModel: PresetViewModel
) {
    var workTime by remember { mutableStateOf(config.workTimeSeconds.toString()) }
    var restTime by remember { mutableStateOf(config.restTimeSeconds.toString()) }
    var rounds by remember { mutableStateOf(config.totalRounds.toString()) }
    var isUnlimited by remember { mutableStateOf(config.isUnlimited) }
    var noRest by remember { mutableStateOf(config.noRest) }

    // Tab state for configuration vs presets (FR-008)
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Configuration", "Presets")

    // Preset management state
    val presetUiState by presetViewModel.uiState.collectAsState()
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }

    val scrollState = rememberScrollState()
    val adaptivePadding = getAdaptivePadding()
    val adaptiveSpacing = getAdaptiveSpacing()
    val adaptiveButtonHeight = getAdaptiveButtonHeight()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(adaptivePadding)
            .verticalScroll(scrollState)
    ) {
        // Header with close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = adaptiveSpacing),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timer Configuration",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close configuration",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Tab row for Configuration vs Presets (FR-008)
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(adaptiveSpacing))

        // Tab content switching (FR-043: Preset Tab Accessibility)
        when (selectedTab) {
            0 -> {
                // Configuration Tab Content
                ConfigurationTabContent(
                    workTime = workTime,
                    onWorkTimeChange = { workTime = it },
                    restTime = restTime,
                    onRestTimeChange = { restTime = it },
                    rounds = rounds,
                    onRoundsChange = { rounds = it },
                    isUnlimited = isUnlimited,
                    onUnlimitedChange = { isUnlimited = it },
                    noRest = noRest,
                    onNoRestChange = { noRest = it },
                    adaptiveSpacing = adaptiveSpacing,
                    adaptiveButtonHeight = adaptiveButtonHeight,
                    config = config,
                    onConfigUpdate = onConfigUpdate,
                    onClose = onClose
                )
            }
            1 -> {
                // Presets Tab Content
                PresetsTabContent(
                    presetUiState = presetUiState,
                    presetViewModel = presetViewModel,
                    onPresetSelect = { preset ->
                        val presetConfig = preset.toTimerConfig()
                        onConfigUpdate(presetConfig)
                        onClose()
                    },
                    presetToDelete = presetToDelete,
                    onPresetToDeleteChange = { presetToDelete = it },
                    adaptiveSpacing = adaptiveSpacing,
                    adaptiveButtonHeight = adaptiveButtonHeight
                )
            }
        }
    }
}

/**
 * Configuration tab content component (FR-043)
 */
@Composable
private fun ConfigurationTabContent(
    workTime: String,
    onWorkTimeChange: (String) -> Unit,
    restTime: String,
    onRestTimeChange: (String) -> Unit,
    rounds: String,
    onRoundsChange: (String) -> Unit,
    isUnlimited: Boolean,
    onUnlimitedChange: (Boolean) -> Unit,
    noRest: Boolean,
    onNoRestChange: (Boolean) -> Unit,
    adaptiveSpacing: Dp,
    adaptiveButtonHeight: Dp,
    config: TimerConfig,
    onConfigUpdate: (TimerConfig) -> Unit,
    onClose: () -> Unit
) {
    Column {
        // Work time input
        OutlinedTextField(
            value = workTime,
            onValueChange = onWorkTimeChange,
            label = { Text("Work Time (seconds)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("5-900 seconds") }
        )

        Spacer(modifier = Modifier.height(adaptiveSpacing))

        // "No Rest" toggle switch (FR-001: "No Rest" toggle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "No Rest Mode",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Skip rest intervals completely",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Switch(
                checked = noRest,
                onCheckedChange = onNoRestChange
            )
        }

        Spacer(modifier = Modifier.height(adaptiveSpacing))

        // Rest time input (only if rest is enabled)
        if (!noRest) {
            OutlinedTextField(
                value = restTime,
                onValueChange = onRestTimeChange,
                label = { Text("Rest Time (seconds)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("5-300 seconds") }
            )

            Spacer(modifier = Modifier.height(adaptiveSpacing))
        }

        // Unlimited rounds toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Unlimited Rounds",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = isUnlimited,
                onCheckedChange = onUnlimitedChange
            )
        }

        Spacer(modifier = Modifier.height(adaptiveSpacing))

        // Rounds input (only if not unlimited)
        if (!isUnlimited) {
            OutlinedTextField(
                value = rounds,
                onValueChange = onRoundsChange,
                label = { Text("Number of Rounds") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("1-99 rounds") }
            )

            Spacer(modifier = Modifier.height(adaptiveSpacing * 2))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save button
        Button(
            onClick = {
                val workTimeInt = workTime.toIntOrNull() ?: config.workTimeSeconds
                val restTimeInt = restTime.toIntOrNull() ?: config.restTimeSeconds
                val roundsInt = rounds.toIntOrNull() ?: config.totalRounds

                try {
                    val newConfig = TimerConfig(
                        workTimeSeconds = workTimeInt,
                        restTimeSeconds = restTimeInt,
                        totalRounds = roundsInt,
                        isUnlimited = isUnlimited,
                        noRest = noRest
                    )
                    onConfigUpdate(newConfig)
                    onClose()
                } catch (e: IllegalArgumentException) {
                    // Handle validation errors - in a real app, show error message
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = adaptiveButtonHeight),
            enabled = isValidConfiguration(workTime, restTime, rounds, isUnlimited, noRest)
        ) {
            Text(
                text = "Save Configuration",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Presets tab content component (FR-043: Preset Tab Accessibility)
 */
@Composable
private fun PresetsTabContent(
    presetUiState: PresetUiState,
    presetViewModel: PresetViewModel,
    onPresetSelect: (Preset) -> Unit,
    presetToDelete: Preset?,
    onPresetToDeleteChange: (Preset?) -> Unit,
    adaptiveSpacing: Dp,
    adaptiveButtonHeight: Dp
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (presetUiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (presetUiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(adaptiveSpacing))
                    Text(
                        text = "Error loading presets",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = presetUiState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            if (presetUiState.presets.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(adaptiveSpacing))
                        Text(
                            text = "No presets available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Create custom configurations to save as presets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Presets list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(adaptiveSpacing)
                ) {
                    items(presetUiState.presets) { preset ->
                        PresetCard(
                            preset = preset,
                            onSelect = { onPresetSelect(preset) },
                            onDelete = { onPresetToDeleteChange(preset) },
                            adaptiveSpacing = adaptiveSpacing
                        )
                    }
                }
            }
        }

        // Delete confirmation dialog
        presetToDelete?.let { preset ->
            AlertDialog(
                onDismissRequest = { onPresetToDeleteChange(null) },
                title = { Text("Delete Preset") },
                text = { Text("Are you sure you want to delete '${preset.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            presetViewModel.deletePreset(preset.id)
                            onPresetToDeleteChange(null)
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onPresetToDeleteChange(null) }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Individual preset card component (FR-043)
 */
@Composable
private fun PresetCard(
    preset: Preset,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    adaptiveSpacing: Dp
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveSpacing),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                preset.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = "${preset.workTimeSeconds}s work • ${preset.restTimeSeconds}s rest • ${preset.totalRounds} rounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete preset",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
