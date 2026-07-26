package com.notecraft.presentation.settings

import com.notecraft.domain.model.AppConfig

data class SettingsState(
    val config: AppConfig = AppConfig(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val isOpen: Boolean = false
)
