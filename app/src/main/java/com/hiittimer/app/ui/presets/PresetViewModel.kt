package com.hiittimer.app.ui.presets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiittimer.app.data.InMemoryPresetRepository
import com.hiittimer.app.data.Preset
import com.hiittimer.app.data.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for preset management (FR-008)
 */
data class PresetUiState(
    val presets: List<Preset> = emptyList(),
    val recentPresets: List<Preset> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Preset> = emptyList(),
    val isSearching: Boolean = false
)

/**
 * ViewModel for managing workout presets (FR-008: Preset System)
 */
class PresetViewModel(
    private val presetRepository: PresetRepository = InMemoryPresetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PresetUiState())
    val uiState: StateFlow<PresetUiState> = _uiState.asStateFlow()

    init {
        loadPresets()
        loadRecentPresets()
    }

    /**
     * Load all presets from repository
     */
    fun loadPresets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val presets = presetRepository.getAllPresets()
                _uiState.value = _uiState.value.copy(
                    presets = presets,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load presets: ${e.message}"
                )
            }
        }
    }

    /**
     * Load recent presets for quick access
     */
    private fun loadRecentPresets() {
        viewModelScope.launch {
            try {
                val recentPresets = presetRepository.getRecentPresets(5)
                _uiState.value = _uiState.value.copy(recentPresets = recentPresets)
            } catch (e: Exception) {
                // Don't show error for recent presets failure
            }
        }
    }

    /**
     * Save a new preset (FR-008: Create new presets)
     */
    fun savePreset(preset: Preset) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                presetRepository.savePreset(preset)
                loadPresets()
                loadRecentPresets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = when {
                        e.message?.contains("Maximum 50 presets") == true -> 
                            "Cannot save preset: Maximum 50 presets allowed"
                        else -> "Failed to save preset: ${e.message}"
                    }
                )
            }
        }
    }

    /**
     * Update an existing preset (FR-008: Edit existing presets)
     */
    fun updatePreset(preset: Preset) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                presetRepository.updatePreset(preset)
                loadPresets()
                loadRecentPresets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update preset: ${e.message}"
                )
            }
        }
    }

    /**
     * Delete a preset (FR-008: Delete presets with confirmation)
     */
    fun deletePreset(presetId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                presetRepository.deletePreset(presetId)
                loadPresets()
                loadRecentPresets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete preset: ${e.message}"
                )
            }
        }
    }

    /**
     * Mark preset as used and update recent presets
     */
    fun usePreset(preset: Preset) {
        viewModelScope.launch {
            try {
                val updatedPreset = preset.markAsUsed()
                presetRepository.updatePreset(updatedPreset)
                loadRecentPresets()
            } catch (e: Exception) {
                // Don't show error for usage tracking failure
            }
        }
    }

    /**
     * Search presets by name, exercise, or description
     */
    fun searchPresets(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val results = presetRepository.searchPresets(query)
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isSearching = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear search results
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Get preset by ID
     */
    suspend fun getPresetById(id: String): Preset? {
        return try {
            presetRepository.getPresetById(id)
        } catch (e: Exception) {
            null
        }
    }
}
