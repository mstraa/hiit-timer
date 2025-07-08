package com.hiittimer.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hiittimer.app.audio.AudioSettings

/**
 * Audio control components for FR-007: Audio Controls
 * Provides mute/unmute toggle and volume adjustment
 */
@Composable
fun AudioControlsSection(
    audioSettings: AudioSettings,
    onToggleAudio: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Audio Settings",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Mute/Unmute toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = if (audioSettings.isEnabled) "Audio enabled" else "Audio disabled",
                    tint = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (audioSettings.isEnabled) 1f else 0.5f
                    )
                )
                Text(
                    text = "Audio Cues",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Switch(
                checked = audioSettings.isEnabled,
                onCheckedChange = { onToggleAudio() }
            )
        }
        
        // Volume slider (only shown when audio is enabled)
        if (audioSettings.isEnabled) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Volume",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(audioSettings.volume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Slider(
                    value = audioSettings.volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Compact audio toggle button for the main timer screen
 */
@Composable
fun AudioToggleButton(
    audioSettings: AudioSettings,
    onToggleAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggleAudio,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = if (audioSettings.isEnabled) "Mute audio" else "Unmute audio",
            tint = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (audioSettings.isEnabled) 1f else 0.5f
            )
        )
    }
}

/**
 * Audio settings card for configuration screen
 */
@Composable
fun AudioSettingsCard(
    audioSettings: AudioSettings,
    onToggleAudio: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        AudioControlsSection(
            audioSettings = audioSettings,
            onToggleAudio = onToggleAudio,
            onVolumeChange = onVolumeChange,
            modifier = Modifier.padding(16.dp)
        )
    }
}
