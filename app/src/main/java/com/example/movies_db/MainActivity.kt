package com.example.movies_db

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.example.movies_db.ui.theme.Movies_DBTheme
import com.example.movies_db.ui.navigation.AppNavigation
import com.example.movies_db.worker.ReleaseNotificationWorker

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var deepLinkMovieId by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleDeepLink(intent)
        
        setContent {
            Movies_DBTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(initialMovieId = deepLinkMovieId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data?.scheme == ReleaseNotificationWorker.DEEP_LINK_SCHEME) {
                    val movieId = extractMovieIdFromUri(data)
                    deepLinkMovieId = movieId
                }
            }
        }
        
        // Also check for movie ID extra
        val movieId = intent.getIntExtra(ReleaseNotificationWorker.EXTRA_MOVIE_ID, -1)
        if (movieId != -1) {
            deepLinkMovieId = movieId
        }
    }

    private fun extractMovieIdFromUri(uri: Uri): Int? {
        // Parse URI like "moviedb://movie/123"
        val pathSegments = uri.pathSegments
        return if (pathSegments.size >= 2 && pathSegments[0] == "movie") {
            pathSegments[1].toIntOrNull()
        } else {
            null
        }
    }
}