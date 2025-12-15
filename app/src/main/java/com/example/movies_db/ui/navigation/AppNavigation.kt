package com.example.movies_db.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movies_db.ui.screens.HomeScreen
import com.example.movies_db.ui.screens.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Search.route // Start with search to demo pagination
    ) {
        // Route 1: Home
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        // Route 2: Search with infinite scroll
        composable(route = Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        // Route 3: Watchlist (Placeholder for now)
        composable(route = Screen.Watchlist.route) {
        }

        // Route 4: Detail (Placeholder for now)
        composable(route = Screen.Detail.route) { backStackEntry ->
            // val movieId = backStackEntry.arguments?.getString("movieId")
            // DetailScreen(movieId)
        }
    }
}