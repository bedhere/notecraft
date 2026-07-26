package com.bedhere.app

import com.notecraft.data.repository.SettingsRepositoryImpl
import com.notecraft.storage.JvmSettingsStorage
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import kotlinx.coroutines.runBlocking

class TrayManager(
    private val dataDir: String,
    private val onShowMain: () -> Unit,
    private val onQuickNote: () -> Unit,
    private val onQuit: () -> Unit
) {
    private var trayIcon: TrayIcon? = null

    fun init() {
        if (!SystemTray.isSupported()) return
        removeExisting()
        val icon = createIcon()
        val popup = createPopup()
        trayIcon = TrayIcon(icon, "Notecraft", popup).apply {
            isImageAutoSize = true
            addActionListener { onShowMain() }
        }
        try { SystemTray.getSystemTray().add(trayIcon) }
        catch (e: Exception) { e.printStackTrace() }
    }

    fun dispose() {
        trayIcon?.let {
            try { SystemTray.getSystemTray().remove(it) } catch (_: Exception) {}
            trayIcon = null
        }
    }

    private fun removeExisting() {
        try {
            val tray = SystemTray.getSystemTray()
            for (icon in tray.trayIcons) {
                if (icon.toolTip == "Notecraft") tray.remove(icon)
            }
        } catch (_: Exception) {}
    }

    private fun createIcon(): BufferedImage {
        val img = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        try {
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
            g.color = java.awt.Color(91, 140, 90)
            g.fillRoundRect(4, 2, 24, 28, 6, 6)
            g.color = java.awt.Color.WHITE
            g.fillRoundRect(7, 6, 18, 3, 2, 2)
            g.fillRoundRect(7, 12, 18, 2, 2, 2)
            g.fillRoundRect(7, 17, 12, 2, 2, 2)
        } finally { g.dispose() }
        return img
    }

    private fun createPopup(): PopupMenu {
        val settingsRepo = SettingsRepositoryImpl(JvmSettingsStorage(dataDir))
        val config = try { runBlocking { settingsRepo.getConfig() } } catch (_: Exception) { null }
        val closeToTray = config?.closeToTray ?: false

        val menu = PopupMenu()

        val showItem = MenuItem("Show Notecraft")
        showItem.addActionListener { onShowMain() }
        menu.add(showItem)

        val noteItem = MenuItem("Quick Note")
        noteItem.addActionListener { onQuickNote() }
        menu.add(noteItem)

        menu.addSeparator()

        val trayItem = CheckboxMenuItem("Close to Tray")
        trayItem.state = closeToTray
        trayItem.addItemListener {
            runBlocking {
                val c = settingsRepo.getConfig()
                settingsRepo.saveConfig(c.copy(closeToTray = trayItem.state))
            }
        }
        menu.add(trayItem)

        menu.addSeparator()

        val quitItem = MenuItem("Quit")
        quitItem.addActionListener { onQuit() }
        menu.add(quitItem)

        return menu
    }
}
