package com.example.movies_db.domain.usecase

import com.example.movies_db.domain.model.Genre
import com.example.movies_db.domain.repository.MovieRepository
import javax.inject.Inject

class GetGenresUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(): List<Genre> {
        return movieRepository.getGenres()
    }
}