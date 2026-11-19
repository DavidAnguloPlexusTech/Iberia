package com.plexus.iberia.data.repository

import com.plexus.iberia.data.cache.SearchCache
import com.plexus.iberia.data.createNasaApi
import com.plexus.iberia.data.readResource
import com.plexus.iberia.data.remote.NasaRemoteDataSource
import com.plexus.iberia.domain.error.DomainError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.TestTimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class NasaImageRepositoryImplTest {

    private val server = MockWebServer().apply { start() }
    private val timeSource = TestTimeSource()
    private val repository = NasaImageRepositoryImpl(
        remoteDataSource = NasaRemoteDataSource(server.createNasaApi()),
        cache = SearchCache(timeSource = timeSource),
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @After
    fun tearDown() = server.close()

    @Test
    fun `requests pages of 10 results`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        repository.search(query = "aircraft", page = 1).first()

        assertEquals("${NasaRemoteDataSource.PAGE_SIZE}", server.takeRequest().url.queryParameter("page_size"))
    }

    @Test
    fun `returns the mapped page on success`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        val page = repository.search(query = "aircraft", page = 1).first().getOrThrow()

        assertEquals(3, page.images.size)
    }

    @Test
    fun `does not call the api until the flow is collected`() {
        repository.search(query = "aircraft", page = 1)

        assertEquals(0, server.requestCount)
    }

    @Test
    fun `returns a Server error when the api fails`() = runTest {
        server.enqueue(MockResponse.Builder().code(500).build())

        val error = repository.search(query = "aircraft", page = 1).first().exceptionOrNull()

        assertEquals(DomainError.Server(500), error)
    }

    @Test
    fun `getImage returns the first valid image`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        val image = repository.getImage("GRC-2015-C-04751").first().getOrThrow()

        assertEquals("GRC-2015-C-04751", image.id)
    }

    @Test
    fun `getImage returns NotFound when there are no results`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"collection":{"items":[]}}""").build())

        val error = repository.getImage("missing").first().exceptionOrNull()

        assertTrue(error is DomainError.NotFound)
    }

    @Test
    fun `a cached page is returned without a network call`() = runTest {
        enqueueSearchResponse()

        val first = repository.search(query = "apollo", page = 1).first()
        val second = repository.search(query = "apollo", page = 1).first()

        assertEquals(1, server.requestCount)
        assertEquals(first, second)
    }

    @Test
    fun `an expired page is fetched again and the cache is updated`() = runTest {
        enqueueSearchResponse()
        enqueueSearchResponse()
        repository.search(query = "apollo", page = 1).first()

        timeSource += SearchCache.DEFAULT_TIME_TO_LIVE
        repository.search(query = "apollo", page = 1).first()
        repository.search(query = "apollo", page = 1).first()

        assertEquals(2, server.requestCount)
    }

    @Test
    fun `another page or query is not served from the cache`() = runTest {
        repeat(3) { enqueueSearchResponse() }

        repository.search(query = "apollo", page = 1).first()
        repository.search(query = "apollo", page = 2).first()
        repository.search(query = "mars", page = 1).first()

        assertEquals(3, server.requestCount)
    }

    @Test
    fun `the same query with different case or spaces uses the cache`() = runTest {
        enqueueSearchResponse()

        repository.search(query = "apollo", page = 1).first()
        repository.search(query = " Apollo ", page = 1).first()

        assertEquals(1, server.requestCount)
    }

    @Test
    fun `errors are not cached`() = runTest {
        server.enqueue(MockResponse.Builder().code(500).build())
        enqueueSearchResponse()

        val failure = repository.search(query = "apollo", page = 1).first()
        val retry = repository.search(query = "apollo", page = 1).first()

        assertEquals(DomainError.Server(500), failure.exceptionOrNull())
        assertTrue(retry.isSuccess)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `detail of an image in a cached page needs no network call`() = runTest {
        enqueueSearchResponse()
        repository.search(query = "aircraft", page = 1).first()

        val image = repository.getImage("GRC-2015-C-04751").first().getOrThrow()

        assertEquals("GRC-2015-C-04751", image.id)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `detail of an image that is not cached is fetched`() = runTest {
        enqueueSearchResponse()

        repository.getImage("GRC-2015-C-04751").first()

        assertEquals("/search", server.takeRequest().url.encodedPath)
        assertEquals(1, server.requestCount)
    }

    private fun enqueueSearchResponse() {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())
    }
}
