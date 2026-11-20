package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SearchImagesUseCaseTest {

    private val repository: NasaImageRepository = mock()
    private val searchImages = SearchImagesUseCase(repository)
    private val page = SearchPage(images = emptyList(), hasNextPage = false)

    @BeforeEach
    fun setUp() {
        whenever(repository.search(any(), any())).thenReturn(flowOf(Result.success(page)))
    }

    @Test
    fun `trims the query before searching`() = runTest {
        searchImages("  apollo  ").first()

        verify(repository).search(query = "apollo", page = SearchImagesUseCase.FIRST_PAGE)
    }

    @Test
    fun `searches the requested page`() = runTest {
        searchImages("apollo", page = 3).first()

        verify(repository).search(query = "apollo", page = 3)
    }

    @Test
    fun `returns the repository result`() = runTest {
        assertEquals(Result.success(page), searchImages("apollo").first())
    }
}
