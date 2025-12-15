package com.example.movies_db.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movies_db.ui.screens.HomeScreen
import com.example.movies_db.ui.screens.WatchlistScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Route 1: Home
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Route 2: Segmented Watchlist with released/upcoming movies
        composable(route = Screen.Watchlist.route) {
            WatchlistScreen(navController = navController)
        }

        // Route 3: Detail (Placeholder for now)
        composable(route = Screen.Detail.route) { backStackEntry ->
            // val movieId = backStackEntry.arguments?.getString("movieId")
            // DetailScreen(movieId)
        }
    }
}