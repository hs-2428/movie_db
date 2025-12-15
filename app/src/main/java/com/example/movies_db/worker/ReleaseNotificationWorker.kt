package com.example.movies_db.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.movies_db.R
import com.example.movies_db.data.local.MovieDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReleaseNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val movieDao: MovieDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "release_notification_work"
        private const val CHANNEL_ID = "movie_releases"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            // Get movies releasing today
            val todayMovies = getTodaysReleases()
            
            if (todayMovies.isNotEmpty()) {
                createNotificationChannel()
                showReleaseNotification(todayMovies.size, todayMovies.first().title)
            }
            
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    private suspend fun getTodaysReleases() = movieDao.getAllMovies().first().filter { movie ->
        try {
            val releaseDate = LocalDate.parse(movie.releaseDate, DateTimeFormatter.ISO_LOCAL_DATE)
            releaseDate == LocalDate.now()
        } catch (e: Exception) {
            false
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Movie Releases",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for movie releases"
        }
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showReleaseNotification(count: Int, firstMovieTitle: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val title = if (count == 1) {
            "Movie Released Today!"
        } else {
            "$count Movies Released Today!"
        }
        
        val content = if (count == 1) {
            firstMovieTitle
        } else {
            "$firstMovieTitle and ${count - 1} others"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @dagger.assisted.AssistedFactory
    interface Factory {
        fun create(context: Context, workerParams: WorkerParameters): ReleaseNotificationWorker
    }
}