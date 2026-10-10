package com.magic.music.stream.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.magic.music.stream.data.api.RadioApiService
import com.magic.music.stream.data.model.RadioStation
import com.magic.music.stream.player.RadioPlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RadioUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedCategory: String = "Semua",
    val stations: List<RadioStation> = emptyList(),
    val topStations: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFullPlayerVisible: Boolean = false
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RadioApiService()
    val radioPlayer = RadioPlayerController(application)

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    val categories = listOf("Semua", "Pop", "Dangdut", "Berita / News", "Religi / Dakwah", "Rock", "Jazz", "Chill / Lofi")

    init {
        loadTopStations()
    }

    fun loadTopStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val list = apiService.getTopIndonesiaStations()
                _uiState.value = _uiState.value.copy(
                    topStations = list,
                    stations = list,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat stasiun radio: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        if (category == "Semua") {
            if (_uiState.value.topStations.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(stations = _uiState.value.topStations)
            } else {
                loadTopStations()
            }
        } else {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val list = apiService.getStationsByCategory(category)
                _uiState.value = _uiState.value.copy(stations = list, isLoading = false)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                stations = if (_uiState.value.topStations.isNotEmpty()) _uiState.value.topStations else emptyList()
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.value = _uiState.value.copy(isLoading = true, isSearching = true)
            val list = apiService.searchStations(query)
            _uiState.value = _uiState.value.copy(stations = list, isLoading = false)
        }
    }

    fun playStation(station: RadioStation) {
        radioPlayer.playStation(station, _uiState.value.stations)
    }

    fun setFullPlayerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isFullPlayerVisible = visible)
    }

    override fun onCleared() {
        super.onCleared()
        radioPlayer.release()
        apiService.close()
    }
}
