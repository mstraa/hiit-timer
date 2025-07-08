package com.hiittimer.app.data

import android.content.Context
import android.content.SharedPreferences
import com.hiittimer.app.audio.AudioSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Theme preference options for FR-014: Manual theme override
 */
enum class ThemePreference {
    SYSTEM,  // Follow system theme
    LIGHT,   // Always light theme
    DARK     // Always dark theme
}

/**
 * Manages user preferences and settings persistence
 * Implements FR-007: Audio preference persistence and FR-014: Theme support
 */
class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    // Audio settings state
    private val _audioSettings = MutableStateFlow(loadAudioSettings())
    val audioSettings: StateFlow<AudioSettings> = _audioSettings.asStateFlow()

    // Theme settings state (FR-014: Manual theme override)
    private val _themePreference = MutableStateFlow(loadThemePreference())
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()
    
    /**
     * Load audio settings from SharedPreferences
     */
    private fun loadAudioSettings(): AudioSettings {
        return AudioSettings(
            isEnabled = sharedPreferences.getBoolean(KEY_AUDIO_ENABLED, true),
            volume = sharedPreferences.getFloat(KEY_AUDIO_VOLUME, 0.7f)
        )
    }

    /**
     * Load theme preference from SharedPreferences (FR-014: Manual theme override)
     */
    private fun loadThemePreference(): ThemePreference {
        val themeString = sharedPreferences.getString(KEY_THEME_PREFERENCE, ThemePreference.SYSTEM.name)
        return try {
            ThemePreference.valueOf(themeString ?: ThemePreference.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemePreference.SYSTEM
        }
    }
    
    /**
     * Save audio settings to SharedPreferences
     */
    fun saveAudioSettings(settings: AudioSettings) {
        sharedPreferences.edit()
            .putBoolean(KEY_AUDIO_ENABLED, settings.isEnabled)
            .putFloat(KEY_AUDIO_VOLUME, settings.volume)
            .apply()
        
        _audioSettings.value = settings
    }
    
    /**
     * Toggle audio enabled/disabled
     */
    fun toggleAudioEnabled() {
        val currentSettings = _audioSettings.value
        val newSettings = currentSettings.copy(isEnabled = !currentSettings.isEnabled)
        saveAudioSettings(newSettings)
    }
    
    /**
     * Set audio volume (0.0 to 1.0)
     */
    fun setAudioVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        val currentSettings = _audioSettings.value
        val newSettings = currentSettings.copy(volume = clampedVolume)
        saveAudioSettings(newSettings)
    }

    /**
     * Save theme preference (FR-014: Manual theme override)
     */
    fun setThemePreference(preference: ThemePreference) {
        sharedPreferences.edit()
            .putString(KEY_THEME_PREFERENCE, preference.name)
            .apply()

        _themePreference.value = preference
    }

    companion object {
        private const val PREFS_NAME = "hiit_timer_prefs"
        private const val KEY_AUDIO_ENABLED = "audio_enabled"
        private const val KEY_AUDIO_VOLUME = "audio_volume"
        private const val KEY_THEME_PREFERENCE = "theme_preference"
    }
}
