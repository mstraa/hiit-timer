package com.hiittimer.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.hiittimer.app.data.TimerConfig
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
 * Contains work time, rest time, rounds, and preset management
 */
@Composable
fun TimerConfigModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    config: TimerConfig,
    onConfigUpdate: (TimerConfig) -> Unit,
    modifier: Modifier = Modifier
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
                        onClose = onClose
                    )
                }
            }
        }
    }
}

/**
 * Content of the timer configuration modal
 */
@Composable
private fun TimerConfigContent(
    config: TimerConfig,
    onConfigUpdate: (TimerConfig) -> Unit,
    onClose: () -> Unit
) {
    var workTime by remember { mutableStateOf(config.workTimeSeconds.toString()) }
    var restTime by remember { mutableStateOf(config.restTimeSeconds.toString()) }
    var rounds by remember { mutableStateOf(config.totalRounds.toString()) }
    var isUnlimited by remember { mutableStateOf(config.isUnlimited) }
    var noRest by remember { mutableStateOf(config.noRest) }

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
                .padding(bottom = adaptiveSpacing * 2),
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

        // Work time input
        OutlinedTextField(
            value = workTime,
            onValueChange = { workTime = it },
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
                onCheckedChange = { noRest = it }
            )
        }

        Spacer(modifier = Modifier.height(adaptiveSpacing))

        // Rest time input (only if rest is enabled)
        if (!noRest) {
            OutlinedTextField(
                value = restTime,
                onValueChange = { restTime = it },
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
                onCheckedChange = { isUnlimited = it }
            )
        }

        Spacer(modifier = Modifier.height(adaptiveSpacing))

        // Rounds input (only if not unlimited)
        if (!isUnlimited) {
            OutlinedTextField(
                value = rounds,
                onValueChange = { rounds = it },
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
