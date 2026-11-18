package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetImageUseCaseTest {

    @Test
    fun `returns the image of the requested id`() = runTest {
        val getImage = GetImageUseCase(FakeRepository())

        assertEquals("GRC-1", getImage("GRC-1").first().getOrThrow().id)
    }

    private class FakeRepository : NasaImageRepository {
        override fun search(query: String, page: Int): Flow<Result<SearchPage>> = error("Not used")

        override fun getImage(id: String): Flow<Result<NasaImage>> = flowOf(
            Result.success(
                NasaImage(
                    id = id, title = "", description = "", thumbnailUrl = null, imageUrl = null,
                    dateCreated = "", center = "", keywords = emptyList(), photographer = null,
                ),
            ),
        )
    }
}
