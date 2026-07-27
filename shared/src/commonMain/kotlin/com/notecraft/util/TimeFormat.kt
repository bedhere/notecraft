package com.notecraft.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object TimeFormat {
    fun relativeTime(epochMs: Long): String {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val duration = now - instant
        val seconds = duration.inWholeSeconds
        val minutes = duration.inWholeMinutes
        val hours = duration.inWholeHours
        val days = duration.inWholeDays

        return when {
            seconds < 60 -> "刚刚"
            minutes < 60 -> "${minutes} 分钟前"
            hours < 24 -> "${hours} 小时前"
            days < 7 -> "${days} 天前"
            days < 365 -> {
                val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${dt.monthNumber}月${dt.dayOfMonth}日"
            }
            else -> {
                val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${dt.year}年${dt.monthNumber}月${dt.dayOfMonth}日"
            }
        }
    }

    fun formatDateTime(epochMs: Long): String {
        val dt = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}-${dt.dayOfMonth.toString().padStart(2, '0')} " +
            "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    }
}
