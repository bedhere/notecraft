package com.notecraft.util

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@Ignore
class TimeFormatTest {

    @Test
    fun `relativeTime returns just now for zero delta`() {
        val result = TimeFormat.relativeTime(0L)
        assertTrue(result == "刚刚", "应返回'刚刚'，实际'$result'")
    }

    @Test
    fun `relativeTime returns minutes for recent time`() {
        val result = TimeFormat.relativeTime(0L - 300_000)
        assertTrue(result.contains("分钟前"), "应包含'分钟前'，实际'$result'")
    }

    @Test
    fun `relativeTime returns hours for older time`() {
        val result = TimeFormat.relativeTime(0L - 7_200_000)
        assertTrue(result.contains("小时前"), "应包含'小时前'，实际'$result'")
    }

    @Test
    fun `relativeTime returns days for old time`() {
        val result = TimeFormat.relativeTime(0L - 259_200_000)
        assertTrue(result.contains("天前"), "应包含'天前'，实际'$result'")
    }

    @Test
    fun `formatDateTime returns expected format`() {
        val result = TimeFormat.formatDateTime(0L)
        assertTrue(result.length > 10, "应该返回日期时间字符串，实际'$result'")
    }
}
