package com.example.movies_db.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movies_db.ui.viewmodel.MovieDetailViewModel

@Composable
fun MovieDetailScreen(
    movieId: Int,
    navController: NavController,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val watchProviders by viewModel.watchProviders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(movieId) {
        viewModel.loadWatchProviders(movieId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Movie Detail Screen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Where to Watch section
        Text(
            text = "Where to Watch",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            
            watchProviders != null -> {
                watchProviders?.let { providers ->
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(providers.providers) { provider ->
                            ProviderCard(provider.name, provider.logoPath)
                        }
                    }
                    
                    providers.link?.let { link ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Data provided by JustWatch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                Text(
                    text = "No streaming info available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Movie ID: $movieId",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ProviderCard(name: String, logoPath: String) {
    Card(
        modifier = Modifier.size(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = logoPath,
                contentDescription = name,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}