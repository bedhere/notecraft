package com.notecraft.util

import kotlin.random.Random

object IdGenerator {
    fun newId(): String {
        val chars = "0123456789abcdef"
        val segments = intArrayOf(8, 4, 4, 4, 12)
        return segments.joinToString("-") { len ->
            (1..len).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        }
    }
}
