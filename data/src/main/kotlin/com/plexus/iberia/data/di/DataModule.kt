package com.plexus.iberia.data.di

import com.plexus.iberia.data.remote.NasaRemoteDataSource
import com.plexus.iberia.data.repository.NasaImageRepositoryImpl
import com.plexus.iberia.domain.repository.NasaImageRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Data sources, API clients and repository implementations. */
val dataModule = module {
    includes(networkModule)
    factoryOf(::NasaRemoteDataSource)
    singleOf(::NasaImageRepositoryImpl) { bind<NasaImageRepository>() }
}
