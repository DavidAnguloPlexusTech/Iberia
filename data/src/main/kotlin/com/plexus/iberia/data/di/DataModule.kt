package com.plexus.iberia.data.di

import com.plexus.iberia.data.cache.SearchCache
import com.plexus.iberia.data.remote.NasaRemoteDataSource
import com.plexus.iberia.data.repository.NasaImageRepositoryImpl
import com.plexus.iberia.domain.repository.NasaImageRepository
import org.koin.dsl.module

val dataModule = module {
    includes(networkModule)
    factory { NasaRemoteDataSource(api = get()) }
    single { SearchCache() }
    single<NasaImageRepository> { NasaImageRepositoryImpl(remoteDataSource = get(), cache = get()) }
}
