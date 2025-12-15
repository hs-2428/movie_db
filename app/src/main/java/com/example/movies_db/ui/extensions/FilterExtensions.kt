package com.example.movies_db.ui.extensions

import com.example.movies_db.domain.model.MovieFilter

/**
 * Helper extensions for MovieFilter UI operations
 */

fun MovieFilter.hasActiveFilters(): Boolean {
    return query.isNotBlank() || 
           !genre.isNullOrBlank() || 
           year != null || 
           minRating > 0f || 
           maxRating < 10f
}

fun MovieFilter.getFilterSummary(): String {
    val parts = mutableListOf<String>()
    
    if (query.isNotBlank()) {
        parts.add("\"$query\"")
    }
    
    if (!genre.isNullOrBlank()) {
        parts.add(genre!!)
    }
    
    if (year != null) {
        parts.add("$year")
    }
    
    if (minRating > 0f || maxRating < 10f) {
        parts.add("Rating: ${minRating.toInt()}-${maxRating.toInt()}")
    }
    
    return when {
        parts.isEmpty() -> "All Movies"
        parts.size == 1 -> parts.first()
        else -> "${parts.size} filters active"
    }
}