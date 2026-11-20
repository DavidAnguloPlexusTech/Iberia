package com.plexus.iberia.data.cache

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class SearchCacheTest {

    private val timeSource = TestTimeSource()
    private val cache = SearchCache(timeToLive = 5.minutes, timeSource = timeSource)
    private val searchPage = page("a")

    @Test
    fun `returns a stored page`() {
        cache.put("apollo", 1, searchPage)

        assertEquals(searchPage, cache.get("apollo", 1))
    }

    @Test
    fun `returns nothing for a query that was never stored`() {
        assertNull(cache.get("apollo", 1))
    }

    @Test
    fun `page is still valid just before it expires`() {
        cache.put("apollo", 1, searchPage)
        timeSource += 5.minutes - 1.seconds

        assertEquals(searchPage, cache.get("apollo", 1))
    }

    @Test
    fun `page expires after the time to live`() {
        cache.put("apollo", 1, searchPage)
        timeSource += 5.minutes

        assertNull(cache.get("apollo", 1))
    }

    @Test
    fun `storing again restarts the time to live`() {
        cache.put("apollo", 1, searchPage)
        timeSource += 4.minutes
        cache.put("apollo", 1, searchPage)
        timeSource += 4.minutes

        assertEquals(searchPage, cache.get("apollo", 1))
    }

    @Test
    fun `each page of a query has its own entry`() {
        cache.put("apollo", 1, searchPage)

        assertNull(cache.get("apollo", 2))
    }

    @Test
    fun `queries are compared ignoring case and surrounding spaces`() {
        cache.put(" Apollo ", 1, searchPage)

        assertEquals(searchPage, cache.get("apollo", 1))
    }

    @Test
    fun `drops the least recently used page when full`() {
        val smallCache = SearchCache(timeSource = timeSource, maxEntries = 2)
        smallCache.put("a", 1, page("a"))
        smallCache.put("b", 1, page("b"))
        smallCache.get("a", 1)
        smallCache.put("c", 1, page("c"))

        assertNull(smallCache.get("b", 1))
        assertEquals(page("a"), smallCache.get("a", 1))
        assertEquals(page("c"), smallCache.get("c", 1))
    }

    @Test
    fun `finds an image of a valid page`() {
        cache.put("apollo", 1, searchPage)

        assertEquals(image("a"), cache.findImage("a"))
    }

    @Test
    fun `does not find images of expired pages`() {
        cache.put("apollo", 1, searchPage)
        timeSource += 5.minutes

        assertNull(cache.findImage("a"))
    }

    private fun page(id: String) = SearchPage(images = listOf(image(id)), hasNextPage = true)

    private fun image(id: String) = NasaImage(
        id = id, title = "Title $id", description = "", thumbnailUrl = null, imageUrl = null,
        dateCreated = "", center = "", keywords = emptyList(), photographer = null,
    )
}
