package com.plexus.iberia.data.di

import com.plexus.iberia.data.remote.NasaRemoteDataSource
import com.plexus.iberia.data.repository.NasaImageRepositoryImpl
import com.plexus.iberia.domain.repository.NasaImageRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Data sources, API clients and repository implementations. */
val dataModule = module {
    includes(networkModule)
    factoryOf(::NasaRemoteDataSource)
    // Explicit constructor call so the default IO dispatcher is used.
    single<NasaImageRepository> { NasaImageRepositoryImpl(remoteDataSource = get()) }
}
