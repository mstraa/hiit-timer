package com.hiittimer.app.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hiittimer.app.R
import com.hiittimer.app.ui.components.AudioSettingsCard
import com.hiittimer.app.ui.components.ThemeSettingsCard
import com.hiittimer.app.ui.timer.TimerViewModel
import com.hiittimer.app.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: TimerViewModel,
    onNavigateBack: () -> Unit
) {
    val timerStatus by viewModel.timerStatus.collectAsState()
    val audioSettings by viewModel.audioSettings.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    val config = timerStatus.config

    // Responsive design values (FR-015: Responsive Design)
    val adaptivePadding = getAdaptivePadding()
    val adaptiveSpacing = getAdaptiveSpacing()
    val adaptiveButtonHeight = getAdaptiveButtonHeight()
    val scrollState = rememberScrollState()

    var workTime by remember { mutableStateOf(config.workTimeSeconds.toString()) }
    var restTime by remember { mutableStateOf(config.restTimeSeconds.toString()) }
    var rounds by remember { mutableStateOf(config.totalRounds.toString()) }
    var isUnlimited by remember { mutableStateOf(config.isUnlimited) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(adaptivePadding)
            .verticalScroll(scrollState)
    ) {
        // Header with adaptive sizing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = adaptiveSpacing * 2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(MinTouchTargetSize)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Timer Configuration",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = adaptiveSpacing)
            )
        }
        
        // Configuration form with adaptive spacing
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(adaptiveSpacing)
        ) {
            // Work time input
            OutlinedTextField(
                value = workTime,
                onValueChange = { workTime = it },
                label = { Text(stringResource(R.string.work_time)) },
                suffix = { Text(stringResource(R.string.seconds)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = workTime.toIntOrNull()?.let { it < 5 || it > 900 } ?: true
            )
            
            // Rest time input
            OutlinedTextField(
                value = restTime,
                onValueChange = { restTime = it },
                label = { Text(stringResource(R.string.rest_time)) },
                suffix = { Text(stringResource(R.string.seconds)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = restTime.toIntOrNull()?.let { it < 5 || it > 300 } ?: true
            )
            
            // Rounds configuration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = rounds,
                    onValueChange = { rounds = it },
                    label = { Text(stringResource(R.string.rounds)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = !isUnlimited,
                    isError = !isUnlimited && (rounds.toIntOrNull()?.let { it < 1 || it > 99 } ?: true)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isUnlimited,
                        onCheckedChange = { isUnlimited = it }
                    )
                    Text(
                        text = stringResource(R.string.unlimited),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Audio settings (FR-007: Audio Controls)
            AudioSettingsCard(
                audioSettings = audioSettings,
                onToggleAudio = { viewModel.toggleAudio() },
                onVolumeChange = { volume -> viewModel.setAudioVolume(volume) }
            )

            Spacer(modifier = Modifier.height(adaptiveSpacing))

            // Theme settings (FR-014: Manual theme override)
            ThemeSettingsCard(
                currentTheme = themePreference,
                onThemeChange = { preference -> viewModel.setThemePreference(preference) }
            )

            Spacer(modifier = Modifier.height(adaptiveSpacing * 2))

            // Save button with adaptive height (FR-015: Minimum 48dp touch targets)
            Button(
                onClick = {
                    val workTimeInt = workTime.toIntOrNull() ?: config.workTimeSeconds
                    val restTimeInt = restTime.toIntOrNull() ?: config.restTimeSeconds
                    val roundsInt = rounds.toIntOrNull() ?: config.totalRounds

                    viewModel.updateConfig(
                        workTimeSeconds = workTimeInt,
                        restTimeSeconds = restTimeInt,
                        totalRounds = roundsInt,
                        isUnlimited = isUnlimited
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = adaptiveButtonHeight),
                enabled = isValidConfiguration(workTime, restTime, rounds, isUnlimited)
            ) {
                Text(
                    text = "Save Configuration",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun isValidConfiguration(
    workTime: String,
    restTime: String,
    rounds: String,
    isUnlimited: Boolean
): Boolean {
    val workTimeInt = workTime.toIntOrNull()
    val restTimeInt = restTime.toIntOrNull()
    val roundsInt = rounds.toIntOrNull()
    
    return workTimeInt != null && workTimeInt in 5..900 &&
            restTimeInt != null && restTimeInt in 5..300 &&
            (isUnlimited || (roundsInt != null && roundsInt in 1..99))
}
