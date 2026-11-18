package com.plexus.iberia.domain.di

import com.plexus.iberia.domain.usecase.GetImageUseCase
import com.plexus.iberia.domain.usecase.GetRandomImagesUseCase
import com.plexus.iberia.domain.usecase.SearchImagesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Use cases. */
val domainModule = module {
    factoryOf(::SearchImagesUseCase)
    factoryOf(::GetImageUseCase)
    // Explicit constructor call so the default Random is used.
    factory { GetRandomImagesUseCase(repository = get()) }
}
