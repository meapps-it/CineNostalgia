package com.meapps.cinenostalgia.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KnowledgeClient {
    private val http = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("User-Agent", "CineNostalgia/1.0 (https://github.com/meapps-it/CineNostalgia)").build())
        }
        .build()

    val wikipedia: WikipediaApi = Retrofit.Builder()
        .baseUrl("https://it.wikipedia.org/")
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WikipediaApi::class.java)

    val wikidata: WikidataApi = Retrofit.Builder()
        .baseUrl("https://query.wikidata.org/")
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WikidataApi::class.java)
}
