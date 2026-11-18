package com.plexus.iberia.data.di

import com.plexus.iberia.data.network.provideOkHttpClient
import com.plexus.iberia.data.network.provideRetrofit
import org.koin.dsl.module

internal val networkModule = module {
    single { provideOkHttpClient() }
    single { provideRetrofit(okHttpClient = get()) }
}
