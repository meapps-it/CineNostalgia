package com.meapps.cinenostalgia.data

import com.meapps.cinenostalgia.network.TmdbApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MovieRepository(
    private val api: TmdbApi,
    private val favorites: FavoriteDao,
    private val knowledge: KnowledgeRepository,
    private val apiKey: String
) {
    val hasApiKey: Boolean get() = apiKey.isNotBlank()
    val favoriteMovies: Flow<List<MovieSummary>> = favorites.observeAll().map { list -> list.map { it.summary() } }

    fun isFavorite(id: Int): Flow<Boolean> = favorites.observeIsFavorite(id)

    suspend fun toggleFavorite(movie: MovieSummary, favorite: Boolean) {
        if (favorite) favorites.delete(movie.id)
        else favorites.insert(FavoriteEntity(movie.id, movie.title, movie.originalTitle, movie.releaseDate, movie.posterPath))
    }

    suspend fun search(query: String): List<MovieSummary> {
        if (query.isBlank()) return emptyList()
        if (!hasApiKey) {
            val movieMatches = demoMovies.filter { it.title.contains(query, true) || it.originalTitle.contains(query, true) }
            val actorMatches = demoDetail.cast.any { it.name.contains(query, true) }
            return if (actorMatches) (movieMatches + demoMovies).distinctBy { it.id } else movieMatches
        }
        return coroutineScope {
            val moviesRequest = async { api.search(apiKey, query).results }
            val peopleRequest = async { runCatching { api.searchPeople(apiKey, query).results.take(3) }.getOrDefault(emptyList()) }
            val actorMovies = peopleRequest.await().map { person ->
                async { runCatching { api.personMovieCredits(person.id, apiKey).cast }.getOrDefault(emptyList()) }
            }.awaitAll().flatten()
            (moviesRequest.await() + actorMovies)
                .distinctBy { it.id }
                .sortedWith(compareByDescending<com.meapps.cinenostalgia.network.MovieDto> { it.releaseDate?.take(4)?.toIntOrNull() ?: 0 }.thenBy { it.title })
                .map { it.toSummary() }
        }
    }

    suspend fun discover(fromYear: Int, toYear: Int, page: Int = 1): List<MovieSummary> {
        if (!hasApiKey) return demoMovies
        return api.discover(apiKey, "$fromYear-01-01", "$toYear-12-31", page).results.map { it.toSummary() }
    }

    suspend fun detail(id: Int): MovieDetail = coroutineScope {
        if (!hasApiKey || id == DEMO_MOVIE_ID && !hasApiKey) return@coroutineScope demoDetail
        val movieRequest = async { api.movie(id, apiKey) }
        val creditsRequest = async { api.credits(id, apiKey) }
        val providersRequest = async { runCatching { api.providers(id, apiKey) }.getOrNull() }
        val movie = movieRequest.await()
        val summary = movie.toSummary()
        val knowledgeRequest = async { runCatching { knowledge.enrich(summary) }.getOrDefault(KnowledgeEnrichment()) }
        val credits = creditsRequest.await()
        val cast = credits.cast.take(8).map { member ->
            async {
                val person = runCatching { api.person(member.id, apiKey) }.getOrNull()
                PersonRole(member.id, member.name, member.character, person?.profilePath ?: member.profilePath, person?.birthday, person?.deathday)
            }
        }.awaitAll()
        val isBackToFuture = id == DEMO_MOVIE_ID
        val enrichment = knowledgeRequest.await()
        val editorialLocations = if (isBackToFuture) demoDetail.locations else emptyList()
        val editorialCuriosities = if (isBackToFuture) demoDetail.curiosities else emptyList()
        MovieDetail(
            summary = summary,
            director = credits.crew.firstOrNull { it.job == "Director" }?.name,
            runtime = movie.runtime,
            genres = movie.genres.map { it.name },
            overview = enrichment.extendedOverview?.takeIf { it.length > (movie.overview?.length ?: 0) } ?: movie.overview,
            spoiler = enrichment.spoiler ?: if (isBackToFuture) demoDetail.spoiler else null,
            cast = cast,
            locations = (editorialLocations + enrichment.locations).distinctBy { "${it.latitude},${it.longitude}" },
            curiosities = (editorialCuriosities + enrichment.curiosities).distinct(),
            providers = providersRequest.await()?.results?.italy?.let { country ->
                (country.flatrate.orEmpty() + country.rent.orEmpty() + country.buy.orEmpty()).distinctBy { it.name }
                    .map { WatchProvider(it.name, it.logoPath) }
            }.orEmpty(),
            sources = enrichment.sources
        )
    }

    private fun com.meapps.cinenostalgia.network.MovieDto.toSummary() =
        MovieSummary(id, title, originalTitle, releaseDate, posterPath)

    private fun com.meapps.cinenostalgia.network.MovieDetailDto.toSummary() =
        MovieSummary(id, title, originalTitle, releaseDate, posterPath)

    companion object {
        const val DEMO_MOVIE_ID = 105

        val demoMovies = listOf(
            MovieSummary(DEMO_MOVIE_ID, "Ritorno al futuro", "Back to the Future", "1985-07-03", "/fNOH9f1aA7XRTzl1sAOx9iF553Q.jpg")
        )

        val demoDetail = MovieDetail(
            summary = demoMovies.first(),
            director = "Robert Zemeckis",
            runtime = 116,
            genres = listOf("Avventura", "Commedia", "Fantascienza"),
            overview = "Marty McFly viene accidentalmente trasportato dal 1985 al 1955. Per tornare a casa deve riunire i suoi futuri genitori e chiedere aiuto all'eccentrico Doc Brown.",
            spoiler = "Marty riesce a far innamorare i genitori e torna nel 1985 grazie al fulmine che alimenta la DeLorean. Al suo ritorno scopre che il presente è cambiato.",
            cast = listOf(
                PersonRole(521, "Michael J. Fox", "Marty McFly", null, "1961-06-09", null),
                PersonRole(1062, "Christopher Lloyd", "Dr. Emmett Brown", null, "1938-10-22", null),
                PersonRole(1063, "Lea Thompson", "Lorraine Baines", null, "1961-05-31", null),
                PersonRole(1064, "Crispin Glover", "George McFly", null, "1964-04-20", null)
            ),
            locations = listOf(
                FilmLocation("Twin Pines Mall", "Doc presenta a Marty la macchina del tempo", "Puente Hills Mall", "City of Industry, California", 33.9934, -117.9261, "Il luogo reale è un centro commerciale. Aspetto e attività possono cambiare: apri la mappa per le informazioni aggiornate."),
                FilmLocation("Casa di Doc nel 1955", "Marty incontra Doc per la prima volta", "Gamble House", "Pasadena, California", 34.1510, -118.1604, "L'edificio storico è conservato ed è noto per la sua architettura Arts and Crafts.")
            ),
            curiosities = listOf(
                "La DeLorean fu scelta perché la sua forma rendeva credibile, agli occhi degli abitanti del 1955, l'arrivo di un veicolo insolito.",
                "La piazza di Hill Valley è un set cinematografico degli Universal Studios, riutilizzato in numerose produzioni."
            ),
            providers = emptyList()
        )
    }
}
