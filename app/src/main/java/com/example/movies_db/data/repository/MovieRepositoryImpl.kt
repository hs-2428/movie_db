package com.example.movies_db.data.repository

import com.example.movies_db.data.local.MovieDao
import com.example.movies_db.data.local.MovieEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import com.example.movies_db.data.remote.ApiService
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.MoviePage
import com.example.movies_db.domain.repository.MovieRepository
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val movieDao: MovieDao
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): MoviePage {
        try {
            // For now, return mock data that simulates API response
            val mockMovies = generateMockMovies(page)
            return MoviePage(
                page = page,
                results = mockMovies,
                totalResults = 10000, // Mock total
                totalPages = 500 // Mock total pages
            )
        } catch (e: Exception) {
            return MoviePage(
                page = page,
                results = emptyList(),
                totalResults = 0,
                totalPages = 0
            )
        }
    }

    override suspend fun searchMovies(query: String, page: Int): MoviePage {
        try {
            // For now, return mock data that simulates search results
            val mockMovies = generateMockSearchResults(query, page)
            return MoviePage(
                page = page,
                results = mockMovies,
                totalResults = 50 * query.length, // Mock based on query
                totalPages = 25 // Mock pages
            )
        } catch (e: Exception) {
            return MoviePage(
                page = page,
                results = emptyList(),
                totalResults = 0,
                totalPages = 0
            )
        }
    }

    override fun getWatchlist(): Flow<List<Movie>> {
        // TODO: Call movieDao.getAllMovies() and map to Domain Movie
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun toggleWatchlist(movie: Movie) {
        // TODO: Logic to add/remove from DB
    }

    // Mock data generators for development
    private fun generateMockMovies(page: Int): List<Movie> {
        return (1..20).map { index ->
            val id = (page - 1) * 20 + index
            Movie(
                id = id,
                title = "Popular Movie $id",
                posterUrl = "https://image.tmdb.org/t/p/w500/mock_poster_$id.jpg",
                releaseDate = "2024-${(index % 12) + 1}-${(index % 28) + 1}"
            )
        }
    }

    private fun generateMockSearchResults(query: String, page: Int): List<Movie> {
        return (1..20).map { index ->
            val id = (page - 1) * 20 + index
            Movie(
                id = id,
                title = "$query Movie $id",
                posterUrl = "https://image.tmdb.org/t/p/w500/search_poster_$id.jpg",
                releaseDate = "2024-${(index % 12) + 1}-${(index % 28) + 1}"
            )
        }
    }
}