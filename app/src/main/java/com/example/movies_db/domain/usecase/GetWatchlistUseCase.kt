package com.example.movies_db.domain.usecase

import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.WatchlistData
import com.example.movies_db.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Efficient UseCase for segmented watchlist with multiple API options
class GetWatchlistUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    // Main API: Get complete segmented watchlist data
    operator fun invoke(): Flow<WatchlistData> {
        return repository.getSegmentedWatchlist()
    }

    // Alternative API: Get all movies in one list (backward compatibility)
    fun getAllWatchlistMovies(): Flow<List<Movie>> {
        return repository.getWatchlist()
    }

    // Specific segment APIs for granular UI control
    fun getReleasedMovies(): Flow<List<Movie>> {
        return repository.getReleasedWatchlist()
    }

    fun getUpcomingMovies(): Flow<List<Movie>> {
        return repository.getUpcomingWatchlist()
    }
}