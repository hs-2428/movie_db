package com.example.movies_db.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.WatchlistData
import com.example.movies_db.domain.usecase.GetWatchlistUseCase
import com.example.movies_db.domain.usecase.ToggleWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadWatchlist()
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            // Option 1: Use segmented API (recommended)
            getWatchlistUseCase().collect { watchlistData ->
                _uiState.value = _uiState.value.copy(
                    watchlistData = watchlistData,
                    isLoading = false
                )
            }
        }
    }

    // Alternative: Load specific segments when needed
    private fun loadReleasedMovies() {
        viewModelScope.launch {
            getWatchlistUseCase.getReleasedMovies().collect { movies ->
                _uiState.value = _uiState.value.copy(
                    watchlistData = _uiState.value.watchlistData.copy(released = movies)
                )
            }
        }
    }

    fun toggleMovieInWatchlist(movie: Movie) {
        viewModelScope.launch {
            try {
                toggleWatchlistUseCase(movie)
            } catch (e: Exception) {
                // Handle error
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update watchlist"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class WatchlistUiState(
    val watchlistData: WatchlistData = WatchlistData(emptyList(), emptyList()),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val releasedCount: Int get() = watchlistData.released.size
    val upcomingCount: Int get() = watchlistData.upcoming.size
    val totalCount: Int get() = releasedCount + upcomingCount
}