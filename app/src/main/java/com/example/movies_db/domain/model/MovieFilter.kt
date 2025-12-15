package com.example.movies_db.domain.model

data class MovieFilter(
    val query: String = "",
    val genre: String? = null,
    val year: Int? = null,
    val minRating: Float = 0f,
    val maxRating: Float = 10f
) {
    fun isEmpty(): Boolean {
        return query.isBlank() && 
               genre.isNullOrBlank() && 
               year == null && 
               minRating == 0f && 
               maxRating == 10f
    }
}