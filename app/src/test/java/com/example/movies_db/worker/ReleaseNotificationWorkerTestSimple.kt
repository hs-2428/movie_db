package com.example.movies_db.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkerFactory
import com.example.movies_db.data.local.MovieDao
import com.example.movies_db.data.local.MovieEntity
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReleaseNotificationWorkerTestSimple {
    
    private lateinit var context: Context
    private lateinit var mockMovieDao: MovieDao
    private val today = "2024-01-15"
    private val tomorrow = "2024-01-16"
    private val yesterday = "2024-01-14"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockMovieDao = mockk()
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    private fun createWorkerFactory(): WorkerFactory {
        return object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return if (workerClassName == ReleaseNotificationWorker::class.java.name) {
                    ReleaseNotificationWorker(appContext, workerParameters, mockMovieDao)
                } else {
                    null
                }
            }
        }
    }

    private fun createMovieEntity(id: Long, title: String, releaseDate: String): MovieEntity {
        return MovieEntity(
            id = id,
            title = title,
            overview = "Test overview for $title",
            releaseDate = releaseDate,
            posterPath = "/test_poster.jpg",
            backdropPath = "/test_backdrop.jpg",
            voteAverage = 7.5,
            voteCount = 1000
        )
    }

    @Test
    fun `worker returns success when no movies release today`() = runTest {
        // Given: No movies releasing today
        every { mockMovieDao.getAllMovies() } returns flowOf(emptyList())
        
        // When: Worker executes
        val worker = TestListenableWorkerBuilder<ReleaseNotificationWorker>(context)
            .setWorkerFactory(createWorkerFactory())
            .build()
        val result = worker.doWork()
        
        // Then: Worker completes successfully even with no movies
        assertEquals(ListenableWorker.Result.success(), result)
        
        // Verify dao was called
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker returns success for movies releasing today`() = runTest {
        // Given: Movies releasing today
        val todaysMovies = listOf(
            createMovieEntity(1, "Movie 1", today),
            createMovieEntity(2, "Movie 2", today)
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(todaysMovies)
        
        // When: Worker executes
        val worker = TestListenableWorkerBuilder<ReleaseNotificationWorker>(context)
            .setWorkerFactory(createWorkerFactory())
            .build()
        val result = worker.doWork()
        
        // Then: Worker completes successfully
        assertEquals(ListenableWorker.Result.success(), result)
        
        // Verify dao was called
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker handles movies with different release dates correctly`() = runTest {
        // Given: Mixed movies (some today, some not)
        val mixedMovies = listOf(
            createMovieEntity(1, "Today Movie", today),
            createMovieEntity(2, "Tomorrow Movie", tomorrow),
            createMovieEntity(3, "Yesterday Movie", yesterday)
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(mixedMovies)
        
        // When: Worker executes
        val worker = TestListenableWorkerBuilder<ReleaseNotificationWorker>(context)
            .setWorkerFactory(createWorkerFactory())
            .build()
        val result = worker.doWork()
        
        // Then: Worker completes successfully
        assertEquals(ListenableWorker.Result.success(), result)
        
        // Verify dao was called
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker handles dao exception gracefully`() = runTest {
        // Given: DAO throws exception
        every { mockMovieDao.getAllMovies() } throws RuntimeException("Database error")
        
        // When: Worker executes
        val worker = TestListenableWorkerBuilder<ReleaseNotificationWorker>(context)
            .setWorkerFactory(createWorkerFactory())
            .build()
        val result = worker.doWork()
        
        // Then: Worker returns failure result
        assertEquals(ListenableWorker.Result.failure(), result)
        
        // Verify dao was called
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker handles movies with invalid date formats gracefully`() = runTest {
        // Given: Movies with invalid date formats
        val moviesWithBadDates = listOf(
            createMovieEntity(1, "Invalid Date Movie", "invalid-date"),
            createMovieEntity(2, "Valid Date Movie", today)
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(moviesWithBadDates)
        
        // When: Worker executes
        val worker = TestListenableWorkerBuilder<ReleaseNotificationWorker>(context)
            .setWorkerFactory(createWorkerFactory())
            .build()
        val result = worker.doWork()
        
        // Then: Worker handles invalid dates and completes successfully
        assertEquals(ListenableWorker.Result.success(), result)
        
        // Verify dao was called
        verify { mockMovieDao.getAllMovies() }
    }
}