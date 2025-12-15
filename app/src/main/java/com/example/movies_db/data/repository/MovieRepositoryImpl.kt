package com.example.movies_db.data.repository

import com.example.movies_db.data.local.MovieDao
import com.example.movies_db.data.local.MovieEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import com.example.movies_db.data.remote.ApiService
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.MoviePage
import com.example.movies_db.domain.model.Resource
import com.example.movies_db.domain.model.WatchProviders
import com.example.movies_db.domain.model.WatchProvider
import com.example.movies_db.domain.repository.MovieRepository
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val movieDao: MovieDao
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): MoviePage {
        try {
            val mockMovies = generateMockMovies(page)
            return MoviePage(
                page = page,
                results = mockMovies,
                totalResults = 10000,
                totalPages = 500
            )
        } catch (e: Exception) {
            return MoviePage(
                page = page,
                results = emptyList(),
                totalResults = 0,
                totalPages = 0
            )
        }
    }

    override fun searchMovies(query: String, page: Int): Flow<Resource<MoviePage>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.searchMovies(
                apiKey = "your_api_key_here", // TODO: Use BuildConfig.API_KEY
                query = query,
                page = page
            )
            
            val movies = parseMovieResponse(response)
            val moviePage = MoviePage(
                page = page,
                results = movies,
                totalResults = (response["total_results"] as? Double)?.toInt() ?: 0,
                totalPages = (response["total_pages"] as? Double)?.toInt() ?: 0
            )
            
            cacheSearchResults(movies)
            emit(Resource.Success(moviePage))
            
        } catch (e: Exception) {
            try {
                val cachedEntities = movieDao.searchCachedMovies(query)
                val cachedMovies = cachedEntities.map { entity ->
                    Movie(
                        id = entity.id,
                        title = entity.title,
                        posterUrl = entity.posterUrl,
                        releaseDate = entity.releaseDate
                    )
                }
                
                if (cachedMovies.isNotEmpty()) {
                    val localPage = MoviePage(
                        page = 1,
                        results = cachedMovies,
                        totalResults = cachedMovies.size,
                        totalPages = 1
                    )
                    emit(Resource.Success(localPage))
                } else {
                    emit(Resource.Error("No results found for \"$query\""))
                }
            } catch (dbError: Exception) {
                emit(Resource.Error("Search failed: ${e.message ?: "Network error"}"))
            }
        }
    }

    override suspend fun getWatchProviders(movieId: Int): WatchProviders? {
        return try {
            // TODO: val response = apiService.getWatchProviders(movieId, BuildConfig.API_KEY)
            val mockResponse = getMockProviderResponse()
            parseWatchProviders(mockResponse)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWatchProviders(response: Map<String, Any>): WatchProviders? {
        val results = response["results"] as? Map<String, Map<String, Any>>
        
        // Try India first
        results?.get("IN")?.let { indiaData ->
            return extractProviders(indiaData)
        }
        
        // Fallback to US
        results?.get("US")?.let { usData ->
            return extractProviders(usData)
        }
        
        // Use first available
        val firstRegion = results?.values?.firstOrNull()
        return firstRegion?.let { extractProviders(it) }
    }

    private fun extractProviders(regionData: Map<String, Any>): WatchProviders {
        val link = regionData["link"] as? String
        val providers = mutableListOf<WatchProvider>()
        
        // Get streaming providers
        val flatrate = regionData["flatrate"] as? List<Map<String, Any>>
        flatrate?.forEach { provider ->
            val name = provider["provider_name"] as? String ?: return@forEach
            val logo = provider["logo_path"] as? String ?: return@forEach
            providers.add(WatchProvider(name, "https://image.tmdb.org/t/p/original$logo"))
        }
        
        return WatchProviders(link, providers)
    }

    override fun getWatchlist(): Flow<List<Movie>> {
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun toggleWatchlist(movie: Movie) {
        // TODO
    }

    private fun generateMockMovies(page: Int): List<Movie> {
        return (1..20).map { index ->
            val id = (page - 1) * 20 + index
            Movie(
                id = id,
                title = "Popular Movie $id",
                posterUrl = "https://image.tmdb.org/t/p/w500/mock_poster_$id.jpg",
                releaseDate = "2024-${(index % 12) + 1}-${(index % 28) + 1}"
            )
        }
    }

    private fun generateMockSearchResults(query: String, page: Int): List<Movie> {
        return (1..20).map { index ->
            val id = (page - 1) * 20 + index
            Movie(
                id = id,
                title = "$query Movie $id",
                posterUrl = "https://image.tmdb.org/t/p/w500/search_poster_$id.jpg",
                releaseDate = "2024-${(index % 12) + 1}-${(index % 28) + 1}"
            )
        }
    }

    private fun getMockProviderResponse(): Map<String, Any> {
        return mapOf(
            "results" to mapOf(
                "IN" to mapOf(
                    "link" to "https://www.themoviedb.org/movie/123/watch?locale=IN",
                    "flatrate" to listOf(
                        mapOf(
                            "provider_name" to "Disney+ Hotstar",
                            "logo_path" to "/Ajqyt5aNxNGjmF9uOfxArGrdf3X.jpg"
                        ),
                        mapOf(
                            "provider_name" to "Amazon Prime Video",
                            "logo_path" to "/68MNrwlkpF7WnmNPXLah69CR5cb.jpg"
                        )
                    )
                ),
                "US" to mapOf(
                    "flatrate" to listOf(
                        mapOf(
                            "provider_name" to "Netflix",
                            "logo_path" to "/t2yyOv40HZeVlLjYsCsPHnWLk4W.jpg"
                        )
                    )
                )
            )
        )
    }

    private fun parseMovieResponse(response: Map<String, Any>): List<Movie> {
        val results = response["results"] as? List<Map<String, Any>> ?: return emptyList()
        
        return results.mapNotNull { movieData ->
            try {
                val id = (movieData["id"] as? Double)?.toInt() ?: return@mapNotNull null
                val title = movieData["title"] as? String ?: return@mapNotNull null
                val posterPath = movieData["poster_path"] as? String
                val releaseDate = movieData["release_date"] as? String ?: ""
                
                val posterUrl = if (posterPath != null) {
                    "https://image.tmdb.org/t/p/w500$posterPath"
                } else {
                    ""
                }
                
                Movie(
                    id = id,
                    title = title,
                    posterUrl = posterUrl,
                    releaseDate = releaseDate
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun cacheSearchResults(movies: List<Movie>) {
        try {
            movies.forEach { movie ->
                val entity = MovieEntity(
                    id = movie.id,
                    title = movie.title,
                    posterUrl = movie.posterUrl,
                    releaseDate = movie.releaseDate
                )
                movieDao.insertMovie(entity)
            }
        } catch (e: Exception) {
            // Ignore failures - cache is optional
        }
    }
}