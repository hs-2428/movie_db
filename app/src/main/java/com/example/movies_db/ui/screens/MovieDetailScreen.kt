package com.example.movies_db.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movies_db.domain.model.Genre
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.ui.viewmodel.MovieDetailViewModel

@Composable
fun MovieDetailScreen(
    movieId: Int,
    navController: NavController,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val watchProviders by viewModel.watchProviders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Mock movie data for demonstration - in real app this would come from ViewModel
    val mockMovie = Movie(
        id = movieId,
        title = "The Amazing Movie",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        releaseDate = "2024-03-15",
        overview = "This is an amazing movie about adventure, friendship, and discovering the true meaning of courage. Join our heroes as they embark on an epic journey through uncharted territories, facing challenges that will test their bonds and reveal their true potential.",
        genres = listOf(
            Genre(1, "Action"),
            Genre(2, "Adventure"),
            Genre(3, "Drama")
        )
    )

    LaunchedEffect(movieId) {
        viewModel.loadWatchProviders(movieId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Movie poster and title section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Movie poster
            Card(
                modifier = Modifier.size(width = 120.dp, height = 180.dp)
            ) {
                AsyncImage(
                    model = mockMovie.posterUrl,
                    contentDescription = mockMovie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Movie info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mockMovie.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Release Date: ${mockMovie.releaseDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Genre chips
                if (mockMovie.genres.isNotEmpty()) {
                    Text(
                        text = "Genres",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mockMovie.genres) { genre ->
                            GenreChip(genre.name)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Where to Watch section
        Text(
            text = "Where to Watch",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            watchProviders != null -> {
                watchProviders?.let { providers ->
                    if (providers.providers.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(providers.providers) { provider ->
                                ProviderCard(provider.name, provider.logoPath)
                            }
                        }
                        
                        providers.link?.let { link ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Data provided by JustWatch",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "No streaming providers available in your region",
                            style = MaterialTheme.typography.bodyMedium,
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Overview section
        if (!mockMovie.overview.isNullOrBlank()) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = mockMovie.overview,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.5
            )
        }
    }
}

@Composable
fun GenreChip(genreName: String) {
    AssistChip(
        onClick = { /* Handle genre click */ },
        label = {
            Text(
                text = genreName,
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = Modifier.height(32.dp)
    )
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