package com.plexus.iberia.data.di

import com.plexus.iberia.data.network.provideOkHttpClient
import com.plexus.iberia.data.network.provideRetrofit
import com.plexus.iberia.data.remote.api.NasaApi
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

internal val networkModule = module {
    single { provideOkHttpClient() }
    single { provideRetrofit(okHttpClient = get()) }
    single<NasaApi> { get<Retrofit>().create() }
}
