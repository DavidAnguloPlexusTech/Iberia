package com.plexus.iberia.domain.repository

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import kotlinx.coroutines.flow.Flow

interface NasaImageRepository {

    fun search(query: String, page: Int): Flow<Result<SearchPage>>

    fun getImage(id: String): Flow<Result<NasaImage>>
}
