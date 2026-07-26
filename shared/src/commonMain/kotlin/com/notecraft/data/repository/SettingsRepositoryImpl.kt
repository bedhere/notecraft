package com.notecraft.data.repository

import com.notecraft.domain.model.AppConfig
import com.notecraft.domain.repository.SettingsRepository
import com.notecraft.data.storage.SettingsStorage

class SettingsRepositoryImpl(
    private val storage: SettingsStorage
) : SettingsRepository {

    override suspend fun getConfig(): AppConfig =
        storage.loadConfig()

    override suspend fun saveConfig(config: AppConfig): AppConfig =
        storage.saveConfig(config)
}
