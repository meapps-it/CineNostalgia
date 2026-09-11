package com.meapps.cinenostalgia.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class SearchResponse(val results:List<MovieDto>)
data class PersonSearchResponse(val results:List<PersonSearchDto>)
data class PersonSearchDto(val id:Int,val name:String)
data class PersonMovieCreditsDto(val cast:List<MovieDto>)
data class TvSearchResponse(val results:List<TvDto>)
data class TvDto(val id:Int,val name:String,@SerializedName("original_name")val originalName:String,
    @SerializedName("first_air_date")val firstAirDate:String?,@SerializedName("poster_path")val posterPath:String?,
    @SerializedName("backdrop_path")val backdropPath:String?)
data class MovieDto(val id:Int,val title:String,@SerializedName("original_title")val originalTitle:String,
    @SerializedName("release_date")val releaseDate:String?,@SerializedName("poster_path")val posterPath:String?,
    @SerializedName("backdrop_path")val backdropPath:String?)
data class GenreDto(val name:String)
data class CrewDto(val name:String,val job:String)
data class CastDto(val id:Int,val name:String,val character:String,@SerializedName("profile_path")val profilePath:String?)
data class CreditsDto(val cast:List<CastDto>,val crew:List<CrewDto>)
data class PersonDto(val id:Int,val name:String,val birthday:String?,val deathday:String?,@SerializedName("profile_path")val profilePath:String?,
    val biography:String?,@SerializedName("place_of_birth")val placeOfBirth:String?,@SerializedName("known_for_department")val knownForDepartment:String?)
data class ProviderDto(@SerializedName("provider_name")val name:String,@SerializedName("logo_path")val logoPath:String?)
data class ProviderCountryDto(val flatrate:List<ProviderDto>?,val rent:List<ProviderDto>?,val buy:List<ProviderDto>?)
data class ProviderResultsDto(@SerializedName("IT")val italy:ProviderCountryDto?)
data class ProviderResponseDto(val results:ProviderResultsDto)
data class MovieDetailDto(val id:Int,val title:String,@SerializedName("original_title")val originalTitle:String,
    @SerializedName("release_date")val releaseDate:String?,@SerializedName("poster_path")val posterPath:String?,
    @SerializedName("backdrop_path")val backdropPath:String?,val runtime:Int?,val overview:String?,val genres:List<GenreDto>)
data class CreatorDto(val name:String)
data class TvDetailDto(val id:Int,val name:String,@SerializedName("original_name")val originalName:String,
    @SerializedName("first_air_date")val firstAirDate:String?,@SerializedName("poster_path")val posterPath:String?,
    @SerializedName("backdrop_path")val backdropPath:String?,@SerializedName("episode_run_time")val episodeRunTime:List<Int>?,val overview:String?,val genres:List<GenreDto>,
    @SerializedName("created_by")val createdBy:List<CreatorDto>?)
data class CombinedCreditDto(val id:Int,@SerializedName("media_type")val mediaType:String?,val title:String?,val name:String?,
    @SerializedName("original_title")val originalTitle:String?,@SerializedName("original_name")val originalName:String?,
    @SerializedName("release_date")val releaseDate:String?,@SerializedName("first_air_date")val firstAirDate:String?,
    @SerializedName("poster_path")val posterPath:String?,@SerializedName("backdrop_path")val backdropPath:String?)
data class CombinedCreditsResponse(val cast:List<CombinedCreditDto>)
data class KeywordDto(val id:Int,val name:String)
data class KeywordSearchResponse(val results:List<KeywordDto>)

interface TmdbApi {
    @GET("search/movie") suspend fun search(@Query("api_key")key:String,@Query("query")query:String,@Query("language")language:String="it-IT"):SearchResponse
    @GET("search/tv") suspend fun searchTv(@Query("api_key")key:String,@Query("query")query:String,@Query("language")language:String="it-IT"):TvSearchResponse
    @GET("search/person") suspend fun searchPeople(@Query("api_key")key:String,@Query("query")query:String,@Query("language")language:String="it-IT"):PersonSearchResponse
    @GET("person/{id}/movie_credits") suspend fun personMovieCredits(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):PersonMovieCreditsDto
    @GET("person/{id}/combined_credits") suspend fun personCombinedCredits(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):CombinedCreditsResponse
    @GET("discover/movie") suspend fun discover(@Query("api_key")key:String,@Query("primary_release_date.gte")from:String?,@Query("primary_release_date.lte")to:String?,
        @Query("page")page:Int=1,@Query("sort_by")sort:String="popularity.desc",@Query("language")language:String="it-IT",
        @Query("include_adult")includeAdult:Boolean=false,@Query("with_original_language")originalLanguage:String?=null,
        @Query("with_genres")genreId:Int?=null,@Query("with_keywords")keywordId:Int?=null,
        @Query("vote_count.gte")minimumVotes:Int=5):SearchResponse
    @GET("search/keyword") suspend fun searchKeyword(@Query("api_key")key:String,@Query("query")query:String,@Query("page")page:Int=1):KeywordSearchResponse
    @GET("discover/tv") suspend fun discoverTv(@Query("api_key")key:String,@Query("first_air_date.gte")from:String,@Query("first_air_date.lte")to:String,
        @Query("page")page:Int=1,@Query("sort_by")sort:String="popularity.desc",@Query("language")language:String="it-IT",
        @Query("include_adult")includeAdult:Boolean=false):TvSearchResponse
    @GET("movie/{id}") suspend fun movie(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):MovieDetailDto
    @GET("movie/{id}/credits") suspend fun credits(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):CreditsDto
    @GET("movie/{id}/watch/providers") suspend fun providers(@Path("id")id:Int,@Query("api_key")key:String):ProviderResponseDto
    @GET("person/{id}") suspend fun person(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):PersonDto
    @GET("tv/{id}") suspend fun tv(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):TvDetailDto
    @GET("tv/{id}/credits") suspend fun tvCredits(@Path("id")id:Int,@Query("api_key")key:String,@Query("language")language:String="it-IT"):CreditsDto
    @GET("tv/{id}/watch/providers") suspend fun tvProviders(@Path("id")id:Int,@Query("api_key")key:String):ProviderResponseDto
}
