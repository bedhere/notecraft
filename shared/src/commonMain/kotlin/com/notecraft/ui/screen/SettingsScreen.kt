package com.notecraft.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notecraft.domain.model.AppConfig
import com.notecraft.presentation.settings.SettingsState
import com.notecraft.util.Strings

@Composable
fun SettingsContent(
    state: SettingsState,
    onThemeChange: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onTabIndentChange: (Int) -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = state.config

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(Strings.settings, style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp))

        SettingsSection(Strings.appearance)

        Text(Strings.theme, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val themeOptions = listOf(
                Triple("light", Strings.themeLight, Strings.themeLight),
                Triple("dark", Strings.themeDark, Strings.themeDark),
                Triple("system", Strings.themeSystem, Strings.themeSystem)
            )
            themeOptions.forEach { (value, label, _) ->
                FilterChip(
                    selected = config.theme == value,
                    onClick = { onThemeChange(value) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(Strings.fontSize + ": " + config.fontSize, style = MaterialTheme.typography.labelMedium)
        Slider(
            value = config.fontSize.toFloat(),
            onValueChange = { onFontSizeChange(it.toInt()) },
            valueRange = 10f..32f,
            steps = 21,
            modifier = Modifier.fillMaxWidth()
        )

        // Editor section
        Spacer(Modifier.height(16.dp))
        SettingsSection(Strings.editorSection)

        Text(Strings.tabIndent + ": " + config.tabIndentSize,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Slider(
            value = config.tabIndentSize.toFloat(),
            onValueChange = { onTabIndentChange(it.toInt()) },
            valueRange = 2f..8f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(Strings.autoSave, style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = config.noteAutoSave,
                onCheckedChange = onAutoSaveChange
            )
        }

        Spacer(Modifier.height(16.dp))
        SettingsSection(Strings.aboutSection)
        Text(Strings.appName, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp))
        Text(Strings.version, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
