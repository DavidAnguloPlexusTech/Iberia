package com.plexus.iberia.data.network

import com.plexus.iberia.data.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal fun provideRetrofit(
    okHttpClient: OkHttpClient,
    baseUrl: String = BuildConfig.NASA_BASE_URL,
): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
