package com.example.movies_db.data.repository

import com.example.movies_db.data.local.MovieDao
import com.example.movies_db.data.local.MovieEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import com.example.movies_db.data.remote.ApiService
import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.model.MoviePage
import com.example.movies_db.domain.model.RegionalProviders
import com.example.movies_db.domain.model.WatchProvider
import com.example.movies_db.domain.model.ProviderType
import com.example.movies_db.domain.repository.MovieRepository
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val movieDao: MovieDao
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): MoviePage {
        try {
            // For now, return mock data that simulates API response
            val mockMovies = generateMockMovies(page)
            return MoviePage(
                page = page,
                results = mockMovies,
                totalResults = 10000, // Mock total
                totalPages = 500 // Mock total pages
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

    override suspend fun searchMovies(query: String, page: Int): MoviePage {
        try {
            // For now, return mock data that simulates search results
            val mockMovies = generateMockSearchResults(query, page)
            return MoviePage(
                page = page,
                results = mockMovies,
                totalResults = 50 * query.length, // Mock based on query
                totalPages = 25 // Mock pages
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

    override suspend fun getWatchProviders(movieId: Int): RegionalProviders? {
        return try {
            // TODO: Replace with actual API call
            // val response = apiService.getWatchProviders(movieId, BuildConfig.API_KEY)
            val mockResponse = generateMockWatchProviderResponse(movieId)
            
            // Filter for India region with fallback logic
            filterRegionSpecificProviders(mockResponse)
        } catch (e: Exception) {
            null
        }
    }

    override fun getWatchlist(): Flow<List<Movie>> {
        // TODO: Call movieDao.getAllMovies() and map to Domain Movie
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun toggleWatchlist(movie: Movie) {
        // TODO: Logic to add/remove from DB
    }

    /**
     * Filters watch providers for India region with fallback logic:
     * 1. First try India (IN)
     * 2. If not available, fallback to US
     * 3. If US not available, use first available region
     */
    private fun filterRegionSpecificProviders(response: Map<String, Any>): RegionalProviders? {
        val results = response["results"] as? Map<String, Map<String, Any>> ?: return null
        
        // Priority order for region fallback
        val regionPriority = listOf("IN", "US")
        
        // Try preferred regions first
        for (region in regionPriority) {
            results[region]?.let { regionData ->
                return parseRegionalProviders(region, regionData)
            }
        }
        
        // If no preferred regions found, use first available region
        val firstAvailableRegion = results.keys.firstOrNull()
        return firstAvailableRegion?.let { region ->
            parseRegionalProviders(region, results[region]!!)
        }
    }

    private fun parseRegionalProviders(region: String, regionData: Map<String, Any>): RegionalProviders {
        val link = regionData["link"] as? String
        
        val flatrateProviders = parseProviders(regionData["flatrate"] as? List<Map<String, Any>>, ProviderType.FLATRATE)
        val buyProviders = parseProviders(regionData["buy"] as? List<Map<String, Any>>, ProviderType.BUY)
        val rentProviders = parseProviders(regionData["rent"] as? List<Map<String, Any>>, ProviderType.RENT)
        
        return RegionalProviders(
            region = region,
            link = link,
            flatrate = flatrateProviders,
            buy = buyProviders,
            rent = rentProviders
        )
    }

    private fun parseProviders(providersData: List<Map<String, Any>>?, type: ProviderType): List<WatchProvider> {
        return providersData?.mapNotNull { providerMap ->
            val id = (providerMap["provider_id"] as? Number)?.toInt() ?: return@mapNotNull null
            val name = providerMap["provider_name"] as? String ?: return@mapNotNull null
            val logoPath = providerMap["logo_path"] as? String ?: return@mapNotNull null
            
            WatchProvider(
                providerId = id,
                providerName = name,
                logoPath = "https://image.tmdb.org/t/p/original$logoPath",
                type = type
            )
        } ?: emptyList()
    }

    // Mock data generators for development
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

    private fun generateMockWatchProviderResponse(movieId: Int): Map<String, Any> {
        return mapOf(
            "id" to movieId,
            "results" to when (movieId % 3) {
                // Case 1: India providers available
                0 -> mapOf(
                    "IN" to mapOf(
                        "link" to "https://www.themoviedb.org/movie/$movieId/watch?locale=IN",
                        "flatrate" to listOf(
                            mapOf(
                                "provider_id" to 337,
                                "provider_name" to "Disney+ Hotstar",
                                "logo_path" to "/Ajqyt5aNxNGjmF9uOfxArGrdf3X.jpg"
                            ),
                            mapOf(
                                "provider_id" to 119,
                                "provider_name" to "Amazon Prime Video",
                                "logo_path" to "/68MNrwlkpF7WnmNPXLah69CR5cb.jpg"
                            )
                        ),
                        "buy" to listOf(
                            mapOf(
                                "provider_id" to 2,
                                "provider_name" to "Apple iTunes",
                                "logo_path" to "/peURlLlr8jggOwK53fJ5wdQl05y.jpg"
                            )
                        )
                    ),
                    "US" to mapOf(
                        "link" to "https://www.themoviedb.org/movie/$movieId/watch?locale=US",
                        "flatrate" to listOf(
                            mapOf(
                                "provider_id" to 8,
                                "provider_name" to "Netflix",
                                "logo_path" to "/t2yyOv40HZeVlLjYsCsPHnWLk4W.jpg"
                            )
                        )
                    )
                )
                // Case 2: Only US providers available (fallback scenario)
                1 -> mapOf(
                    "US" to mapOf(
                        "link" to "https://www.themoviedb.org/movie/$movieId/watch?locale=US",
                        "flatrate" to listOf(
                            mapOf(
                                "provider_id" to 8,
                                "provider_name" to "Netflix",
                                "logo_path" to "/t2yyOv40HZeVlLjYsCsPHnWLk4W.jpg"
                            ),
                            mapOf(
                                "provider_id" to 15,
                                "provider_name" to "Hulu",
                                "logo_path" to "/gNbdjDi1HamTCrfvM9JeA94bNi2.jpg"
                            )
                        ),
                        "rent" to listOf(
                            mapOf(
                                "provider_id" to 10,
                                "provider_name" to "Amazon Video",
                                "logo_path" to "/seGSXajazLMCKGB5hnRCidtjay1.jpg"
                            )
                        )
                    ),
                    "GB" to mapOf(
                        "flatrate" to listOf(
                            mapOf(
                                "provider_id" to 8,
                                "provider_name" to "Netflix",
                                "logo_path" to "/t2yyOv40HZeVlLjYsCsPHnWLk4W.jpg"
                            )
                        )
                    )
                )
                // Case 3: Random other regions (fallback to first available)
                else -> mapOf(
                    "CA" to mapOf(
                        "link" to "https://www.themoviedb.org/movie/$movieId/watch?locale=CA",
                        "flatrate" to listOf(
                            mapOf(
                                "provider_id" to 230,
                                "provider_name" to "Crave",
                                "logo_path" to "/5NyLm42TmCqCMOZFvH4fcoSNKEW.jpg"
                            )
                        )
                    ),
                    "DE" to mapOf(
                        "flatrate" to listOf(
                            mapOf(
                                "provider_id" to 8,
                                "provider_name" to "Netflix",
                                "logo_path" to "/t2yyOv40HZeVlLjYsCsPHnWLk4W.jpg"
                            )
                        )
                    )
                )
            }
        )
    }
}