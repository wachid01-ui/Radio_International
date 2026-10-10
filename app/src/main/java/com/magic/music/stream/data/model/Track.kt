package com.magic.music.stream.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "Magic Stream",
    val durationMs: Long = 210_000L,
    val mediaUri: String = "",
    val artworkUri: String? = null,
    val videoId: String = id
) {
    val thumbnailUrl: String
        get() = artworkUri ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

    fun formattedDuration(): String {
        val totalSec = durationMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }
}
