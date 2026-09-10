package com.meapps.cinenostalgia.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TmdbClient {
    private val http=OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply{level=HttpLoggingInterceptor.Level.BASIC}).build()
    val api:TmdbApi=Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/").client(http).addConverterFactory(GsonConverterFactory.create()).build().create(TmdbApi::class.java)
}
