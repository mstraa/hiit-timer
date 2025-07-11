package com.hiittimer.app.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hiittimer.app.data.TimerConfig
import com.hiittimer.app.ui.timer.UnifiedTimerViewModel

/**
 * Configuration screen that works with UnifiedTimerViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: UnifiedTimerViewModel,
    onNavigateBack: () -> Unit
) {
    val timerStatus by viewModel.timerStatus.collectAsState()
    var config by remember { mutableStateOf(timerStatus.config) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Work Duration Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Work Duration",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    DurationSlider(
                        value = config.workTimeSeconds,
                        onValueChange = { 
                            config = config.copy(workTimeSeconds = it)
                            viewModel.updateConfig(workTimeSeconds = it)
                        },
                        range = 5..300,
                        steps = 58
                    )
                }
            }

            // Rest Duration Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Rest Duration",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    DurationSlider(
                        value = config.restTimeSeconds,
                        onValueChange = { 
                            config = config.copy(restTimeSeconds = it)
                            viewModel.updateConfig(restTimeSeconds = it)
                        },
                        range = 0..120,
                        steps = 23
                    )
                }
            }

            // Rounds Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Rounds",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = config.totalRounds.toString(),
                            style = MaterialTheme.typography.displaySmall
                        )
                        
                        Slider(
                            value = config.totalRounds.toFloat(),
                            onValueChange = { 
                                config = config.copy(totalRounds = it.toInt())
                                viewModel.updateConfig(totalRounds = it.toInt())
                            },
                            valueRange = 1f..50f,
                            steps = 48,
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        )
                    }
                }
            }

            // Quick Presets
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                config = TimerConfig(
                                    workTimeSeconds = 20,
                                    restTimeSeconds = 10,
                                    totalRounds = 8
                                )
                                viewModel.updateConfig(
                                    workTimeSeconds = 20,
                                    restTimeSeconds = 10,
                                    totalRounds = 8
                                )
                            }
                        ) {
                            Text("Tabata")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                config = TimerConfig(
                                    workTimeSeconds = 45,
                                    restTimeSeconds = 15,
                                    totalRounds = 12
                                )
                                viewModel.updateConfig(
                                    workTimeSeconds = 45,
                                    restTimeSeconds = 15,
                                    totalRounds = 12
                                )
                            }
                        ) {
                            Text("Classic")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                config = TimerConfig(
                                    workTimeSeconds = 40,
                                    restTimeSeconds = 20,
                                    totalRounds = 10
                                )
                                viewModel.updateConfig(
                                    workTimeSeconds = 40,
                                    restTimeSeconds = 20,
                                    totalRounds = 10
                                )
                            }
                        ) {
                            Text("Moderate")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    steps: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(value),
                style = MaterialTheme.typography.displaySmall
            )
            
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = steps,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    return if (seconds == 0) {
        "None"
    } else if (seconds < 60) {
        "$seconds sec"
    } else {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        if (remainingSeconds == 0) {
            "$minutes min"
        } else {
            "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
        }
    }
}