package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.RandomFeed
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GetRandomImagesUseCaseTest {

    private val repository = FakeRepository()
    private val getRandomImages = GetRandomImagesUseCase(repository)
    private val feed = RandomFeed(topics = listOf("mars", "moon", "sun"), firstPage = 7)

    @Test
    fun `new feed shuffles every topic and starts on a page in range`() {
        repeat(50) { seed ->
            val newFeed = GetRandomImagesUseCase(repository, Random(seed)).newFeed()

            assertEquals(GetRandomImagesUseCase.TOPICS.toSet(), newFeed.topics.toSet())
            assertTrue(newFeed.firstPage in 1..GetRandomImagesUseCase.MAX_FIRST_PAGE)
        }
    }

    @Test
    fun `feeds differ between random sources`() {
        val feeds = (0 until 10).map { GetRandomImagesUseCase(repository, Random(it)).newFeed() }

        assertNotEquals(1, feeds.distinct().size)
    }

    @Test
    fun `each page uses the next topic`() = runTest {
        val searches = (1..3).map { page -> searchOf(page) }

        assertEquals(listOf("mars" to 7, "moon" to 7, "sun" to 7), searches)
    }

    @Test
    fun `after the last topic it starts again on the next api page`() = runTest {
        assertEquals("mars" to 8, searchOf(page = 4))
    }

    @Test
    fun `the feed never ends`() = runTest {
        repository.hasNextPage = false

        val searchPage = getRandomImages(feed, page = 1).first().getOrThrow()

        assertTrue(searchPage.hasNextPage)
    }

    private suspend fun searchOf(page: Int): Pair<String, Int>? {
        getRandomImages(feed, page).first()
        return repository.lastSearch
    }

    private class FakeRepository : NasaImageRepository {
        var lastSearch: Pair<String, Int>? = null
        var hasNextPage = true

        override fun search(query: String, page: Int): Flow<Result<SearchPage>> {
            lastSearch = query to page
            return flowOf(Result.success(SearchPage(images = emptyList(), hasNextPage = hasNextPage)))
        }

        override fun getImage(id: String): Flow<Result<NasaImage>> = error("Not used")
    }
}
