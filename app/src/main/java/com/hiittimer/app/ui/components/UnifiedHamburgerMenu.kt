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
            viewModel.toggleAudio()
        },
        onVolumeChange = { volume ->
            viewModel.setAudioVolume(volume)
        },
        onThemeChange = { theme ->
            viewModel.setThemePreference(theme)
        },
        onNavigateToHistory = onNavigateToHistory,
        modifier = modifier
    )
}