package com.notecraft.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notecraft.domain.model.AppConfig
import com.notecraft.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadConfig()
    }

    fun loadConfig() {
        viewModelScope.launch {
            try {
                val config = settingsRepository.getConfig()
                _state.value = _state.value.copy(config = config)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateTheme(theme: String) {
        val updated = _state.value.config.copy(theme = theme)
        _state.value = _state.value.copy(config = updated)
        save(updated)
    }

    fun updateFontSize(size: Int) {
        val clamped = size.coerceIn(10, 32)
        val updated = _state.value.config.copy(fontSize = clamped)
        _state.value = _state.value.copy(config = updated)
        save(updated)
    }

    fun updateTabIndentSize(size: Int) {
        val clamped = size.coerceIn(2, 8)
        val updated = _state.value.config.copy(tabIndentSize = clamped)
        _state.value = _state.value.copy(config = updated)
        save(updated)
    }

    fun updateNoteAutoSave(enabled: Boolean) {
        val updated = _state.value.config.copy(noteAutoSave = enabled)
        _state.value = _state.value.copy(config = updated)
        save(updated)
    }

    fun updateCloseToTray(enabled: Boolean) {
        val updated = _state.value.config.copy(closeToTray = enabled)
        _state.value = _state.value.copy(config = updated)
        save(updated)
    }

    fun updateLocale(locale: String) {
        val updated = _state.value.config.copy(locale = locale)
        _state.value = _state.value.copy(config = updated)
        save(updated)
    }

    fun toggleOpen() {
        _state.value = _state.value.copy(isOpen = !_state.value.isOpen)
        if (_state.value.isOpen) loadConfig()
    }

    fun close() {
        _state.value = _state.value.copy(isOpen = false)
    }

    private fun save(config: AppConfig) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                val saved = settingsRepository.saveConfig(config)
                _state.value = _state.value.copy(config = saved, isSaving = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
