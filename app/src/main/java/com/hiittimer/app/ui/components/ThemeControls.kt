package com.hiittimer.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hiittimer.app.data.ThemePreference

/**
 * Theme control components for FR-014: Manual theme override
 * Provides system/light/dark theme selection
 */
@Composable
fun ThemeControlsSection(
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Theme Settings",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Theme selection radio buttons
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOption(
                text = "System Default",
                icon = Icons.Default.Settings,
                selected = currentTheme == ThemePreference.SYSTEM,
                onClick = { onThemeChange(ThemePreference.SYSTEM) }
            )
            
            ThemeOption(
                text = "Light Theme",
                icon = Icons.Default.Settings,
                selected = currentTheme == ThemePreference.LIGHT,
                onClick = { onThemeChange(ThemePreference.LIGHT) }
            )

            ThemeOption(
                text = "Dark Theme",
                icon = Icons.Default.Settings,
                selected = currentTheme == ThemePreference.DARK,
                onClick = { onThemeChange(ThemePreference.DARK) }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null // handled by selectable modifier
        )
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (selected) 1f else 0.7f
            )
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (selected) 1f else 0.7f
            )
        )
    }
}

/**
 * Theme settings card for configuration screen
 */
@Composable
fun ThemeSettingsCard(
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        ThemeControlsSection(
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Compact theme toggle button for quick switching
 */
@Composable
fun ThemeToggleButton(
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val nextTheme = when (currentTheme) {
        ThemePreference.SYSTEM -> ThemePreference.LIGHT
        ThemePreference.LIGHT -> ThemePreference.DARK
        ThemePreference.DARK -> ThemePreference.SYSTEM
    }
    
    val icon = Icons.Default.Settings
    
    IconButton(
        onClick = { onThemeChange(nextTheme) },
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Switch theme",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
