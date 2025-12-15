package com.example.movies_db.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "movie_release_notifications"

    fun scheduleReleaseNotifications(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReleaseNotificationWorker>(
            repeatInterval = 24, 
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelReleaseNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}