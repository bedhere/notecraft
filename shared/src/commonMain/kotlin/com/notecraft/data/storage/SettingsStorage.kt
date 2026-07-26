package com.notecraft.data.storage

import com.notecraft.domain.model.AppConfig

interface SettingsStorage {
    suspend fun loadConfig(): AppConfig
    suspend fun saveConfig(config: AppConfig): AppConfig
}
