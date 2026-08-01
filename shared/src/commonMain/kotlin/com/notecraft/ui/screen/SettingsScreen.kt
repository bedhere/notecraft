package com.notecraft.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notecraft.presentation.settings.SettingsState
import com.notecraft.util.Strings
import com.notecraft.ui.theme.AppComponentDefaults
import com.notecraft.ui.theme.AppShapes
import com.notecraft.ui.theme.AppSpacing

@Composable
fun SettingsContent(
    state: SettingsState,
    onThemeChange: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onTabIndentChange: (Int) -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    onCloseToTrayChange: ((Boolean) -> Unit)? = null,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val config = state.config

    Column(
        modifier = modifier
            .padding(AppSpacing.xl)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "应用设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.isSaving) {
                    Text(
                        text = Strings.saveSaving,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(
                onClick = onClose,
                contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 0.dp)
            ) {
                Text("×", style = MaterialTheme.typography.titleMedium)
            }
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = AppSpacing.md)
            )
        }

        SettingsSection(Strings.appearance)

        Text(Strings.theme, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = AppSpacing.md, bottom = AppSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
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

        Spacer(Modifier.height(AppSpacing.lg))
        Text(Strings.fontSize + ": " + config.fontSize, style = MaterialTheme.typography.labelMedium)
        Slider(
            value = config.fontSize.toFloat(),
            onValueChange = { onFontSizeChange(it.toInt()) },
            valueRange = 10f..32f,
            steps = 21,
            modifier = Modifier.fillMaxWidth()
        )

        // Editor section
        Spacer(Modifier.height(AppSpacing.xl))
        SettingsSection(Strings.editorSection)

        Text(Strings.tabIndent + ": " + config.tabIndentSize,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = AppSpacing.md, bottom = AppSpacing.sm))
        Slider(
            value = config.tabIndentSize.toFloat(),
            onValueChange = { onTabIndentChange(it.toInt()) },
            valueRange = 2f..8f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(AppSpacing.md))
        SettingsSwitchRow(
            text = Strings.autoSave,
            checked = config.noteAutoSave,
            onCheckedChange = onAutoSaveChange
        )

        onCloseToTrayChange?.let { updateCloseToTray ->
            Spacer(Modifier.height(AppSpacing.xl))
            SettingsSection(Strings.desktopSection)
            SettingsSwitchRow(
                text = Strings.trayCloseToTray,
                checked = config.closeToTray,
                onCheckedChange = updateCloseToTray
            )
        }

        Spacer(Modifier.height(AppSpacing.xl))
        SettingsSection(Strings.aboutSection)
        Text(
            text = Strings.appDisplayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = AppSpacing.sm)
        )
        Text(
            text = Strings.aboutDescription,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.sm)
        )
        Text(
            text = Strings.version,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.sm)
        )

        Spacer(Modifier.height(AppSpacing.xxxl))
    }
}

@Composable
private fun SettingsSwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = AppShapes.control,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = AppSpacing.xs,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = AppSpacing.sm)
    )
}
