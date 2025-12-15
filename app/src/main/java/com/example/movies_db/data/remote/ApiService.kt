package com.example.movies_db.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Popular movies with pagination support
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Map<String, Any> // They will replace Map with a real DTO class
    
    // Search movies with pagination support
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Map<String, Any> // They will replace Map with a real DTO class
    
    // Get watch providers for a specific movie
    @GET("movie/{movie_id}/watch/providers")
    suspend fun getWatchProviders(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Map<String, Any> // They will replace Map with a real DTO class
}