package com.example.movies_db.data.repository

import com.example.movies_db.data.local.MovieDao
import com.example.movies_db.data.local.MovieEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import com.example.movies_db.data.remote.ApiService
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.WatchlistData
import com.example.movies_db.domain.repository.MovieRepository
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val movieDao: MovieDao
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): List<Movie> {
        // TODO: Call apiService.getPopularMovies() and map to Domain Movie
        return emptyList()
    }

    override fun getWatchlist(): Flow<List<Movie>> {
        return movieDao.getAllMovies().map { entities ->
            entities.map { it.toDomainMovie() }
        }
    }

    // Main segmented API - combines both flows efficiently
    override fun getSegmentedWatchlist(): Flow<WatchlistData> {
        return combine(
            movieDao.getReleasedMovies(),
            movieDao.getUpcomingMovies()
        ) { releasedEntities, upcomingEntities ->
            WatchlistData(
                released = releasedEntities.map { it.toDomainMovie() },
                upcoming = upcomingEntities.map { it.toDomainMovie() }
            )
        }
    }

    // Separate flows for specific segments
    override fun getReleasedWatchlist(): Flow<List<Movie>> {
        return movieDao.getReleasedMovies().map { entities ->
            entities.map { it.toDomainMovie() }
        }
    }

    override fun getUpcomingWatchlist(): Flow<List<Movie>> {
        return movieDao.getUpcomingMovies().map { entities ->
            entities.map { it.toDomainMovie() }
        }
    }

    override suspend fun toggleWatchlist(movie: Movie) {
        if (isInWatchlist(movie.id)) {
            removeFromWatchlist(movie)
        } else {
            addToWatchlist(movie)
        }
    }

    override suspend fun addToWatchlist(movie: Movie) {
        movieDao.insertMovie(movie.toEntity())
    }

    override suspend fun removeFromWatchlist(movie: Movie) {
        movieDao.deleteMovieById(movie.id)
    }

    override suspend fun isInWatchlist(movieId: Int): Boolean {
        return movieDao.isMovieInWatchlist(movieId)
    }

    // Extension functions for mapping
    private fun MovieEntity.toDomainMovie() = Movie(
        id = id,
        title = title,
        posterUrl = posterUrl,
        releaseDate = releaseDate,
        inWatchlist = true // Always true since it's from watchlist
    )

    private fun Movie.toEntity() = MovieEntity(
        id = id,
        title = title,
        posterUrl = posterUrl,
        releaseDate = releaseDate
    )
}