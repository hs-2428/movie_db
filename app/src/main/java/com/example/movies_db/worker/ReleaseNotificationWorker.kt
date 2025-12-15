package com.example.movies_db.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.movies_db.MainActivity
import com.example.movies_db.R
import com.example.movies_db.data.local.MovieDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class ReleaseNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val movieDao: MovieDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "movie_releases"
        const val NOTIFICATION_ID = 1001
        const val DEEP_LINK_SCHEME = "moviedb"
        const val EXTRA_MOVIE_ID = "movie_id"
    }

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel()
            
            if (!hasNotificationPermission()) {
                return Result.success()
            }

            // Get upcoming movies from watchlist
            val upcomingMovies = movieDao.getUpcomingWatchlist()
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            
            // Check if any movies have release date equals to today
            val releasedToday = upcomingMovies.filter { movie ->
                movie.releaseDate == today
            }
            
            // Send notifications for movies released today
            releasedToday.forEach { movie ->
                showNotification(movie.id, movie.title)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Movie Releases",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new movie releases"
                setShowBadge(true)
            }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showNotification(movieId: Int, movieTitle: String) {
        val deepLinkIntent = createDeepLinkIntent(movieId)
        val pendingIntent = createPendingIntent(deepLinkIntent)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Movie Released!")
            .setContentText("$movieTitle is now available")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$movieTitle has been released! Tap to view details and find where to watch.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "View Details",
                pendingIntent
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        
        if (hasNotificationPermission()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createDeepLinkIntent(movieId: Int): Intent {
        val deepLinkUri = Uri.parse("$DEEP_LINK_SCHEME://movie/$movieId")
        
        return Intent(applicationContext, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = deepLinkUri
            putExtra(EXTRA_MOVIE_ID, movieId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    private fun createPendingIntent(intent: Intent): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            flags
        )
    }

}