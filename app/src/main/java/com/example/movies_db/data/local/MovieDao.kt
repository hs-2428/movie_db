package com.example.movies_db.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'")
    suspend fun searchCachedMovies(query: String): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE isInWatchlist = 1")
    fun getWatchlistMovies(): Flow<List<MovieEntity>>

    @Query("UPDATE movies SET isInWatchlist = :inWatchlist WHERE id = :movieId")
    suspend fun updateWatchlistStatus(movieId: Int, inWatchlist: Boolean)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?
}