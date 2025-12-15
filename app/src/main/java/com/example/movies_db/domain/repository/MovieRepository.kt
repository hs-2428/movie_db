package com.example.movies_db.domain.repository

import com.example.movies_db.domain.model.Genre
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.MoviePage
import com.example.movies_db.domain.model.Resource
import com.example.movies_db.domain.model.WatchProviders
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): MoviePage
    fun searchMovies(query: String, page: Int): Flow<Resource<MoviePage>>
    suspend fun getWatchProviders(movieId: Int): WatchProviders?
    suspend fun getGenres(): List<Genre>
    fun getWatchlist(): Flow<List<Movie>>
    suspend fun toggleWatchlist(movie: Movie)
}