package com.plexus.iberia.data.remote

import com.plexus.iberia.domain.error.DomainError
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ApiCallTest {

    @Test
    fun `wraps the value on success`() = runTest {
        assertEquals(Result.success(1), apiCall { 1 })
    }

    @Test
    fun `maps connection errors to Network`() = runTest {
        assertTrue(failureOf { throw IOException() } is DomainError.Network)
    }

    @Test
    fun `maps http errors to Server with its code`() = runTest {
        val error = HttpException(Response.error<Any>(503, "".toResponseBody()))

        assertEquals(DomainError.Server(503), failureOf { throw error })
    }

    @Test
    fun `maps anything else to Unknown`() = runTest {
        assertTrue(failureOf { throw IllegalStateException() } is DomainError.Unknown)
    }

    @Test
    fun `does not swallow cancellation`() {
        assertThrows<CancellationException> {
            runTest { apiCall { throw CancellationException() } }
        }
    }

    private suspend fun failureOf(block: suspend () -> Unit): Throwable? =
        apiCall(block).exceptionOrNull()
}
