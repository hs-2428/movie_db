package com.example.movies_db.di

import android.app.Application
import androidx.room.Room
import com.example.movies_db.data.local.MovieDao
import com.example.movies_db.data.local.MovieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMovieDatabase(app: Application): MovieDatabase {
        return Room.databaseBuilder(
            app,
            MovieDatabase::class.java,
            "movie_database"
        )
            .addMigrations(MovieDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideMovieDao(db: MovieDatabase): MovieDao {
        return db.movieDao()
    }
}