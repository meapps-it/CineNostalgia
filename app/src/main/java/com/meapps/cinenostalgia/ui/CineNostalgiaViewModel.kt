package com.meapps.cinenostalgia.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meapps.cinenostalgia.CineNostalgiaApplication
import com.meapps.cinenostalgia.data.MovieDetail
import com.meapps.cinenostalgia.data.MovieRepository
import com.meapps.cinenostalgia.data.MovieSummary
import com.meapps.cinenostalgia.data.PersonDetail
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MovieUiState(
    val query: String = "",
    val results: List<MovieSummary> = emptyList(),
    val featured: List<MovieSummary> = MovieRepository.demoMovies,
    val italianMovies: List<MovieSummary> = emptyList(),
    val featuredSeries: List<MovieSummary> = emptyList(),
    val detail: MovieDetail? = null,
    val personDetail: PersonDetail? = null,
    val decadeLabel: String? = null,
    val decadeMovies: List<MovieSummary> = emptyList(),
    val decadePage: Int = 0,
    val canLoadMore: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null
)

class CineNostalgiaViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as CineNostalgiaApplication
    private val repository = app.repository
    private val settingsRepository = app.settingsRepository
    private val _state = MutableStateFlow(MovieUiState())
    val state: StateFlow<MovieUiState> = _state.asStateFlow()
    val favorites = repository.favoriteMovies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val fontScale = settingsRepository.fontScale.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1f)
    val apiReady: Boolean get() = repository.hasApiKey
    private var searchJob: Job? = null
    private var activeBrowse = BrowseFilter("Film", 1960, 2009)

    init { loadFeatured() }

    fun updateQuery(value: String) {
        _state.value = _state.value.copy(query = value)
        searchJob?.cancel()
        if (value.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), error = null)
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            runLoading { current -> current.copy(results = repository.search(value)) }
        }
    }

    fun openMovie(item: MovieSummary) = viewModelScope.launch {
        runLoading { current -> current.copy(detail = repository.detail(item)) }
    }
    fun closeMovie() { _state.value = _state.value.copy(detail = null, error = null) }

    fun openPerson(id: Int) = viewModelScope.launch {
        runLoading { current -> current.copy(personDetail = repository.personDetail(id)) }
    }

    fun closePerson() { _state.value = _state.value.copy(personDetail = null, error = null) }

    fun toggleFavorite(movie: MovieSummary, isFavorite: Boolean) = viewModelScope.launch {
        repository.toggleFavorite(movie, isFavorite)
    }

    fun setFontScale(value: Float) = viewModelScope.launch { settingsRepository.setFontScale(value) }

    fun openDecade(label: String, fromYear: Int, toYear: Int) = viewModelScope.launch {
        activeBrowse = BrowseFilter(label, fromYear, toYear)
        _state.value = _state.value.copy(decadeLabel = label, decadeMovies = emptyList(), decadePage = 0, canLoadMore = true)
        loadBrowsePage()
    }

    fun openCategory(label: String, genreId: Int? = null, originalLanguage: String? = null, keyword: String? = null, fromYear: Int = 1960, toYear: Int = 2009) = viewModelScope.launch {
        activeBrowse = BrowseFilter(label, fromYear, toYear, genreId, originalLanguage, keyword)
        _state.value = _state.value.copy(decadeLabel = label, decadeMovies = emptyList(), decadePage = 0, canLoadMore = true)
        loadBrowsePage()
    }

    fun loadMoreBrowse() = viewModelScope.launch { loadBrowsePage() }

    fun closeDecade() {
        _state.value = _state.value.copy(decadeLabel = null, decadeMovies = emptyList(), decadePage = 0, canLoadMore = true)
    }

    private fun loadFeatured() = viewModelScope.launch {
        val italian = runCatching { repository.discoverCategory(originalLanguage = "it") }.getOrDefault(emptyList())
        val films = runCatching { repository.discover(1970, 2009) }.getOrDefault(MovieRepository.demoMovies)
        val series = runCatching { repository.discoverSeries() }.getOrDefault(emptyList())
        _state.value = _state.value.copy(italianMovies = italian, featured = films.ifEmpty { MovieRepository.demoMovies }, featuredSeries = series)
    }

    private suspend fun loadBrowsePage() {
        if (_state.value.loading || !_state.value.canLoadMore) return
        val nextPage = _state.value.decadePage + 1
        runLoading { current ->
            val page = repository.discoverCategory(activeBrowse.fromYear, activeBrowse.toYear, nextPage, activeBrowse.genreId, activeBrowse.originalLanguage, activeBrowse.keyword)
            current.copy(
                decadeMovies = (current.decadeMovies + page).distinctBy { it.id },
                decadePage = nextPage,
                canLoadMore = page.isNotEmpty() && repository.hasApiKey
            )
        }
    }

    private data class BrowseFilter(val label: String, val fromYear: Int, val toYear: Int, val genreId: Int? = null, val originalLanguage: String? = null, val keyword: String? = null)

    private suspend fun runLoading(transform: suspend (MovieUiState) -> MovieUiState) {
        _state.value = _state.value.copy(loading = true, error = null)
        _state.value = try {
            transform(_state.value).copy(loading = false)
        } catch (_: Exception) {
            _state.value.copy(loading = false, error = "Impossibile caricare i dati. Controlla la connessione e riprova.")
        }
    }
}
