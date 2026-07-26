package com.notecraft.data.storage

import com.notecraft.domain.model.AppConfig

class InMemorySettingsStorage : SettingsStorage {
    private var config = AppConfig()

    override suspend fun loadConfig(): AppConfig = config

    override suspend fun saveConfig(config: AppConfig): AppConfig {
        this.config = config
        return config
    }
}
