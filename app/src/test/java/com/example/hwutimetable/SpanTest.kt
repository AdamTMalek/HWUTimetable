package com.example.hwutimetable

import org.joda.time.Period
import org.junit.Assert.*
import org.junit.experimental.theories.*
import org.junit.runner.RunWith

@RunWith(Theories::class)
class SpanTest {
    class PositiveMinutes(
        // Minutes define minutes using which, period objects will be made
        val minutes : Array<Int> = arrayOf(7, 15, 30, 45, 60, 120),
        val expectedSpan: Array<Int> = arrayOf(0, 1, 2, 3, 4, 8)
    )

    companion object {
        @JvmField
        @DataPoint
        val positiveMinutes = PositiveMinutes()
    }

    @Theory
    fun correctSpanFromPeriodMinutes(positiveMinutes: PositiveMinutes) {
        positiveMinutes.minutes.forEachIndexed { i, minutes ->
            val period = Period(0, minutes, 0, 0)
            assertEquals(positiveMinutes.expectedSpan[i], Span.getSpanFromPeriod(period))
        }
    }
}