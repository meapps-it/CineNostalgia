package com.meapps.cinenostalgia.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meapps.cinenostalgia.CineNostalgiaApplication
import com.meapps.cinenostalgia.data.MovieDetail
import com.meapps.cinenostalgia.data.MovieRepository
import com.meapps.cinenostalgia.data.MovieSummary
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
    val detail: MovieDetail? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class CineNostalgiaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as CineNostalgiaApplication).repository
    private val _state = MutableStateFlow(MovieUiState())
    val state: StateFlow<MovieUiState> = _state.asStateFlow()
    val favorites = repository.favoriteMovies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val apiReady: Boolean get() = repository.hasApiKey
    private var searchJob: Job? = null

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
            runLoading { copy(results = repository.search(value)) }
        }
    }

    fun openMovie(id: Int) = viewModelScope.launch { runLoading { copy(detail = repository.detail(id)) } }
    fun closeMovie() { _state.value = _state.value.copy(detail = null, error = null) }

    fun toggleFavorite(movie: MovieSummary, isFavorite: Boolean) = viewModelScope.launch {
        repository.toggleFavorite(movie, isFavorite)
    }

    private fun loadFeatured() = viewModelScope.launch {
        runCatching { repository.discover(1970, 2009) }
            .onSuccess { _state.value = _state.value.copy(featured = it.ifEmpty { MovieRepository.demoMovies }) }
    }

    private suspend fun runLoading(transform: suspend MovieUiState.() -> MovieUiState) {
        _state.value = _state.value.copy(loading = true, error = null)
        _state.value = try {
            _state.value.transform().copy(loading = false)
        } catch (_: Exception) {
            _state.value.copy(loading = false, error = "Impossibile caricare i dati. Controlla la connessione e riprova.")
        }
    }
}
