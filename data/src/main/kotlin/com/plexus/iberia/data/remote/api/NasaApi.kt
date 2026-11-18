package com.plexus.iberia.data.remote.api

import com.plexus.iberia.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface NasaApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("media_type") mediaType: String = MEDIA_TYPE_IMAGE,
    ): SearchResponseDto

    @GET("search")
    suspend fun getImage(
        @Query("nasa_id") nasaId: String,
        @Query("media_type") mediaType: String = MEDIA_TYPE_IMAGE,
    ): SearchResponseDto

    companion object {
        const val MEDIA_TYPE_IMAGE = "image"
    }
}
