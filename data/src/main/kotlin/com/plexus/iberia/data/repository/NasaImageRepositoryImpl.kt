package com.plexus.iberia.data.repository

import com.plexus.iberia.data.mapper.firstImage
import com.plexus.iberia.data.mapper.toSearchPage
import com.plexus.iberia.data.remote.NasaRemoteDataSource
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class NasaImageRepositoryImpl(
    private val remoteDataSource: NasaRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NasaImageRepository {

    override fun search(query: String, page: Int): Flow<Result<SearchPage>> = flow {
        emit(remoteDataSource.search(query = query, page = page).map { it.toSearchPage() })
    }.flowOn(ioDispatcher)

    override fun getImage(id: String): Flow<Result<NasaImage>> = flow {
        emit(remoteDataSource.getImage(id).mapCatching { it.firstImage() })
    }.flowOn(ioDispatcher)
}
