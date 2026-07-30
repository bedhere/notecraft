package com.notecraft.util

import com.notecraft.platform.currentTimeMillis
import kotlin.math.floor

object TimeFormat {
    fun relativeTime(epochMs: Long): String {
        val nowMs = currentTimeMillis()
        val diffMs = nowMs - epochMs
        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "刚刚"
            minutes < 60 -> "${minutes} 分钟前"
            hours < 24 -> "${hours} 小时前"
            days < 7 -> "${days} 天前"
            days < 365 -> {
                val (month, day) = epochToMonthDay(epochMs)
                "${month}月${day}日"
            }
            else -> {
                val (year, month, day) = epochToDate(epochMs)
                "${year}年${month}月${day}日"
            }
        }
    }

    fun formatDateTime(epochMs: Long): String {
        val (year, month, day, hour, minute) = epochToDateTime(epochMs)
        return "${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} " +
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    fun formatMonthDay(epochMs: Long): String {
        val (_, month, day, _, _) = epochToDateTime(epochMs)
        return "${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    // Simple epoch-to-date conversion (no timezone support — uses UTC)
    private fun epochToDate(ms: Long): Triple<Int, Int, Int> {
        val (y, m, d, _, _) = epochToDateTime(ms)
        return Triple(y, m, d)
    }

    private fun epochToMonthDay(ms: Long): Pair<Int, Int> {
        val (_, m, d, _, _) = epochToDateTime(ms)
        return Pair(m, d)
    }

    private fun epochToDateTime(ms: Long): FiveTuple {
        var days = ms / 86_400_000L
        val timeRemainder = ms % 86_400_000L
        val hour = (timeRemainder / 3_600_000L).toInt()
        val minute = ((timeRemainder % 3_600_000L) / 60_000L).toInt()

        // Days since 1970-01-01 to year/month/day
        var y = 1970L
        while (true) {
            val daysInYear = if (isLeapYear(y)) 366 else 365
            if (days < daysInYear) break
            days -= daysInYear
            y++
        }

        val leap = isLeapYear(y)
        val monthDays = monthDaysArray(leap)
        var m = 0
        while (m < 12 && days >= monthDays[m]) {
            days -= monthDays[m]
            m++
        }
        m++ // 1-based month

        return FiveTuple(y.toInt(), m, (days + 1).toInt(), hour, minute)
    }

    private fun isLeapYear(y: Long): Boolean =
        (y % 4 == 0L && y % 100 != 0L) || (y % 400 == 0L)

    private fun monthDaysArray(leap: Boolean): IntArray =
        if (leap) intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        else intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    private data class FiveTuple(val a: Int, val b: Int, val c: Int, val d: Int, val e: Int)
}
