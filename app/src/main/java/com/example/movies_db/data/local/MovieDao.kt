package com.example.movies_db.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies")
    fun getAllMovies(): Flow<List<MovieEntity>>

    // Efficient query for released movies (date <= today)
    @Query("SELECT * FROM movies WHERE releaseDate <= date('now') ORDER BY releaseDate DESC")
    fun getReleasedMovies(): Flow<List<MovieEntity>>

    // Efficient query for upcoming movies (date > today)
    @Query("SELECT * FROM movies WHERE releaseDate > date('now') ORDER BY releaseDate ASC")
    fun getUpcomingMovies(): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovieById(movieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE id = :movieId)")
    suspend fun isMovieInWatchlist(movieId: Int): Boolean
}