package com.plexus.iberia.data.repository

import com.plexus.iberia.data.createNasaApi
import com.plexus.iberia.data.readResource
import com.plexus.iberia.data.remote.NasaRemoteDataSource
import com.plexus.iberia.domain.error.DomainError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class NasaImageRepositoryImplTest {

    private val server = MockWebServer()
    private val repository = NasaImageRepositoryImpl(NasaRemoteDataSource(server.createNasaApi()))

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `requests pages of 20 results`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("search_response.json")))

        repository.search(query = "aircraft", page = 1).first()

        assertEquals("20", server.takeRequest().requestUrl?.queryParameter("page_size"))
    }

    @Test
    fun `returns the mapped page on success`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("search_response.json")))

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
        server.enqueue(MockResponse().setResponseCode(500))

        val error = repository.search(query = "aircraft", page = 1).first().exceptionOrNull()

        assertEquals(DomainError.Server(500), error)
    }
}
