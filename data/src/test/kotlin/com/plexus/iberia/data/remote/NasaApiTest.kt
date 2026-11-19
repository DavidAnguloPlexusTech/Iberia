package com.plexus.iberia.data.remote

import com.plexus.iberia.data.createNasaApi
import com.plexus.iberia.data.readResource
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class NasaApiTest {

    private val server = MockWebServer().apply { start() }
    private val api = server.createNasaApi()

    @After
    fun tearDown() = server.close()

    @Test
    fun `search sends the expected query parameters`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        api.search(query = "aircraft", page = 2, pageSize = 20)

        val url = server.takeRequest().url
        assertEquals("/search", url.encodedPath)
        assertEquals("aircraft", url.queryParameter("q"))
        assertEquals("2", url.queryParameter("page"))
        assertEquals("20", url.queryParameter("page_size"))
        assertEquals("image", url.queryParameter("media_type"))
    }

    @Test
    fun `search parses the response`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        val response = api.search(query = "aircraft", page = 1, pageSize = 20)

        val firstItem = response.collection?.items?.first()?.data?.first()
        assertEquals("GRC-2015-C-04751", firstItem?.nasaId)
        assertEquals("2015-07-29T00:00:00Z", firstItem?.dateCreated)
    }

    @Test
    fun `getImage filters by nasa id`() = runTest {
        server.enqueue(MockResponse.Builder().body(readResource("search_response.json")).build())

        api.getImage(nasaId = "GRC-1")

        val url = server.takeRequest().url
        assertEquals("/search", url.encodedPath)
        assertEquals("GRC-1", url.queryParameter("nasa_id"))
        assertEquals("image", url.queryParameter("media_type"))
    }

}
