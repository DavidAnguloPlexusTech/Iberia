package com.plexus.iberia.data.remote

import com.plexus.iberia.data.remote.api.NasaApi
import com.plexus.iberia.data.remote.dto.SearchResponseDto

internal class NasaRemoteDataSource(private val api: NasaApi) {

    suspend fun search(query: String, page: Int): Result<SearchResponseDto> =
        apiCall { api.search(query = query, page = page, pageSize = PAGE_SIZE) }

    suspend fun getImage(nasaId: String): Result<SearchResponseDto> =
        apiCall { api.getImage(nasaId = nasaId) }

    companion object {
        const val PAGE_SIZE = 20
    }
}
