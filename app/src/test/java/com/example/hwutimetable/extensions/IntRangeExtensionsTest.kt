package com.example.hwutimetable.extensions

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class IntRangeExtensionsTest {
    @Test
    fun testRange() {
        val expected = intArrayOf(-2, -1, 0, 1, 2, 3)
        val actual = (-2..3).toIntArray()

        assertArrayEquals(expected, actual)
    }

    @Test
    fun testInvalidRange() {
        val expected = IntArray(0)
        @Suppress("EmptyRange") val actual = (5..1).toIntArray()

        assertArrayEquals(expected, actual)
    }
}