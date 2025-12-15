package com.example.movies_db.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HiltWorkerFactory @Inject constructor(
    private val releaseNotificationWorkerFactory: ReleaseNotificationWorker.Factory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            ReleaseNotificationWorker::class.java.name -> {
                releaseNotificationWorkerFactory.create(appContext, workerParameters)
            }
            else -> null
        }
    }
}