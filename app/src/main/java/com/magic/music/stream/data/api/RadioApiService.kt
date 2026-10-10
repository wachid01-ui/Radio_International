package com.magic.music.stream.data.api

import com.magic.music.stream.data.model.RadioStation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class RadioApiService {
    private val baseUrl = "https://de1.api.radio-browser.info/json"
    private val cache = ConcurrentHashMap<String, List<RadioStation>>()

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(6, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getTopIndonesiaStations(): List<RadioStation> {
        val cacheKey = "stations_id_top"
        cache[cacheKey]?.let { return it }

        return try {
            val response: List<RadioStation> = client.get("$baseUrl/stations/bycountry/Indonesia?order=votes&reverse=true&limit=80") {
                header("User-Agent", "MagicRadioApp/1.0.0")
            }.body()

            val filtered = response.filter { it.streamUrl.isNotBlank() && it.name.isNotBlank() }
            if (filtered.isNotEmpty()) {
                cache[cacheKey] = filtered
            }
            filtered
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback backup mirror
            getStationsFromMirror("stations/bycountry/Indonesia?order=votes&reverse=true&limit=80")
        }
    }

    suspend fun getStationsByCategory(category: String): List<RadioStation> {
        val cacheKey = "category_${category.lowercase()}"
        cache[cacheKey]?.let { return it }

        val tag = when (category.lowercase()) {
            "pop" -> "pop"
            "dangdut" -> "dangdut"
            "berita / news", "berita" -> "news"
            "religi / dakwah", "religi" -> "islamic"
            "rock" -> "rock"
            "jazz" -> "jazz"
            "chill / lofi", "chill" -> "chill"
            else -> ""
        }

        if (tag.isBlank()) return getTopIndonesiaStations()

        return try {
            val response: List<RadioStation> = client.get("$baseUrl/stations/bytag/$tag?order=votes&reverse=true&limit=50") {
                header("User-Agent", "MagicRadioApp/1.0.0")
            }.body()

            val filtered = response.filter { it.streamUrl.isNotBlank() && it.name.isNotBlank() }
            if (filtered.isNotEmpty()) {
                cache[cacheKey] = filtered
            }
            filtered
        } catch (e: Exception) {
            e.printStackTrace()
            getTopIndonesiaStations()
        }
    }

    suspend fun searchStations(query: String): List<RadioStation> {
        val clean = query.trim()
        if (clean.isBlank()) return getTopIndonesiaStations()

        val cacheKey = "search_${clean.lowercase()}"
        cache[cacheKey]?.let { return it }

        return try {
            val response: List<RadioStation> = client.get("$baseUrl/stations/byname/$clean?order=votes&reverse=true&limit=50") {
                header("User-Agent", "MagicRadioApp/1.0.0")
            }.body()

            val filtered = response.filter { it.streamUrl.isNotBlank() && it.name.isNotBlank() }
            if (filtered.isNotEmpty()) {
                cache[cacheKey] = filtered
            }
            filtered
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun getStationsFromMirror(path: String): List<RadioStation> {
        return try {
            val mirrorUrl = "https://nl1.api.radio-browser.info/json/$path"
            val response: List<RadioStation> = client.get(mirrorUrl) {
                header("User-Agent", "MagicRadioApp/1.0.0")
            }.body()
            response.filter { it.streamUrl.isNotBlank() && it.name.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun close() {
        client.close()
    }
}
