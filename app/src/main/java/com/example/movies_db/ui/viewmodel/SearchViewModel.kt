package com.example.movies_db.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.MovieFilter
import com.example.movies_db.domain.model.Resource
import com.example.movies_db.domain.repository.MovieRepository
import com.example.movies_db.ui.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(MovieFilter())
    val currentFilter: StateFlow<MovieFilter> = _currentFilter.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentMovies = emptyList<Movie>()
    private var currentPage = 0
    private var hasNextPage = true
    private var isLoadingMore = false

    init {
        // Debounce filter changes to avoid API spam
        viewModelScope.launch {
            _currentFilter
                .debounce(300)
                .distinctUntilChanged()
                .collect { filter ->
                    performSearch(filter, isNewSearch = true)
                }
        }
        
        // Load popular movies initially
        loadPopularMovies()
    }

    fun updateQuery(query: String) {
        _currentFilter.value = _currentFilter.value.copy(query = query.trim())
    }

    fun updateGenre(genre: String?) {
        _currentFilter.value = _currentFilter.value.copy(genre = genre)
    }

    fun updateYear(year: Int?) {
        _currentFilter.value = _currentFilter.value.copy(year = year)
    }

    fun updateRatingRange(minRating: Float, maxRating: Float) {
        _currentFilter.value = _currentFilter.value.copy(
            minRating = minRating,
            maxRating = maxRating
        )
    }

    fun clearFilters() {
        _currentFilter.value = MovieFilter()
    }

    fun loadMoreMovies() {
        if (isLoadingMore || !hasNextPage) return
        
        val filter = _currentFilter.value
        if (filter.isEmpty()) {
            loadPopularMovies(isLoadMore = true)
        } else {
            performSearch(filter, isNewSearch = false)
        }
    }

    private fun performSearch(filter: MovieFilter, isNewSearch: Boolean) {
        if (filter.isEmpty()) {
            loadPopularMovies()
            return
        }

        viewModelScope.launch {
            isLoadingMore = !isNewSearch
            val targetPage = if (isNewSearch) 1 else currentPage + 1
            
            if (isNewSearch) {
                _uiState.value = SearchUiState.Loading
                currentMovies = emptyList()
                currentPage = 0
            }

            movieRepository.searchMovies(filter.query, targetPage)
                .catch { e ->
                    handleError("Search failed")
                }
                .collect { resource ->
                    handleSearchResource(resource, isNewSearch, filter)
                }
        }
    }

    private fun handleSearchResource(
        resource: Resource<*>, 
        isNewSearch: Boolean, 
        filter: MovieFilter
    ) {
        when (resource) {
            is Resource.Loading -> {
                if (isNewSearch) {
                    _uiState.value = SearchUiState.Loading
                }
            }
            is Resource.Success -> {
                val moviePage = resource.data as com.example.movies_db.domain.model.MoviePage
                val filteredMovies = applyLocalFilters(moviePage.results, filter)
                
                if (isNewSearch) {
                    currentMovies = filteredMovies
                } else {
                    currentMovies = currentMovies + filteredMovies
                }
                
                currentPage = moviePage.page
                hasNextPage = moviePage.page < moviePage.totalPages
                
                _uiState.value = when {
                    currentMovies.isEmpty() -> SearchUiState.Empty
                    else -> SearchUiState.Success(currentMovies, hasNextPage)
                }
                
                isLoadingMore = false
            }
            is Resource.Error -> {
                handleError(resource.message)
            }
        }
    }

    private fun applyLocalFilters(movies: List<Movie>, filter: MovieFilter): List<Movie> {
        return movies.filter { movie ->
            var matches = true
            
            // Year filter
            if (filter.year != null) {
                val movieYear = movie.releaseDate.split("-").getOrNull(0)?.toIntOrNull()
                matches = matches && movieYear == filter.year
            }
            
            // Future: Add genre and rating filters when Movie model is extended
            
            matches
        }
    }

    private fun loadPopularMovies(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            isLoadingMore = isLoadMore
            val targetPage = if (isLoadMore) currentPage + 1 else 1
            
            if (!isLoadMore) {
                _uiState.value = SearchUiState.Loading
                currentMovies = emptyList()
                currentPage = 0
            }

            try {
                val moviePage = movieRepository.getPopularMovies(targetPage)
                
                if (isLoadMore) {
                    currentMovies = currentMovies + moviePage.results
                } else {
                    currentMovies = moviePage.results
                }
                
                currentPage = moviePage.page
                hasNextPage = moviePage.page < moviePage.totalPages
                
                _uiState.value = when {
                    currentMovies.isEmpty() -> SearchUiState.Empty
                    else -> SearchUiState.Success(currentMovies, hasNextPage)
                }
                
            } catch (e: Exception) {
                handleError("Failed to load movies")
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun handleError(message: String) {
        _uiState.value = SearchUiState.Error(message)
        isLoadingMore = false
    }
}