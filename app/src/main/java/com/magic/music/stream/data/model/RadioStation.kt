package com.magic.music.stream.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RadioStation(
    @SerialName("stationuuid")
    val id: String,
    val name: String,
    val url: String,
    @SerialName("url_resolved")
    val urlResolved: String = "",
    val homepage: String = "",
    val favicon: String = "",
    val tags: String = "",
    val country: String = "Indonesia",
    @SerialName("countrycode")
    val countryCode: String = "ID",
    val state: String = "",
    val language: String = "indonesian",
    val votes: Int = 0,
    val codec: String = "MP3",
    val bitrate: Int = 128,
    @SerialName("lastcheckok")
    val lastCheckOk: Int = 1,
    @SerialName("hls")
    val hls: Int = 0
) {
    val streamUrl: String
        get() = urlResolved.ifBlank { url }

    val logoUrl: String
        get() = favicon.ifBlank { "" }

    val cleanTags: String
        get() = tags.split(",").take(3).joinToString(" • ") { it.trim().replaceFirstChar { c -> c.uppercase() } }
}
