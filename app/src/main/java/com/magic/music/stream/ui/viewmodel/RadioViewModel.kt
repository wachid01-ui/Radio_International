package com.magic.music.stream.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.magic.music.stream.data.api.RadioApiService
import com.magic.music.stream.data.model.RadioStation
import com.magic.music.stream.data.repository.FavoritesRepository
import com.magic.music.stream.player.RadioPlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class CountryInfo(
    val code: String,
    val name: String,
    val flag: String
)

data class RadioUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedCategory: String = "🌍 Global Hits",
    val selectedCountry: CountryInfo = CountryInfo("ID", "Indonesia", "🇮🇩"),
    val stations: List<RadioStation> = emptyList(),
    val topStations: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFullPlayerVisible: Boolean = false,
    val isCountryPickerVisible: Boolean = false
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RadioApiService()
    val radioPlayer = RadioPlayerController(application)
    val favoritesRepo = FavoritesRepository(application)
    private val prefs = application.getSharedPreferences("magic_radio_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = favoritesRepo.favoriteIds
    val favoriteStations: StateFlow<List<RadioStation>> = favoritesRepo.favorites

    private var searchJob: Job? = null

    val popularCountries = listOf(
        CountryInfo("ID", "Indonesia", "🇮🇩"),
        CountryInfo("MY", "Malaysia", "🇲🇾"),
        CountryInfo("SG", "Singapore", "🇸🇬"),
        CountryInfo("US", "United States", "🇺🇸"),
        CountryInfo("GB", "United Kingdom", "🇬🇧"),
        CountryInfo("JP", "Japan", "🇯🇵"),
        CountryInfo("KR", "South Korea", "🇰🇷"),
        CountryInfo("AU", "Australia", "🇦🇺"),
        CountryInfo("SA", "Saudi Arabia", "🇸🇦"),
        CountryInfo("TR", "Turkey", "🇹🇷"),
        CountryInfo("DE", "Germany", "🇩🇪"),
        CountryInfo("FR", "France", "🇫🇷"),
        CountryInfo("IN", "India", "🇮🇳"),
        CountryInfo("BR", "Brazil", "🇧🇷"),
        CountryInfo("PH", "Philippines", "🇵🇭")
    )

    val categories = listOf(
        "🌍 Global Hits",
        "⭐ Favorites",
        "🏠 My Country",
        "Pop",
        "Dangdut",
        "News / Talk",
        "Spiritual / Faith",
        "Rock",
        "Jazz",
        "Chill / Lofi",
        "Dance / EDM"
    )

    init {
        detectAndInitCountry()
        loadInitialStations()

        // Sync favorite stations dynamically if viewing Favorites tab
        viewModelScope.launch {
            favoriteStations.collect { favs ->
                if ((_uiState.value.selectedCategory == "favorites" || _uiState.value.selectedCategory == "⭐ Favorit" || _uiState.value.selectedCategory == "⭐ Favorites") && !_uiState.value.isSearching) {
                    _uiState.value = _uiState.value.copy(stations = favs)
                }
            }
        }
    }

    private fun detectAndInitCountry() {
        val savedCode = prefs.getString("saved_country_code", null)
        val savedName = prefs.getString("saved_country_name", null)
        val savedFlag = prefs.getString("saved_country_flag", null)

        if (!savedCode.isNullOrBlank() && !savedName.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                selectedCountry = CountryInfo(savedCode, savedName, savedFlag ?: "🌍")
            )
            return
        }

        // Auto-detect from system locale
        try {
            val locale = Locale.getDefault()
            val code = locale.country.uppercase()
            val name = locale.displayCountry.ifBlank { "Indonesia" }
            val flag = countryCodeToEmoji(code)

            val matched = popularCountries.find { it.code.equals(code, ignoreCase = true) }
                ?: CountryInfo(if (code.isNotBlank()) code else "ID", name, flag)

            _uiState.value = _uiState.value.copy(selectedCountry = matched)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(selectedCountry = CountryInfo("ID", "Indonesia", "🇮🇩"))
        }
    }

    private fun countryCodeToEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌍"
        val firstChar = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    private fun loadInitialStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Default landing: Global Top Hits
                val list = apiService.getGlobalTopStations()
                _uiState.value = _uiState.value.copy(
                    topStations = list,
                    stations = list,
                    selectedCategory = "global",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load radio stations: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        when (category) {
            "global", "🌍 Global Hits" -> {
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    val list = apiService.getGlobalTopStations()
                    _uiState.value = _uiState.value.copy(stations = list, isLoading = false)
                }
            }
            "favorites", "⭐ Favorit", "⭐ Favorites" -> {
                _uiState.value = _uiState.value.copy(stations = favoritesRepo.favorites.value)
            }
            "country", "🏠 Negara Saya", "🏠 My Country" -> {
                loadCountryStations(_uiState.value.selectedCountry.code)
            }
            else -> {
                val tag = when (category.lowercase()) {
                    "news" -> "news"
                    "spiritual" -> "islamic"
                    "chill" -> "chill"
                    "dance" -> "dance"
                    else -> category
                }
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    val list = apiService.getStationsByCategory(tag, _uiState.value.selectedCountry.code)
                    _uiState.value = _uiState.value.copy(stations = list, isLoading = false)
                }
            }
        }
    }

    fun selectCountry(country: CountryInfo) {
        _uiState.value = _uiState.value.copy(
            selectedCountry = country,
            isCountryPickerVisible = false,
            selectedCategory = "country"
        )
        prefs.edit()
            .putString("saved_country_code", country.code)
            .putString("saved_country_name", country.name)
            .putString("saved_country_flag", country.flag)
            .apply()

        loadCountryStations(country.code)
    }

    private fun loadCountryStations(countryCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val list = apiService.getTopStationsByCountry(countryCode)
            _uiState.value = _uiState.value.copy(stations = list, isLoading = false)
        }
    }

    fun setCountryPickerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isCountryPickerVisible = visible)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                stations = when (_uiState.value.selectedCategory) {
                    "favorites", "⭐ Favorites", "⭐ Favorit" -> favoritesRepo.favorites.value
                    else -> if (_uiState.value.topStations.isNotEmpty()) _uiState.value.topStations else emptyList()
                }
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

    fun toggleFavorite(station: RadioStation) {
        favoritesRepo.toggleFavorite(station)
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
