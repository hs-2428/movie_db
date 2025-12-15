package com.example.movies_db.domain.usecase

import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.repository.MovieRepository
import javax.inject.Inject

// UseCase for adding/removing movies from watchlist
class ToggleWatchlistUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movie: Movie) {
        repository.toggleWatchlist(movie)
    }
    
    suspend fun addToWatchlist(movie: Movie) {
        repository.addToWatchlist(movie)
    }
    
    suspend fun removeFromWatchlist(movie: Movie) {
        repository.removeFromWatchlist(movie)
    }
    
    suspend fun isInWatchlist(movieId: Int): Boolean {
        return repository.isInWatchlist(movieId)
    }
}