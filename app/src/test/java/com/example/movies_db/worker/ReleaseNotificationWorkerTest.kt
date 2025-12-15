package com.example.movies_db.worker

import android.app.NotificationManager
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNotificationManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
class ReleaseNotificationWorkerTest {

    private lateinit var context: Context
    private lateinit var mockMovieDao: MovieDao
    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager
    
    private val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockMovieDao = mockk()
        
        // Setup notification manager spy
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = shadowOf(notificationManager)
        
        // Clear any existing notifications
        shadowNotificationManager.cancelAll()
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `worker returns success when no movies release today`() = runTest {
        // Given: No movies releasing today
        val moviesWithDifferentDates = listOf(
            createMovieEntity(1, "Yesterday Movie", yesterday),
            createMovieEntity(2, "Tomorrow Movie", tomorrow)
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(moviesWithDifferentDates)

        // When: Worker executes
        val worker = createWorker()
        val result = worker.doWork()

        // Then: Returns success and no notifications sent
        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, shadowNotificationManager.allNotifications.size)
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker returns success and sends notification for single movie releasing today`() = runTest {
        // Given: One movie releasing today
        val todaysMovies = listOf(
            createMovieEntity(1, "Awesome Movie", today),
            createMovieEntity(2, "Future Movie", tomorrow)
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(todaysMovies)

        // When: Worker executes
        val worker = createWorker()
        val result = worker.doWork()

        // Then: Returns success and notification sent
        assertEquals(ListenableWorker.Result.success(), result)
        
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(1, notifications.size)
        
        val notification = notifications[0]
        assertEquals("Movie Released Today!", notification.extras.getString("android.title"))
        assertEquals("Awesome Movie", notification.extras.getString("android.text"))
        
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker returns success and sends notification for multiple movies releasing today`() = runTest {
        // Given: Multiple movies releasing today
        val todaysMovies = listOf(
            createMovieEntity(1, "First Movie", today),
            createMovieEntity(2, "Second Movie", today),
            createMovieEntity(3, "Third Movie", today),
            createMovieEntity(4, "Future Movie", tomorrow)
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(todaysMovies)

        // When: Worker executes
        val worker = createWorker()
        val result = worker.doWork()

        // Then: Returns success and notification sent with count
        assertEquals(ListenableWorker.Result.success(), result)
        
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(1, notifications.size)
        
        val notification = notifications[0]
        assertEquals("3 Movies Released Today!", notification.extras.getString("android.title"))
        assertEquals("First Movie and 2 others", notification.extras.getString("android.text"))
        
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker handles movies with invalid date formats gracefully`() = runTest {
        // Given: Movies with various date formats including invalid ones
        val moviesWithMixedDates = listOf(
            createMovieEntity(1, "Valid Date Movie", today),
            createMovieEntity(2, "Invalid Date Movie", "invalid-date"),
            createMovieEntity(3, "Empty Date Movie", ""),
            createMovieEntity(4, "Malformed Date Movie", "2024-13-45")
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(moviesWithMixedDates)

        // When: Worker executes
        val worker = createWorker()
        val result = worker.doWork()

        // Then: Returns success and only processes valid dates
        assertEquals(ListenableWorker.Result.success(), result)
        
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(1, notifications.size)
        
        val notification = notifications[0]
        assertEquals("Movie Released Today!", notification.extras.getString("android.title"))
        assertEquals("Valid Date Movie", notification.extras.getString("android.text"))
        
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker returns failure when dao throws exception`() = runTest {
        // Given: DAO throws exception
        every { mockMovieDao.getAllMovies() } throws RuntimeException("Database error")

        // When: Worker executes
        val worker = createWorker()
        val result = worker.doWork()

        // Then: Returns failure and no notifications sent
        assertEquals(ListenableWorker.Result.failure(), result)
        assertEquals(0, shadowNotificationManager.allNotifications.size)
        
        verify { mockMovieDao.getAllMovies() }
    }

    @Test
    fun `worker creates notification channel before sending notification`() = runTest {
        // Given: Movie releasing today
        val todaysMovies = listOf(createMovieEntity(1, "Test Movie", today))
        every { mockMovieDao.getAllMovies() } returns flowOf(todaysMovies)

        // When: Worker executes
        val worker = createWorker()
        val result = worker.doWork()

        // Then: Notification channel is created
        assertEquals(ListenableWorker.Result.success(), result)
        
        val channels = shadowNotificationManager.notificationChannels
        assertTrue("Notification channel should be created", channels.isNotEmpty())
        
        val movieChannel = channels.find { it.id == "movie_releases" }
        assertNotNull("Movie releases channel should exist", movieChannel)
        assertEquals("Movie Releases", movieChannel?.name)
    }

    @Test
    fun `notification has correct properties for single movie`() = runTest {
        // Given: Single movie releasing today
        val todaysMovies = listOf(createMovieEntity(42, "Epic Movie", today))
        every { mockMovieDao.getAllMovies() } returns flowOf(todaysMovies)

        // When: Worker executes
        val worker = createWorker()
        worker.doWork()

        // Then: Notification has correct properties
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(1, notifications.size)
        
        val notification = notifications[0]
        
        // Verify notification content
        assertEquals("Movie Released Today!", notification.extras.getString("android.title"))
        assertEquals("Epic Movie", notification.extras.getString("android.text"))
        
        // Verify notification properties
        assertTrue("Notification should auto-cancel", notification.flags and android.app.Notification.FLAG_AUTO_CANCEL != 0)
        assertEquals("Channel ID should match", "movie_releases", notification.channelId)
    }

    @Test
    fun `notification content is properly formatted for multiple movies`() = runTest {
        // Given: Five movies releasing today
        val todaysMovies = listOf(
            createMovieEntity(1, "Action Movie", today),
            createMovieEntity(2, "Comedy Movie", today),
            createMovieEntity(3, "Drama Movie", today),
            createMovieEntity(4, "Horror Movie", today),
            createMovieEntity(5, "Sci-Fi Movie", today)
        )
        every { mockMovieDao.getAllMovies() } returns flowOf(todaysMovies)

        // When: Worker executes
        val worker = createWorker()
        worker.doWork()

        // Then: Notification content is properly formatted
        val notifications = shadowNotificationManager.allNotifications
        val notification = notifications[0]
        
        assertEquals("5 Movies Released Today!", notification.extras.getString("android.title"))
        assertEquals("Action Movie and 4 others", notification.extras.getString("android.text"))
    }

    @Test
    fun `worker uses correct notification id for consistency`() = runTest {
        // Given: Movie releasing today
        val todaysMovies = listOf(createMovieEntity(1, "Test Movie", today))
        every { mockMovieDao.getAllMovies() } returns flowOf(todaysMovies)

        // When: Worker executes multiple times
        val worker1 = createWorker()
        worker1.doWork()
        
        val worker2 = createWorker()
        worker2.doWork()

        // Then: Notifications use same ID (replacing previous)
        val notifications = shadowNotificationManager.allNotifications
        assertEquals("Should only have one notification due to same ID", 1, notifications.size)
    }

    @Test
    fun `worker handles empty movie list gracefully`() = runTest {
        // Given: No movies in database
        every { mockMovieDao.getAllMovies() } returns flowOf(emptyList())

        // When: Worker executes
        val worker = createWorker()
        val result = worker.doWork()

        // Then: Returns success and no notifications sent
        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, shadowNotificationManager.allNotifications.size)
        verify { mockMovieDao.getAllMovies() }
    }

    // Helper methods for clean test setup
    private fun createWorker(): ReleaseNotificationWorker {
        return TestListenableWorkerBuilder<ReleaseNotificationWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return ReleaseNotificationWorker(appContext, workerParameters, mockMovieDao)
                }
            })
            .build()
    }

    private fun createMovieEntity(id: Int, title: String, releaseDate: String) = MovieEntity(
        id = id,
        title = title,
        posterUrl = "https://example.com/poster$id.jpg",
        releaseDate = releaseDate
    )
}