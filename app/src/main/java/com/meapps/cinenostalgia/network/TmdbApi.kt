package com.meapps.cinenostalgia.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class SearchResponse(val results:List<MovieDto>)
data class MovieDto(val id:Int,val title:String,@SerializedName("original_title")val originalTitle:String,
    @SerializedName("release_date")val releaseDate:String?,@SerializedName("poster_path")val posterPath:String?)
data class GenreDto(val name:String)
data class CrewDto(val name:String,val job:String)
data class CastDto(val id:Int,val name:String,val character:String,@SerializedName("profile_path")val profilePath:String?)
data class CreditsDto(val cast:List<CastDto>,val crew:List<CrewDto>)
data class PersonDto(val id:Int,val name:String,val birthday:String?,val deathday:String?,@SerializedName("profile_path")val profilePath:String?)
data class ProviderDto(@SerializedName("provider_name")val name:String,@SerializedName("logo_path")val logoPath:String?)
data class ProviderCountryDto(val flatrate:List<ProviderDto>?,val rent:List<ProviderDto>?,val buy:List<ProviderDto>?)
data class ProviderResultsDto(@SerializedName("IT")val italy:ProviderCountryDto?)
data class ProviderResponseDto(val results:ProviderResultsDto)
data class MovieDetailDto(val id:Int,val title:String,@SerializedName("original_title")val originalTitle:String,
    @SerializedName("release_date")val releaseDate:String?,@SerializedName("poster_path")val posterPath:String?,
    val runtime:Int?,val overview:String?,val genres:List<GenreDto>)

interface TmdbApi {
    @GET("search/movie") suspend fun search(@Query("api_key")key:String,@Query("query")query:String,@Query("language")language:String="it-IT"):SearchResponse
    @GET("discover/movie") suspend fun discover(@Query("api_key")key:String,@Query("primary_release_date.gte")from:String,@Query("primary_release_date.lte")to:String,
        @Query("page")page:Int=1,@Query("sort_by")sort:String="popularity.desc",@Query("language")language:String="it-IT",
        @Query("include_adult")includeAdult:Boolean=false):SearchResponse
    @GET("movie/{id}") suspend fun movie(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):MovieDetailDto
    @GET("movie/{id}/credits") suspend fun credits(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):CreditsDto
    @GET("movie/{id}/watch/providers") suspend fun providers(@Path("id")id:Int,@Query("api_key")key:String):ProviderResponseDto
    @GET("person/{id}") suspend fun person(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):PersonDto
}
