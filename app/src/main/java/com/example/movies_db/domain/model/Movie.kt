package com.example.movies_db.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val releaseDate: String,
    val inWatchlist: Boolean = false
) {
    // Helper to check if movie is already released
    fun isReleased(): Boolean {
        return try {
            val releaseLocalDate = LocalDate.parse(releaseDate, DateTimeFormatter.ISO_LOCAL_DATE)
            !releaseLocalDate.isAfter(LocalDate.now())
        } catch (e: Exception) {
            // If parsing fails, assume it's released to be safe
            true
        }
    }
}

data class WatchlistData(
    val released: List<Movie>,
    val upcoming: List<Movie>
)