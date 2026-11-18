package com.plexus.iberia.data.repository

import com.plexus.iberia.data.mapper.toSearchPage
import com.plexus.iberia.data.remote.NasaRemoteDataSource
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class NasaImageRepositoryImpl(
    private val remoteDataSource: NasaRemoteDataSource,
) : NasaImageRepository {

    override fun search(query: String, page: Int): Flow<Result<SearchPage>> = flow {
        emit(remoteDataSource.search(query = query, page = page).map { it.toSearchPage() })
    }
}
