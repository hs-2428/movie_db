package com.example.movies_db.domain.repository

import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.WatchlistData
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): List<Movie>
    fun getWatchlist(): Flow<List<Movie>>
    fun getSegmentedWatchlist(): Flow<WatchlistData>
    fun getReleasedWatchlist(): Flow<List<Movie>>
    fun getUpcomingWatchlist(): Flow<List<Movie>>
    suspend fun toggleWatchlist(movie: Movie)
    suspend fun addToWatchlist(movie: Movie)
    suspend fun removeFromWatchlist(movie: Movie)
    suspend fun isInWatchlist(movieId: Int): Boolean
}