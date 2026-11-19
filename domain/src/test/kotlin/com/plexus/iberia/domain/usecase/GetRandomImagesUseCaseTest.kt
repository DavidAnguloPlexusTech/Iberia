package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.RandomFeed
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.random.Random

class GetRandomImagesUseCaseTest {

    private val repository: NasaImageRepository = mock()
    private val getRandomImages = GetRandomImagesUseCase(repository)
    private val feed = RandomFeed(topics = listOf("mars", "moon", "sun"), firstPage = 7)

    @Before
    fun setUp() {
        givenRepositoryPage(hasNextPage = true)
    }

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
        (1..3).forEach { page -> getRandomImages(feed, page).first() }

        inOrder(repository) {
            verify(repository).search(query = "mars", page = 7)
            verify(repository).search(query = "moon", page = 7)
            verify(repository).search(query = "sun", page = 7)
        }
    }

    @Test
    fun `after the last topic it starts again on the next api page`() = runTest {
        getRandomImages(feed, page = 4).first()

        verify(repository).search(query = "mars", page = 8)
    }

    @Test
    fun `the feed never ends`() = runTest {
        givenRepositoryPage(hasNextPage = false)

        val searchPage = getRandomImages(feed, page = 1).first().getOrThrow()

        assertTrue(searchPage.hasNextPage)
    }

    private fun givenRepositoryPage(hasNextPage: Boolean) {
        whenever(repository.search(any(), any()))
            .thenReturn(flowOf(Result.success(SearchPage(images = emptyList(), hasNextPage = hasNextPage))))
    }
}
