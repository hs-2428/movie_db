package com.example.movies_db.ui.state

import com.example.movies_db.domain.model.Movie

sealed class SearchUiState {
    object Loading : SearchUiState()
    data class Success(val movies: List<Movie>, val hasMore: Boolean = false) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    object Empty : SearchUiState()
}