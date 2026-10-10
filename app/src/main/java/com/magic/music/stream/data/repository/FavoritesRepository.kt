package com.magic.music.stream.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.magic.music.stream.data.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FavoritesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _favorites = MutableStateFlow<List<RadioStation>>(emptyList())
    val favorites: StateFlow<List<RadioStation>> = _favorites.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val rawJson = prefs.getString(KEY_FAVORITES, null)
        if (!rawJson.isNullOrBlank()) {
            try {
                val list = json.decodeFromString<List<RadioStation>>(rawJson)
                _favorites.value = list
                _favoriteIds.value = list.map { it.id }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
                _favorites.value = emptyList()
                _favoriteIds.value = emptySet()
            }
        }
    }

    fun isFavorite(stationId: String): Boolean {
        return _favoriteIds.value.contains(stationId)
    }

    fun toggleFavorite(station: RadioStation) {
        val currentList = _favorites.value.toMutableList()
        val exists = currentList.any { it.id == station.id }

        if (exists) {
            currentList.removeAll { it.id == station.id }
        } else {
            currentList.add(0, station)
        }

        _favorites.value = currentList
        _favoriteIds.value = currentList.map { it.id }.toSet()

        saveFavorites(currentList)
    }

    private fun saveFavorites(list: List<RadioStation>) {
        try {
            val encoded = json.encodeToString(list)
            prefs.edit().putString(KEY_FAVORITES, encoded).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PREFS_NAME = "magic_radio_favorites"
        private const val KEY_FAVORITES = "favorite_stations"
    }
}
