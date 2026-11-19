package com.plexus.iberia.ui.home

import app.cash.turbine.test
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.usecase.GetRandomImagesUseCase
import com.plexus.iberia.domain.usecase.SearchImagesUseCase
import com.plexus.iberia.ui.FakeNasaImageRepository
import com.plexus.iberia.ui.MainDispatcherRule
import com.plexus.iberia.ui.home.HomeViewModel.Companion.SEARCH_DEBOUNCE
import com.plexus.iberia.ui.searchPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNasaImageRepository()
    private val viewModel by lazy {
        HomeViewModel(SearchImagesUseCase(repository), GetRandomImagesUseCase(repository, Random(0)))
    }

    @Test
    fun `searches the trimmed query after the debounce`() = runTest {
        viewModel.onQueryChange("  apollo  ")
        advanceUntilIdle()

        assertEquals(listOf("apollo" to 1), repository.searches)
        assertEquals(searchPage(1).images, viewModel.state.value.images)
    }

    @Test
    fun `keeps the typed text in the state`() = runTest {
        viewModel.onQueryChange("apo")

        assertEquals("apo", viewModel.state.value.query)
    }

    @Test
    fun `only searches the last query when typing fast`() = runTest {
        viewModel.onQueryChange("ap")
        advanceTimeBy(SEARCH_DEBOUNCE / 2)
        viewModel.onQueryChange("apollo")
        advanceUntilIdle()

        assertEquals(listOf("apollo" to 1), repository.searches)
    }

    @Test
    fun `shows loading while the first page is requested`() = runTest {
        repository.responseDelay = 1.seconds
        viewModel.onQueryChange("apollo")
        advanceTimeBy(SEARCH_DEBOUNCE + 1.milliseconds)
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `exposes the error when the search fails`() = runTest {
        repository.searchResults = { Result.failure(DomainError.Network()) }
        viewModel.onQueryChange("apollo")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error is DomainError.Network)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `starts loading`() = runTest {
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `appends the next page`() = runTest {
        searchFor("apollo")
        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(searchPage(1).images + searchPage(2).images, viewModel.state.value.images)
        assertEquals(2, viewModel.state.value.page)
    }

    @Test
    fun `does not request more pages after the last one`() = runTest {
        repository.searchResults = { searchPage(it, hasNextPage = false).let { page -> Result.success(page) } }
        searchFor("apollo")
        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(listOf("apollo" to 1), repository.searches)
    }

    @Test
    fun `retry repeats the failed first page`() = runTest {
        repository.searchResults = { Result.failure(DomainError.Network()) }
        searchFor("apollo")
        repository.searchResults = { Result.success(searchPage(it)) }
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(searchPage(1).images, viewModel.state.value.images)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `retry repeats the failed next page`() = runTest {
        searchFor("apollo")
        repository.searchResults = { Result.failure(DomainError.Network()) }
        viewModel.loadNextPage()
        advanceUntilIdle()
        repository.searchResults = { Result.success(searchPage(it)) }
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.page)
    }

    @Test
    fun `shows the first page of a random feed without a query`() = runTest {
        viewModel
        advanceUntilIdle()

        val (topic, page) = repository.searches.single()
        assertTrue(topic in GetRandomImagesUseCase.TOPICS)
        assertEquals(searchPage(page).images, viewModel.state.value.images)
    }

    @Test
    fun `loads the next random page from another topic when scrolling`() = runTest {
        viewModel
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        val (firstTopic, firstPage) = repository.searches[0]
        val (secondTopic, secondPage) = repository.searches[1]
        assertNotEquals(firstTopic, secondTopic)
        assertEquals(firstPage, secondPage)
        assertEquals(2, repository.searches.size)
    }

    @Test
    fun `shows a random feed again when the query is cleared`() = runTest {
        searchFor("apollo")
        viewModel.onQueryChange("   ")
        advanceUntilIdle()

        assertTrue(repository.searches.last().first in GetRandomImagesUseCase.TOPICS)
    }

    @Test
    fun `retry repeats the failed random page`() = runTest {
        repository.searchResults = { Result.failure(DomainError.Network()) }
        viewModel
        advanceUntilIdle()
        assertTrue(viewModel.state.value.error is DomainError.Network)
        repository.searchResults = { Result.success(searchPage(it)) }
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.error)
        assertTrue(viewModel.state.value.images.isNotEmpty())
    }

    @Test
    fun `navigates to detail when an image is clicked`() = runTest {
        viewModel.events.test {
            viewModel.onImageClick("GRC-1")

            assertEquals(HomeEvent.NavigateToDetail("GRC-1"), awaitItem())
        }
    }

    private fun kotlinx.coroutines.test.TestScope.searchFor(query: String) {
        viewModel.onQueryChange(query)
        advanceUntilIdle()
    }
}
