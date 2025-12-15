package com.example.movies_db.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterUrl: String,
    val releaseDate: String,
    val inWatchlist: Boolean = false,
    val overview: String? = null,
    val rating: Float = 0f
)