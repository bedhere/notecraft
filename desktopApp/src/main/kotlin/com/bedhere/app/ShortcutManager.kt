package com.bedhere.app

import com.notecraft.domain.model.AppConfig
import java.awt.event.KeyEvent

data class ShortcutBinding(
    val keyCode: Int,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false
)

class ShortcutManager(
    private val onQuickNote: () -> Unit,
    private val onToggleVisibility: () -> Unit
) {

    private val ctrlMask = KeyEvent.CTRL_DOWN_MASK
    private val altMask = KeyEvent.ALT_DOWN_MASK
    private val shiftMask = KeyEvent.SHIFT_DOWN_MASK
    private var quickNoteBinding: ShortcutBinding? = null
    private var visibilityBinding: ShortcutBinding? = null

    fun registerFromConfig(config: AppConfig) {
        quickNoteBinding = parseShortcut(config.globalShortcut)
        visibilityBinding = parseShortcut(config.toggleVisibilityShortcut)
    }

    fun unregisterAll() {
        quickNoteBinding = null
        visibilityBinding = null
    }

    fun handleKeyEvent(keyCode: Int, modifiers: Int): Boolean {
        val ctrl = modifiers and ctrlMask != 0
        val alt = modifiers and altMask != 0
        val shift = modifiers and shiftMask != 0
        val qn = quickNoteBinding
        if (qn != null && qn.keyCode == keyCode && qn.ctrl == ctrl && qn.alt == alt && qn.shift == shift) {
            onQuickNote(); return true
        }
        val tv = visibilityBinding
        if (tv != null && tv.keyCode == keyCode && tv.ctrl == ctrl && tv.alt == alt && tv.shift == shift) {
            onToggleVisibility(); return true
        }
        return false
    }

    fun checkConflict(config: AppConfig): String? {
        if (config.globalShortcut.isBlank() || config.toggleVisibilityShortcut.isBlank()) return null
        if (config.globalShortcut == config.toggleVisibilityShortcut)
            return "Quick note and visibility shortcuts must differ"
        return null
    }

    private fun parseShortcut(shortcut: String): ShortcutBinding? {
        if (shortcut.isBlank()) return null
        val parts = shortcut.split("+").map { it.trim() }
        var ctrl = false; var alt = false; var shift = false; var key = ""
        for (part in parts) {
            when (part.lowercase()) {
                "ctrl", "control" -> ctrl = true
                "alt", "option" -> alt = true
                "shift" -> shift = true
                "meta", "command", "cmd" -> {}
                else -> key = part
            }
        }
        if (key.isBlank()) return null
        val keyCode = keyNameToCode(key) ?: return null
        return ShortcutBinding(keyCode, ctrl, alt, shift)
    }

    private fun keyNameToCode(name: String): Int? {
        val u = name.uppercase()
        if (u.length == 1) {
            val c = u[0]
            if (c >= 'A' && c <= 'Z') return KeyEvent.getExtendedKeyCodeForChar(c.code)
            if (c >= '0' && c <= '9') return KeyEvent.getExtendedKeyCodeForChar(c.code)
        }
        return when (u) {
            "SPACE" -> KeyEvent.VK_SPACE
            "ENTER" -> KeyEvent.VK_ENTER
            "TAB" -> KeyEvent.VK_TAB
            "ESCAPE", "ESC" -> KeyEvent.VK_ESCAPE
            "BACKSPACE" -> KeyEvent.VK_BACK_SPACE
            "DELETE" -> KeyEvent.VK_DELETE
            "HOME" -> KeyEvent.VK_HOME
            "END" -> KeyEvent.VK_END
            "PAGEUP" -> KeyEvent.VK_PAGE_UP
            "PAGEDOWN" -> KeyEvent.VK_PAGE_DOWN
            "INSERT" -> KeyEvent.VK_INSERT
            "UP", "ARROWUP" -> KeyEvent.VK_UP
            "DOWN", "ARROWDOWN" -> KeyEvent.VK_DOWN
            "LEFT", "ARROWLEFT" -> KeyEvent.VK_LEFT
            "RIGHT", "ARROWRIGHT" -> KeyEvent.VK_RIGHT
            "F1" -> KeyEvent.VK_F1; "F2" -> KeyEvent.VK_F2
            "F3" -> KeyEvent.VK_F3; "F4" -> KeyEvent.VK_F4
            "F5" -> KeyEvent.VK_F5; "F6" -> KeyEvent.VK_F6
            "F7" -> KeyEvent.VK_F7; "F8" -> KeyEvent.VK_F8
            "F9" -> KeyEvent.VK_F9; "F10" -> KeyEvent.VK_F10
            "F11" -> KeyEvent.VK_F11; "F12" -> KeyEvent.VK_F12
            "COMMA" -> KeyEvent.VK_COMMA; "PERIOD" -> KeyEvent.VK_PERIOD
            "MINUS" -> KeyEvent.VK_MINUS; "EQUALS" -> KeyEvent.VK_EQUALS
            "SLASH" -> KeyEvent.VK_SLASH; "BACKSLASH" -> KeyEvent.VK_BACK_SLASH
            "SEMICOLON" -> KeyEvent.VK_SEMICOLON; "QUOTE" -> KeyEvent.VK_QUOTE
            "BRACKETLEFT" -> KeyEvent.VK_OPEN_BRACKET
            "BRACKETRIGHT" -> KeyEvent.VK_CLOSE_BRACKET
            else -> null
        }
    }
}
