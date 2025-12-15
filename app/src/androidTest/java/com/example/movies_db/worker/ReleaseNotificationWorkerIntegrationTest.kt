package com.example.movies_db.worker

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.movies_db.data.local.MovieDao
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ReleaseNotificationWorkerIntegrationTest {

    private lateinit var workManager: WorkManager
    private lateinit var mockMovieDao: MovieDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
        
        mockMovieDao = mockk(relaxed = true)
    }

    @Test
    fun `worker can be scheduled successfully`() {
        // Given: A work request for release notifications
        val workRequest = OneTimeWorkRequestBuilder<ReleaseNotificationWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .addTag(ReleaseNotificationWorker.WORK_NAME)
            .build()

        // When: Work is enqueued
        workManager.enqueue(workRequest)

        // Then: Work is in the queue
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()
        assertTrue("Work should be enqueued", 
            workInfo.state == WorkInfo.State.ENQUEUED || 
            workInfo.state == WorkInfo.State.RUNNING
        )
    }

    @Test
    fun `worker has correct configuration`() {
        // Given: A work request
        val workRequest = OneTimeWorkRequestBuilder<ReleaseNotificationWorker>()
            .addTag(ReleaseNotificationWorker.WORK_NAME)
            .build()

        // When: Examining work request properties
        val tags = workRequest.tags

        // Then: Work has correct tag
        assertTrue("Work should have correct tag", 
            tags.contains(ReleaseNotificationWorker.WORK_NAME))
        assertEquals("Work should target correct worker class",
            ReleaseNotificationWorker::class.java.name,
            workRequest.workSpec.workerClassName)
    }
}