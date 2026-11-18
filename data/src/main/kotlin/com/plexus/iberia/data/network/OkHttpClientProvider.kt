package com.plexus.iberia.data.network

import com.plexus.iberia.data.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.time.Duration.Companion.seconds

private val TIMEOUT = 15.seconds

internal fun provideOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(TIMEOUT)
        .readTimeout(TIMEOUT)
        .addInterceptor(provideLoggingInterceptor())
        .build()

private fun provideLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply { level = loggingLevel() }

private fun loggingLevel(): HttpLoggingInterceptor.Level =
    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
