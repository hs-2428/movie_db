package com.example.movies_db.domain.repository

import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.MoviePage
import com.example.movies_db.domain.model.RegionalProviders
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): MoviePage
    suspend fun searchMovies(query: String, page: Int): MoviePage
    suspend fun getWatchProviders(movieId: Int): RegionalProviders?
    fun getWatchlist(): Flow<List<Movie>>
    suspend fun toggleWatchlist(movie: Movie)
}