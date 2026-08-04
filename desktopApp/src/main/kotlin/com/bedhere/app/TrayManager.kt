package com.bedhere.app

import com.notecraft.util.Strings
import com.notecraft.data.repository.SettingsRepositoryImpl
import com.notecraft.storage.JvmSettingsStorage
import java.awt.*
import java.awt.image.BufferedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TrayManager(
    private val dataDir: String,
    private val onShowMain: () -> Unit,
    private val onQuit: () -> Unit
) {
    private var trayIcon: TrayIcon? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init() {
        if (!SystemTray.isSupported()) return
        removeExisting()
        val icon = createIcon()
        val popup = createPopup()
        trayIcon = TrayIcon(icon, Strings.appName, popup).apply {
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
        ioScope.cancel()
    }

    private fun removeExisting() {
        try {
            val tray = SystemTray.getSystemTray()
            for (icon in tray.trayIcons) {
                if (icon.toolTip == Strings.appName) tray.remove(icon)
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
        val menu = PopupMenu()

        val showItem = MenuItem(Strings.trayShow)
        showItem.addActionListener { onShowMain() }
        menu.add(showItem)

        val trayItem = CheckboxMenuItem(Strings.trayCloseToTray)
        trayItem.state = false
        ioScope.launch {
            val closeToTray = try { settingsRepo.getConfig().closeToTray } catch (_: Exception) { false }
            EventQueue.invokeLater { trayItem.state = closeToTray }
        }
        trayItem.addItemListener {
            val enabled = trayItem.state
            ioScope.launch {
                try {
                    val c = settingsRepo.getConfig()
                    settingsRepo.saveConfig(c.copy(closeToTray = enabled))
                } catch (_: Exception) {
                    // A tray toggle should not block or crash the AWT event thread.
                }
            }
        }
        menu.add(trayItem)

        menu.addSeparator()

        val quitItem = MenuItem(Strings.trayQuit)
        quitItem.addActionListener { onQuit() }
        menu.add(quitItem)

        return menu
    }
}
