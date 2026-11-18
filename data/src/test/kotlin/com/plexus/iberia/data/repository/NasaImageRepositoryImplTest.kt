package com.plexus.iberia.data.repository

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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NasaImageRepositoryImplTest {

    private val server = MockWebServer().apply { start() }
    private val repository = NasaImageRepositoryImpl(
        remoteDataSource = NasaRemoteDataSource(server.createNasaApi()),
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @After
    fun tearDown() = server.close()

    @Test
    fun `requests pages of 20 results`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        repository.search(query = "aircraft", page = 1).first()

        assertEquals("20", server.takeRequest().url.queryParameter("page_size"))
    }

    @Test
    fun `returns the mapped page on success`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        val page = repository.search(query = "aircraft", page = 1).first().getOrThrow()

        assertEquals(2, page.images.size)
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

        assertEquals(DomainError.NotFound, error)
    }
}
