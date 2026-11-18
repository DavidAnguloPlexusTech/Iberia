package com.plexus.iberia.domain.repository

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import kotlinx.coroutines.flow.Flow

interface NasaImageRepository {

    /** Emits one page of results; a failure always carries a [com.plexus.iberia.domain.error.DomainError]. */
    fun search(query: String, page: Int): Flow<Result<SearchPage>>

    /** Emits the image with the given id, or [com.plexus.iberia.domain.error.DomainError.NotFound]. */
    fun getImage(id: String): Flow<Result<NasaImage>>
}
