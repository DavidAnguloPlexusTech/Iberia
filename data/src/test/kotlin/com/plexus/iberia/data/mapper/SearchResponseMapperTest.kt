package com.plexus.iberia.data.mapper

import com.google.gson.Gson
import com.plexus.iberia.data.readResource
import com.plexus.iberia.data.remote.dto.CollectionDto
import com.plexus.iberia.data.remote.dto.SearchResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResponseMapperTest {

    private val response = Gson().fromJson(readResource("search_response.json"), SearchResponseDto::class.java)

    @Test
    fun `skips items without nasa id`() {
        val page = response.toSearchPage()

        assertEquals(listOf("GRC-2015-C-04751", "NO-TITLE-1"), page.images.map { it.id })
    }

    @Test
    fun `maps the item fields`() {
        val image = response.toSearchPage().images.first()

        assertEquals("Learjet 25 Research Aircraft", image.title)
        assertEquals("GRC", image.center)
        assertEquals(listOf("learjet", "aircraft"), image.keywords)
    }

    @Test
    fun `uses an empty title when it is missing`() {
        val image = response.toSearchPage().images.last()

        assertEquals("", image.title)
    }

    @Test
    fun `uses the preview link as thumbnail and the widest alternate as image`() {
        val image = response.toSearchPage().images.first()

        assertEquals("https://images.nasa/thumb.jpg", image.thumbnailUrl)
        assertEquals("https://images.nasa/large.jpg", image.imageUrl)
    }

    @Test
    fun `has next page when a next link exists`() {
        assertTrue(response.toSearchPage().hasNextPage)
    }

    @Test
    fun `has no next page without a next link`() {
        val lastPage = SearchResponseDto(CollectionDto(items = emptyList(), links = emptyList()))

        assertFalse(lastPage.toSearchPage().hasNextPage)
    }

    @Test
    fun `returns an empty page when the collection is missing`() {
        val page = SearchResponseDto(collection = null).toSearchPage()

        assertTrue(page.images.isEmpty())
        assertFalse(page.hasNextPage)
    }

}
