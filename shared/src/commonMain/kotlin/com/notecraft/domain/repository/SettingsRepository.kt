package com.notecraft.domain.repository

import com.notecraft.domain.model.AppConfig

interface SettingsRepository {
    suspend fun getConfig(): AppConfig
    suspend fun saveConfig(config: AppConfig): AppConfig
}
