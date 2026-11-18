package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow

class SearchImagesUseCase(private val repository: NasaImageRepository) {

    operator fun invoke(query: String, page: Int = FIRST_PAGE): Flow<Result<SearchPage>> =
        repository.search(query = query.trim(), page = page)

    companion object {
        const val FIRST_PAGE = 1
    }
}
