package com.example.movies_db.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.PaginatedMovies
import com.example.movies_db.domain.model.Resource
import com.example.movies_db.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _moviesState = MutableStateFlow(
        PaginatedMovies(
            movies = emptyList(),
            currentPage = 0,
            hasNextPage = true,
            isLoading = false,
            isError = false
        )
    )
    val moviesState: StateFlow<PaginatedMovies> = _moviesState.asStateFlow()

    private var currentSearchQuery = ""
    private var isLoadingNextPage = false

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && query != currentSearchQuery) {
            currentSearchQuery = query
            searchMovies(query, isNewSearch = true)
        } else if (query.isBlank()) {
            loadPopularMovies(isNewSearch = true)
        }
    }

    fun loadMoreMovies() {
        if (isLoadingNextPage || !_moviesState.value.hasNextPage) return
        
        if (currentSearchQuery.isNotBlank()) {
            searchMovies(currentSearchQuery, isNewSearch = false)
        } else {
            loadPopularMovies(isNewSearch = false)
        }
    }

    private fun searchMovies(query: String, isNewSearch: Boolean) {
        viewModelScope.launch {
            isLoadingNextPage = true
            val targetPage = if (isNewSearch) 1 else _moviesState.value.currentPage + 1
            
            if (isNewSearch) {
                _moviesState.value = _moviesState.value.copy(isLoading = true, isError = false)
            }

            movieRepository.searchMovies(query, targetPage)
                .catch { e ->
                    _moviesState.value = _moviesState.value.copy(
                        isLoading = false,
                        isError = true
                    )
                    isLoadingNextPage = false
                }
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            if (isNewSearch) {
                                _moviesState.value = _moviesState.value.copy(isLoading = true, isError = false)
                            }
                        }
                        is Resource.Success -> {
                            val moviePage = resource.data
                            val existingMovies = if (isNewSearch) emptyList() else _moviesState.value.movies
                            val newMovies = existingMovies + moviePage.results
                            
                            _moviesState.value = PaginatedMovies(
                                movies = newMovies,
                                currentPage = moviePage.page,
                                hasNextPage = moviePage.page < moviePage.totalPages,
                                isLoading = false,
                                isError = false
                            )
                            isLoadingNextPage = false
                        }
                        is Resource.Error -> {
                            _moviesState.value = _moviesState.value.copy(
                                isLoading = false,
                                isError = true
                            )
                            isLoadingNextPage = false
                        }
                    }
                }
        }
    }

    private fun loadPopularMovies(isNewSearch: Boolean) {
        viewModelScope.launch {
            isLoadingNextPage = true
            val targetPage = if (isNewSearch) 1 else _moviesState.value.currentPage + 1
            
            if (isNewSearch) {
                _moviesState.value = _moviesState.value.copy(isLoading = true, isError = false)
            }

            try {
                val moviePage = movieRepository.getPopularMovies(targetPage)
                
                val existingMovies = if (isNewSearch) emptyList() else _moviesState.value.movies
                val newMovies = existingMovies + moviePage.results
                
                _moviesState.value = PaginatedMovies(
                    movies = newMovies,
                    currentPage = moviePage.page,
                    hasNextPage = moviePage.page < moviePage.totalPages,
                    isLoading = false,
                    isError = false
                )
            } catch (e: Exception) {
                _moviesState.value = _moviesState.value.copy(
                    isLoading = false,
                    isError = true
                )
            } finally {
                isLoadingNextPage = false
            }
        }
    }

    init {
        loadPopularMovies(isNewSearch = true)
    }
}