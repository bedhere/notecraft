package com.notecraft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val locale: String = "zh-CN",
    val theme: String = "system",
    val fontSize: Int = 14,
    val tabIndentSize: Int = 2,
    val defaultViewMode: String = "split",
    val noteAutoSave: Boolean = true,
    val closeToTray: Boolean = false,
    val globalShortcut: String = "Ctrl+Space",
    val toggleVisibilityShortcut: String = ""
)
