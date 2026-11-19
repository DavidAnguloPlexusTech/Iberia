package com.plexus.iberia.data.repository

import com.plexus.iberia.data.cache.SearchCache
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
    private val cache: SearchCache,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NasaImageRepository {

    override fun search(query: String, page: Int): Flow<Result<SearchPage>> = flow {
        val cached = cache.get(query, page)
        emit(if (cached != null) Result.success(cached) else fetchPage(query, page))
    }.flowOn(ioDispatcher)

    override fun getImage(id: String): Flow<Result<NasaImage>> = flow {
        val cached = cache.findImage(id)
        emit(if (cached != null) Result.success(cached) else fetchImage(id))
    }.flowOn(ioDispatcher)

    private suspend fun fetchPage(query: String, page: Int): Result<SearchPage> =
        remoteDataSource.search(query = query, page = page)
            .map { it.toSearchPage() }
            .onSuccess { cache.put(query, page, it) }

    private suspend fun fetchImage(id: String): Result<NasaImage> =
        remoteDataSource.getImage(id).mapCatching { it.firstImage() }
}
