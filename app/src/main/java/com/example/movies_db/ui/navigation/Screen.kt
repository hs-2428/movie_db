package com.example.movies_db.ui.navigation

sealed class Screen(val route: String) {
    // The main list of movies
    data object Home : Screen("home_screen")
    
    // Search movies with infinite scroll
    data object Search : Screen("search_screen")

    // The user's saved movies
    data object Watchlist : Screen("watchlist_screen")

    // The detail view (requires an ID)
    data object Detail : Screen("detail_screen/{movieId}") {
        fun createRoute(movieId: Int) = "detail_screen/$movieId"
    }
}