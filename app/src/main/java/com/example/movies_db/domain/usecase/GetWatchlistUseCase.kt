package com.example.movies_db.domain.usecase

import com.example.movies_db.domain.model.Movie
import com.example.movies_db.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlistUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<List<Movie>> = repository.getWatchlist()
}