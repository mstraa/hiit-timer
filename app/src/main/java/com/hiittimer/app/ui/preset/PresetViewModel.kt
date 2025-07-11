package com.hiittimer.app.ui.preset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiittimer.app.data.InMemoryPresetRepository
import com.hiittimer.app.data.Preset
import com.hiittimer.app.data.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PresetViewModel(
    private val repository: PresetRepository = InMemoryPresetRepository()
) : ViewModel() {
    
    private val _presets = MutableStateFlow<List<Preset>>(emptyList())
    val presets: StateFlow<List<Preset>> = _presets.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPresets()
    }
    
    private fun loadPresets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _presets.value = repository.getAllPresets()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    suspend fun savePreset(preset: Preset) {
        try {
            repository.savePreset(preset)
            loadPresets()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }
    
    suspend fun updatePreset(preset: Preset) {
        try {
            repository.updatePreset(preset)
            loadPresets()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }
    
    suspend fun deletePreset(id: String) {
        try {
            repository.deletePreset(id)
            loadPresets()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }
    
    suspend fun markPresetAsUsed(preset: Preset) {
        try {
            repository.updatePreset(preset.markAsUsed())
            loadPresets()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}