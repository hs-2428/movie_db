package com.example.movies_db.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movies_db.ui.screens.HomeScreen
import com.example.movies_db.ui.screens.SearchScreen
import com.example.movies_db.ui.screens.MovieDetailScreen

@Composable
fun AppNavigation(initialMovieId: Int? = null) {
    val navController = rememberNavController()

    // Handle deep link navigation
    LaunchedEffect(initialMovieId) {
        initialMovieId?.let { movieId ->
            navController.navigate(Screen.Detail.createRoute(movieId))
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(route = Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        composable(route = Screen.Watchlist.route) {
        }

        composable(route = Screen.Detail.route) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull() ?: 0
            MovieDetailScreen(
                movieId = movieId,
                navController = navController
            )
        }
    }
}