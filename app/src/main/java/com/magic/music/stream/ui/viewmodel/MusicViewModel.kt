package com.magic.music.stream.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.magic.music.stream.data.api.YouTubeMusicService
import com.magic.music.stream.data.model.Track
import com.magic.music.stream.player.StreamPlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedGenre: String = "Trending",
    val tracks: List<Track> = emptyList(),
    val trendingTracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFullPlayerVisible: Boolean = false
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = YouTubeMusicService()
    val playerController = StreamPlayerController()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    val genres = listOf("Trending", "Pop", "Rock", "Lofi & Chill", "EDM", "R&B / Soul", "Hip Hop", "Acoustic", "Jazz")

    init {
        loadTrending()
    }

    fun loadTrending() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val trending = apiService.fetchTrendingMusic()
                _uiState.value = _uiState.value.copy(
                    trendingTracks = trending,
                    tracks = trending,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat lagu trending: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onGenreSelected(genre: String) {
        _uiState.value = _uiState.value.copy(selectedGenre = genre)
        if (genre == "Trending") {
            if (_uiState.value.trendingTracks.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(tracks = _uiState.value.trendingTracks)
            } else {
                loadTrending()
            }
        } else {
            searchSongs("$genre songs music")
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                tracks = if (_uiState.value.trendingTracks.isNotEmpty()) _uiState.value.trendingTracks else emptyList()
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce
            searchSongs(query)
        }
    }

    private fun searchSongs(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSearching = true, errorMessage = null)
            try {
                val results = apiService.searchTracks(query)
                _uiState.value = _uiState.value.copy(
                    tracks = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Pencarian gagal: ${e.localizedMessage}"
                )
            }
        }
    }

    fun playTrack(track: Track) {
        val currentList = _uiState.value.tracks
        playerController.playTrack(track, currentList)
    }

    fun setFullPlayerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isFullPlayerVisible = visible)
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
        apiService.close()
    }
}
