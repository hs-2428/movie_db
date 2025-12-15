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

// Watch Provider Models
data class WatchProvider(
    val providerId: Int,
    val providerName: String,
    val logoPath: String,
    val link: String? = null,
    val type: ProviderType
)

data class RegionalProviders(
    val region: String,
    val link: String? = null,
    val flatrate: List<WatchProvider> = emptyList(),
    val buy: List<WatchProvider> = emptyList(),
    val rent: List<WatchProvider> = emptyList()
)

data class WatchProviderResponse(
    val id: Int,
    val results: Map<String, RegionalProviders>
)

enum class ProviderType {
    FLATRATE, BUY, RENT
}