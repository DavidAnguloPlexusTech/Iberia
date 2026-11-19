package com.plexus.iberia.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleMapperTest {

    @Test
    fun `restores a cut title from the description`() {
        val title = "One of NASA Glenn's T-34C research aircraft is removed from t..."
        val description = "One of NASA Glenn's T-34C research aircraft is removed from the hangar prior to a flight"

        assertEquals(description, completeTitle(title, description))
    }

    @Test
    fun `keeps only the first sentence of the description`() {
        val title = "The newly renovated NASA Glenn Research Center, GRC Lewis Fie..."
        val description = "The newly renovated NASA Glenn Research Center, GRC Lewis Field West Gate at dusk.  More text here."

        assertEquals(
            "The newly renovated NASA Glenn Research Center, GRC Lewis Field West Gate at dusk",
            completeTitle(title, description),
        )
    }

    @Test
    fun `understands the ellipsis character`() {
        assertEquals("Apollo 11 lands on the Moon", completeTitle("Apollo 11 lands on…", "Apollo 11 lands on the Moon. Crew safe."))
    }

    @Test
    fun `keeps the title when the description does not continue it`() {
        assertEquals("Mars rover...", completeTitle("Mars rover...", "A different text."))
    }

    @Test
    fun `keeps titles that are not cut`() {
        assertEquals("Apollo 11", completeTitle("Apollo 11", "Apollo 11 lands on the Moon."))
    }
}
