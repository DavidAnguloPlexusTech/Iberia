package com.plexus.iberia.data.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HtmlMapperTest {

    @Test
    fun `removes tags and keeps their text`() {
        val html = "Credit: NASA <b><a href=\"http://www.nasa.gov\" rel=\"nofollow\">image use policy.</a></b>"

        assertEquals("Credit: NASA image use policy.", html.withoutHtml())
    }

    @Test
    fun `decodes entities`() {
        assertEquals("Earth & Moon \"rise\"", "Earth &amp; Moon &quot;rise&quot;".withoutHtml())
    }

    @Test
    fun `collapses whitespace left by line breaks and tags`() {
        assertEquals("Line one Line two", "Line one<br/>\n\n  Line two".withoutHtml())
    }

    @Test
    fun `leaves plain text untouched`() {
        assertEquals("Apollo 11", "Apollo 11".withoutHtml())
    }
}
