package com.example.movies_db.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MovieEntity::class], 
    version = 2,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns with default values
                database.execSQL(
                    "ALTER TABLE movies ADD COLUMN inWatchlist INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE movies ADD COLUMN overview TEXT"
                )
                database.execSQL(
                    "ALTER TABLE movies ADD COLUMN rating REAL NOT NULL DEFAULT 0.0"
                )
            }
        }
    }
}