package com.plexus.iberia.ui.home

import app.cash.turbine.test
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import com.plexus.iberia.domain.usecase.GetRandomImagesUseCase
import com.plexus.iberia.domain.usecase.SearchImagesUseCase
import com.plexus.iberia.ui.MainDispatcherRule
import com.plexus.iberia.ui.home.HomeViewModel.Companion.SEARCH_DEBOUNCE
import com.plexus.iberia.ui.image
import com.plexus.iberia.ui.searchPage
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.mockingDetails
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: NasaImageRepository = mock()
    private var searchResults: (page: Int) -> Result<SearchPage> = { Result.success(searchPage(it)) }
    private var responseDelay: Duration = Duration.ZERO
    private val viewModel by lazy {
        HomeViewModel(SearchImagesUseCase(repository), GetRandomImagesUseCase(repository, Random(0)))
    }

    @Before
    fun setUp() {
        whenever(repository.search(any(), any())).thenAnswer { invocation ->
            val page = invocation.getArgument<Int>(1)
            flow {
                delay(responseDelay)
                emit(searchResults(page))
            }
        }
    }

    @Test
    fun `searches the trimmed query after the debounce`() = runTest {
        viewModel.onQueryChange("  apollo  ")
        advanceUntilIdle()

        assertEquals(listOf("apollo" to 1), searches())
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

        assertEquals(listOf("apollo" to 1), searches())
    }

    @Test
    fun `a new search cancels the one in progress and only its results are shown`() = runTest {
        var oldSearchFinished = false
        whenever(repository.search(eq("apollo"), any())).thenAnswer {
            flow {
                delay(2.seconds)
                oldSearchFinished = true
                emit(Result.success(pageOf("apollo")))
            }
        }
        whenever(repository.search(eq("mars"), any())).thenAnswer { flowOf(Result.success(pageOf("mars"))) }

        viewModel.onQueryChange("apollo")
        advanceTimeBy(SEARCH_DEBOUNCE + 1.milliseconds)
        runCurrent()
        assertTrue(viewModel.state.value.isLoading)

        viewModel.onQueryChange("mars")
        advanceUntilIdle()

        assertEquals(pageOf("mars").images, viewModel.state.value.images)
        assertFalse(oldSearchFinished)
    }

    @Test
    fun `shows loading while the first page is requested`() = runTest {
        responseDelay = 1.seconds
        viewModel.onQueryChange("apollo")
        advanceTimeBy(SEARCH_DEBOUNCE + 1.milliseconds)
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `exposes the error when the search fails`() = runTest {
        searchResults = { Result.failure(DomainError.Network()) }
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
        searchResults = { searchPage(it, hasNextPage = false).let { page -> Result.success(page) } }
        searchFor("apollo")
        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(listOf("apollo" to 1), searches())
    }

    @Test
    fun `retry repeats the failed first page`() = runTest {
        searchResults = { Result.failure(DomainError.Network()) }
        searchFor("apollo")
        searchResults = { Result.success(searchPage(it)) }
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(searchPage(1).images, viewModel.state.value.images)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `retry repeats the failed next page`() = runTest {
        searchFor("apollo")
        searchResults = { Result.failure(DomainError.Network()) }
        viewModel.loadNextPage()
        advanceUntilIdle()
        searchResults = { Result.success(searchPage(it)) }
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.page)
    }

    @Test
    fun `shows the first page of a random feed without a query`() = runTest {
        viewModel
        advanceUntilIdle()

        val (topic, page) = searches().single()
        assertTrue(topic in GetRandomImagesUseCase.TOPICS)
        assertEquals(searchPage(page).images, viewModel.state.value.images)
    }

    @Test
    fun `loads the next random page from another topic when scrolling`() = runTest {
        viewModel
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        val (firstTopic, firstPage) = searches()[0]
        val (secondTopic, secondPage) = searches()[1]
        assertNotEquals(firstTopic, secondTopic)
        assertEquals(firstPage, secondPage)
        assertEquals(2, searches().size)
    }

    @Test
    fun `shows a random feed again when the query is cleared`() = runTest {
        searchFor("apollo")
        viewModel.onQueryChange("   ")
        advanceUntilIdle()

        assertTrue(searches().last().first in GetRandomImagesUseCase.TOPICS)
    }

    @Test
    fun `retry repeats the failed random page`() = runTest {
        searchResults = { Result.failure(DomainError.Network()) }
        viewModel
        advanceUntilIdle()
        assertTrue(viewModel.state.value.error is DomainError.Network)
        searchResults = { Result.success(searchPage(it)) }
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

    private fun searches(): List<Pair<String, Int>> =
        mockingDetails(repository).invocations
            .filter { it.method.name == "search" }
            .map { it.getArgument<String>(0) to it.getArgument<Int>(1) }

    private fun pageOf(query: String) = SearchPage(images = listOf(image("$query-1")), hasNextPage = false)
}
