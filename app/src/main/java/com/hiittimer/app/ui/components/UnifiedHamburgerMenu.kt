package com.hiittimer.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hiittimer.app.ui.timer.UnifiedTimerViewModel

/**
 * Hamburger menu panel that works with UnifiedTimerViewModel
 */
@Composable
fun HamburgerMenuPanel(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    viewModel: UnifiedTimerViewModel,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val audioSettings by viewModel.audioSettings.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    
    HamburgerMenuPanel(
        isOpen = isOpen,
        onClose = onDismiss,
        audioSettings = audioSettings,
        themePreference = themePreference,
        onToggleAudio = {
            val newSettings = audioSettings.copy(
                isEnabled = !audioSettings.isEnabled
            )
            // Note: updateAudioSettings method needs to be added to UnifiedTimerViewModel
        },
        onVolumeChange = { volume ->
            val newSettings = audioSettings.copy(
                volume = volume
            )
            // Note: updateAudioSettings method needs to be added to UnifiedTimerViewModel
        },
        onThemeChange = { theme ->
            // Note: updateThemePreference method needs to be added to UnifiedTimerViewModel
        },
        onNavigateToHistory = onNavigateToHistory,
        modifier = modifier
    )
}