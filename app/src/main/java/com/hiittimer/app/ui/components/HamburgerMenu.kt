package com.hiittimer.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hiittimer.app.audio.AudioSettings
import com.hiittimer.app.data.ThemePreference

/**
 * Hamburger menu button for FR-016: Navigation and Settings UI
 */
@Composable
fun HamburgerMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Open settings menu",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Sliding settings panel that opens from the left (FR-016)
 * Contains audio settings, theme settings, and app preferences
 */
@Composable
fun HamburgerMenuPanel(
    isOpen: Boolean,
    onClose: () -> Unit,
    audioSettings: AudioSettings,
    themePreference: ThemePreference,
    onToggleAudio: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    // Overlay background when panel is open
    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onClose() }
                .zIndex(10f)
        )
    }

    // Sliding panel with 250ms animation duration (FR-016)
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(250)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(250)
        ),
        modifier = modifier.zIndex(11f)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Audio settings section
                AudioSettingsCard(
                    audioSettings = audioSettings,
                    onToggleAudio = onToggleAudio,
                    onVolumeChange = onVolumeChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Theme settings section
                ThemeSettingsCard(
                    currentTheme = themePreference,
                    onThemeChange = onThemeChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // App info section
                AppInfoSection()
            }
        }
    }
}

/**
 * App information section for the settings panel
 */
@Composable
private fun AppInfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "HIIT Timer v1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = "A minimalist HIIT and EMOM timer for focused workouts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
