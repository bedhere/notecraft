package com.notecraft.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StringsTest {
    @Test
    fun `brand name is stable and separated from note count`() {
        assertEquals("\u7B3A\u9020", Strings.appBrandName)
        assertEquals("\u7B3A\u9020 \u00B7 Notecraft", Strings.appDisplayName)

        listOf(0, 1, 4).forEach { count ->
            assertEquals("$count \u7BC7\u7B14\u8BB0", Strings.notesCount(count))
            assertFalse(Strings.appBrandName.contains(count.toString()))
            assertFalse(Strings.appDisplayName.contains("($count)"))
        }
    }

    @Test
    fun `about and tray strings use stable desktop brand`() {
        assertEquals(Strings.appDisplayName, Strings.appName)
        assertEquals("\u663E\u793A\u7B3A\u9020 \u00B7 Notecraft", Strings.trayShow)
        assertFalse(Strings.trayShow.contains("\u7B14\u8BB0\u5DE5\u574A"))
        assertFalse(Strings.aboutDescription.contains("\u82B1\u7B3A"))
    }
}
