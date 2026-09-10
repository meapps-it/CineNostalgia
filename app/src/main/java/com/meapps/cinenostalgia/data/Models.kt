package com.meapps.cinenostalgia.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period

data class MovieSummary(val id:Int,val title:String,val originalTitle:String,val releaseDate:String?,val posterPath:String?) {
    val year get()=releaseDate?.take(4).orEmpty()
    val posterUrl get()=posterPath?.let{"https://image.tmdb.org/t/p/w500$it"}
}
data class PersonRole(val id:Int,val name:String,val character:String,val profilePath:String?,val birthday:String?,val deathday:String?) {
    val profileUrl get()=profilePath?.let{"https://image.tmdb.org/t/p/w342$it"}
    fun ageAt(date:String):Int?=birthday?.let{runCatching{Period.between(LocalDate.parse(it),LocalDate.parse(date)).years}.getOrNull()}
    fun currentAge():Int?=birthday?.let { birthDate ->
        runCatching {
            val endDate = deathday?.let { LocalDate.parse(it) } ?: LocalDate.now()
            Period.between(LocalDate.parse(birthDate), endDate).years
        }.getOrNull()
    }
}
data class FilmLocation(val name:String,val scene:String,val realPlace:String,val city:String,val latitude:Double,val longitude:Double,val today:String)
data class WatchProvider(val name:String,val logoPath:String?){val logoUrl get()=logoPath?.let{"https://image.tmdb.org/t/p/w92$it"}}
data class MovieDetail(val summary:MovieSummary,val director:String?,val runtime:Int?,val genres:List<String>,val overview:String?,val spoiler:String?,val cast:List<PersonRole>,val locations:List<FilmLocation>,val curiosities:List<String>,val providers:List<WatchProvider>,val sources:List<String> = emptyList())

@Entity(tableName="favorites")
data class FavoriteEntity(@PrimaryKey val movieId:Int,val title:String,val originalTitle:String,val releaseDate:String?,val posterPath:String?) {
    fun summary()=MovieSummary(movieId,title,originalTitle,releaseDate,posterPath)
}
