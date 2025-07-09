package com.hiittimer.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager as SystemAudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages audio playback for HIIT timer as specified in FR-006, FR-007, and FR-025
 * Provides distinct sounds for work/rest intervals and countdown beeps
 * Enhanced with media audio stream integration (FR-025: Media Audio Output)
 */
class AudioManager(private val context: Context) {
    private val systemAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as SystemAudioManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Audio settings state
    private val _audioSettings = MutableStateFlow(AudioSettings())
    val audioSettings: StateFlow<AudioSettings> = _audioSettings.asStateFlow()
    
    // Audio focus management
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    
    // Tone generators for different sounds
    private var toneGenerator: ToneGenerator? = null
    
    init {
        initializeToneGenerator()
    }
    
    private fun initializeToneGenerator() {
        try {
            // FR-025: Use STREAM_MUSIC instead of STREAM_NOTIFICATION for media audio output
            toneGenerator = ToneGenerator(
                SystemAudioManager.STREAM_MUSIC,
                (_audioSettings.value.volume * 100).toInt()
            )
        } catch (e: RuntimeException) {
            // ToneGenerator creation failed, audio will be disabled
            toneGenerator = null
        }
    }
    
    /**
     * Play work interval start sound (energetic tone)
     */
    fun playWorkIntervalSound() {
        if (!_audioSettings.value.isEnabled) return
        
        requestAudioFocus {
            playTone(ToneGenerator.TONE_PROP_BEEP, 500) // Energetic beep
        }
    }
    
    /**
     * Play rest interval start sound (calming tone)
     */
    fun playRestIntervalSound() {
        if (!_audioSettings.value.isEnabled) return
        
        requestAudioFocus {
            playTone(ToneGenerator.TONE_PROP_BEEP2, 300) // Calmer beep
        }
    }
    
    /**
     * Play countdown beep (3 seconds before interval change)
     */
    fun playCountdownBeep() {
        if (!_audioSettings.value.isEnabled) return
        
        requestAudioFocus {
            playTone(ToneGenerator.TONE_PROP_PROMPT, 200) // Short prompt beep
        }
    }
    
    /**
     * Play completion sound when workout finishes
     */
    fun playCompletionSound() {
        if (!_audioSettings.value.isEnabled) return
        
        requestAudioFocus {
            // Play a sequence of ascending tones for completion
            scope.launch {
                playTone(ToneGenerator.TONE_PROP_BEEP, 200)
                delay(100)
                playTone(ToneGenerator.TONE_PROP_BEEP, 200)
                delay(100)
                playTone(ToneGenerator.TONE_PROP_BEEP, 400)
            }
        }
    }
    
    private fun playTone(toneType: Int, durationMs: Int) {
        toneGenerator?.let { generator ->
            try {
                generator.startTone(toneType, durationMs)
            } catch (e: RuntimeException) {
                // Tone playback failed, reinitialize generator
                initializeToneGenerator()
            }
        }
    }
    
    private fun requestAudioFocus(onFocusGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAudioFocusApi26(onFocusGranted)
        } else {
            @Suppress("DEPRECATION")
            val result = systemAudioManager.requestAudioFocus(
                { focusChange -> handleAudioFocusChange(focusChange) },
                SystemAudioManager.STREAM_MUSIC, // FR-025: Use STREAM_MUSIC for media audio output
                SystemAudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )

            if (result == SystemAudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                hasAudioFocus = true
                onFocusGranted()
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusApi26(onFocusGranted: () -> Unit) {
        // FR-025: Use media audio attributes for proper media stream integration
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA) // Changed from USAGE_NOTIFICATION
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // Changed from CONTENT_TYPE_SONIFICATION
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(SystemAudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener { focusChange -> handleAudioFocusChange(focusChange) }
            .build()

        audioFocusRequest?.let { request ->
            val result = systemAudioManager.requestAudioFocus(request)
            if (result == SystemAudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                hasAudioFocus = true
                onFocusGranted()
            }
        }
    }
    
    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            SystemAudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
            }
            SystemAudioManager.AUDIOFOCUS_LOSS,
            SystemAudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                hasAudioFocus = false
                abandonAudioFocus()
            }
        }
    }
    
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                systemAudioManager.abandonAudioFocusRequest(request)
            }
        } else {
            @Suppress("DEPRECATION")
            systemAudioManager.abandonAudioFocus { }
        }
        hasAudioFocus = false
    }
    
    /**
     * Update audio settings (FR-007: Audio Controls)
     */
    fun updateSettings(settings: AudioSettings) {
        _audioSettings.value = settings
        
        // Reinitialize tone generator with new volume
        toneGenerator?.release()
        initializeToneGenerator()
    }
    
    /**
     * Toggle mute/unmute
     */
    fun toggleMute() {
        val currentSettings = _audioSettings.value
        updateSettings(currentSettings.copy(isEnabled = !currentSettings.isEnabled))
    }
    
    /**
     * Set volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        val currentSettings = _audioSettings.value
        updateSettings(currentSettings.copy(volume = clampedVolume))
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        toneGenerator?.release()
        toneGenerator = null
        abandonAudioFocus()
    }
}

/**
 * Data class for audio settings (FR-007: Audio preference persistence)
 */
data class AudioSettings(
    val isEnabled: Boolean = true,
    val volume: Float = 0.7f // 70% volume by default
)
