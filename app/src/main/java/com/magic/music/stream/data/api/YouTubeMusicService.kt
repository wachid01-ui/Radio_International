package com.magic.music.stream.data.api

import com.magic.music.stream.data.model.Track
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class YouTubeMusicService {
    private val cache = ConcurrentHashMap<String, List<Track>>()

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(5, TimeUnit.SECONDS)
                readTimeout(8, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchTrendingMusic(): List<Track> {
        val cacheKey = "trending_home"
        cache[cacheKey]?.let { return it }

        return try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20240726.00.00")
                        put("hl", "id")
                        put("gl", "ID")
                    })
                })
                put("query", "top hits lagu indonesia viral terbaru audio")
            }

            val rawJson: String = client.post("https://www.youtube.com/youtubei/v1/search") {
                contentType(ContentType.Application.Json)
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setBody(payload.toString())
            }.bodyAsText()

            val results = parseSearchResults(rawJson, 30)
            if (results.isNotEmpty()) {
                cache[cacheKey] = results
            }
            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchTracks(query: String): List<Track> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return fetchTrendingMusic()

        val cacheKey = "search_${cleanQuery.lowercase()}"
        cache[cacheKey]?.let { return it }

        return try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20240726.00.00")
                        put("hl", "id")
                        put("gl", "ID")
                    })
                })
                put("query", "$cleanQuery audio")
            }

            val rawJson: String = client.post("https://www.youtube.com/youtubei/v1/search") {
                contentType(ContentType.Application.Json)
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setBody(payload.toString())
            }.bodyAsText()

            val results = parseSearchResults(rawJson, 30)
            if (results.isNotEmpty()) {
                cache[cacheKey] = results
            }
            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseSearchResults(rawJson: String, limit: Int): List<Track> {
        val results = mutableListOf<Track>()
        try {
            val json = JSONObject(rawJson)
            val contents = json.optJSONObject("contents")
                ?.optJSONObject("twoColumnSearchResultsRenderer")
                ?.optJSONObject("primaryContents")
                ?.optJSONObject("sectionListRenderer")
                ?.optJSONArray("contents") ?: return emptyList()

            for (i in 0 until contents.length()) {
                val section = contents.optJSONObject(i)
                val items = section?.optJSONObject("itemSectionRenderer")?.optJSONArray("contents") ?: continue
                for (j in 0 until items.length()) {
                    val item = items.optJSONObject(j) ?: continue
                    val v = item.optJSONObject("videoRenderer") ?: continue
                    val vid = v.optString("videoId")
                    if (vid.isNullOrEmpty()) continue

                    val titleRaw = v.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "Track"
                    val artist = v.optJSONObject("ownerText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "Artis"
                    val durationText = v.optJSONObject("lengthText")?.optString("simpleText") ?: "3:30"

                    val thumbs = v.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                    val thumb = thumbs?.optJSONObject(thumbs.length() - 1)?.optString("url")
                        ?: "https://i.ytimg.com/vi/$vid/hqdefault.jpg"

                    val cleanTitle = titleRaw
                        .replace(Regex("(?i)\\((official.*|video|audio|lirik.*|lyrics.*|hq|hd)\\)"), "")
                        .replace(Regex("(?i)\\[(official.*|video|audio|lirik.*|lyrics.*|hq|hd)\\]"), "")
                        .trim()

                    val durationMs = parseDurationToMs(durationText)

                    results.add(
                        Track(
                            id = vid,
                            title = cleanTitle.ifBlank { titleRaw },
                            artist = artist,
                            album = "Full HQ",
                            durationMs = durationMs,
                            mediaUri = "https://www.youtube.com/watch?v=$vid",
                            artworkUri = thumb,
                            videoId = vid
                        )
                    )
                    if (results.size >= limit) break
                }
                if (results.size >= limit) break
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    private fun parseDurationToMs(durationStr: String): Long {
        return try {
            val parts = durationStr.split(":").map { it.trim().toLongOrNull() ?: 0L }
            when (parts.size) {
                1 -> parts[0] * 1000L
                2 -> (parts[0] * 60L + parts[1]) * 1000L
                3 -> (parts[0] * 3600L + parts[1] * 60L + parts[2]) * 1000L
                else -> 210_000L
            }
        } catch (e: Exception) {
            210_000L
        }
    }

    fun close() {
        client.close()
    }
}
