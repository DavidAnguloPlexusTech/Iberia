package com.plexus.iberia.domain.di

import com.plexus.iberia.domain.usecase.GetImageUseCase
import com.plexus.iberia.domain.usecase.GetRandomImagesUseCase
import com.plexus.iberia.domain.usecase.SearchImagesUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { SearchImagesUseCase(repository = get()) }
    factory { GetImageUseCase(repository = get()) }
    factory { GetRandomImagesUseCase(repository = get()) }
}
