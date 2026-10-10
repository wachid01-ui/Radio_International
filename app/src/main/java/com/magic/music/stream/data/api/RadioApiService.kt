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

    suspend fun getGlobalTopStations(): List<RadioStation> {
        val cacheKey = "stations_global_top"
        cache[cacheKey]?.let { return it }

        return try {
            val response: List<RadioStation> = client.get("$baseUrl/stations/topclick/80") {
                header("User-Agent", "MagicRadioApp/1.0.0")
            }.body()

            val filtered = response.filter { it.streamUrl.isNotBlank() && it.name.isNotBlank() }
            if (filtered.isNotEmpty()) {
                cache[cacheKey] = filtered
            }
            filtered
        } catch (e: Exception) {
            e.printStackTrace()
            getStationsFromMirror("stations/topclick/80")
        }
    }

    suspend fun getTopStationsByCountry(countryCode: String): List<RadioStation> {
        val code = countryCode.trim().uppercase().ifBlank { "ID" }
        val cacheKey = "stations_country_${code}"
        cache[cacheKey]?.let { return it }

        return try {
            val response: List<RadioStation> = client.get("$baseUrl/stations/bycountrycodeexact/$code?order=votes&reverse=true&limit=80") {
                header("User-Agent", "MagicRadioApp/1.0.0")
            }.body()

            val filtered = response.filter { it.streamUrl.isNotBlank() && it.name.isNotBlank() }
            if (filtered.isNotEmpty()) {
                cache[cacheKey] = filtered
                filtered
            } else {
                // If specific country returns empty, fallback to global top
                getGlobalTopStations()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getStationsFromMirror("stations/bycountrycodeexact/$code?order=votes&reverse=true&limit=80")
        }
    }

    suspend fun getStationsByCategory(category: String, countryCode: String? = null): List<RadioStation> {
        val cacheKey = "category_${category.lowercase()}_${countryCode ?: "all"}"
        cache[cacheKey]?.let { return it }

        val tag = when (category.lowercase()) {
            "pop" -> "pop"
            "dangdut" -> "dangdut"
            "berita / news", "berita", "news" -> "news"
            "religi / dakwah", "religi" -> "islamic"
            "rock" -> "rock"
            "jazz" -> "jazz"
            "chill / lofi", "chill" -> "chill"
            "dance / edm" -> "dance"
            "classical" -> "classical"
            else -> category.lowercase()
        }

        return try {
            val url = "$baseUrl/stations/bytag/$tag?order=votes&reverse=true&limit=60"
            val response: List<RadioStation> = client.get(url) {
                header("User-Agent", "MagicRadioApp/1.0.0")
            }.body()

            val filtered = response.filter { it.streamUrl.isNotBlank() && it.name.isNotBlank() }
            if (filtered.isNotEmpty()) {
                cache[cacheKey] = filtered
            }
            filtered
        } catch (e: Exception) {
            e.printStackTrace()
            getGlobalTopStations()
        }
    }

    suspend fun searchStations(query: String): List<RadioStation> {
        val clean = query.trim()
        if (clean.isBlank()) return getGlobalTopStations()

        val cacheKey = "search_${clean.lowercase()}"
        cache[cacheKey]?.let { return it }

        return try {
            val response: List<RadioStation> = client.get("$baseUrl/stations/byname/$clean?order=votes&reverse=true&limit=60") {
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
