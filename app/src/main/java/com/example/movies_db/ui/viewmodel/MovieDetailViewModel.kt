package com.example.movies_db.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movies_db.domain.model.WatchProviders
import com.example.movies_db.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _watchProviders = MutableStateFlow<WatchProviders?>(null)
    val watchProviders: StateFlow<WatchProviders?> = _watchProviders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadWatchProviders(movieId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val providers = repository.getWatchProviders(movieId)
                _watchProviders.value = providers
            } catch (e: Exception) {
                _watchProviders.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}