package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow

class GetImageUseCase(private val repository: NasaImageRepository) {

    operator fun invoke(id: String): Flow<Result<NasaImage>> = repository.getImage(id)
}
