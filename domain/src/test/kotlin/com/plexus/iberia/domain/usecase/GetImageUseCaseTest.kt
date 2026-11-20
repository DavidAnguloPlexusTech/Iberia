package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetImageUseCaseTest {

    private val repository: NasaImageRepository = mock()
    private val getImage = GetImageUseCase(repository)

    @Test
    fun `returns the image of the requested id`() = runTest {
        val image = image("GRC-1")
        whenever(repository.getImage("GRC-1")).thenReturn(flowOf(Result.success(image)))

        assertEquals(image, getImage("GRC-1").first().getOrThrow())
        verify(repository).getImage("GRC-1")
    }

    @Test
    fun `returns the repository error`() = runTest {
        whenever(repository.getImage("missing")).thenReturn(flowOf(Result.failure(DomainError.NotFound())))

        assertTrue(getImage("missing").first().exceptionOrNull() is DomainError.NotFound)
    }

    private fun image(id: String) = NasaImage(
        id = id, title = "", description = "", thumbnailUrl = null, imageUrl = null,
        dateCreated = "", center = "", keywords = emptyList(), photographer = null,
    )
}
