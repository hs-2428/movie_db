package com.example.movies_db.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val releaseDate: String,
    val inWatchlist: Boolean = false
)

data class MoviePage(
    val page: Int,
    val results: List<Movie>,
    val totalResults: Int,
    val totalPages: Int
)

data class PaginatedMovies(
    val movies: List<Movie>,
    val currentPage: Int,
    val hasNextPage: Boolean,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

data class WatchProvider(
    val name: String,
    val logoPath: String
)

data class WatchProviders(
    val link: String?,
    val providers: List<WatchProvider>
)