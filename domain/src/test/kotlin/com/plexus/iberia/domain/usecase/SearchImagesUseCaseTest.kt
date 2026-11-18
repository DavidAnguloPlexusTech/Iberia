package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchImagesUseCaseTest {

    private val repository = FakeNasaImageRepository()
    private val searchImages = SearchImagesUseCase(repository)

    @Test
    fun `trims the query before searching`() = runTest {
        searchImages("  apollo  ").first()

        assertEquals("apollo", repository.lastQuery)
    }

    @Test
    fun `searches the first page by default`() = runTest {
        searchImages("apollo").first()

        assertEquals(SearchImagesUseCase.FIRST_PAGE, repository.lastPage)
    }

    @Test
    fun `returns the repository result`() = runTest {
        val result = searchImages("apollo", page = 2).first()

        assertEquals(Result.success(FakeNasaImageRepository.PAGE), result)
    }

    private class FakeNasaImageRepository : NasaImageRepository {
        var lastQuery: String? = null
        var lastPage: Int? = null

        override fun search(query: String, page: Int): Flow<Result<SearchPage>> {
            lastQuery = query
            lastPage = page
            return flowOf(Result.success(PAGE))
        }

        companion object {
            val PAGE = SearchPage(images = emptyList(), hasNextPage = false)
        }
    }
}
