package com.plexus.iberia.data

import com.plexus.iberia.data.network.provideRetrofit
import com.plexus.iberia.data.remote.api.NasaApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.create

internal fun readResource(name: String): String =
    requireNotNull(object {}.javaClass.classLoader?.getResource(name)) { "Missing resource $name" }
        .readText()

internal fun MockWebServer.createNasaApi(): NasaApi =
    provideRetrofit(okHttpClient = OkHttpClient(), baseUrl = url("/").toString()).create()
