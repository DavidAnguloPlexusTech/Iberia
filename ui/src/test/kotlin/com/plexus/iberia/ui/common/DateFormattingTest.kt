package com.plexus.iberia.ui.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DateFormattingTest {

    @Test
    fun `display date keeps only the day`() {
        assertEquals("2015-07-29", "2015-07-29T00:00:00Z".toDisplayDate())
    }

    @Test
    fun `accessible date is written in words`() {
        assertEquals("July 29, 2015", "2015-07-29T00:00:00Z".toAccessibleDate())
    }

    @Test
    fun `accessible date falls back to the displayed date when it cannot be parsed`() {
        assertEquals("unknown", "unknown".toAccessibleDate())
    }
}
