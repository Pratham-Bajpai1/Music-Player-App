package com.example.musicplayerapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JamendoResponse(
    val results: List<Track>
)

@Serializable
data class Track(
    @SerialName("id") val id: String,
    @SerialName("name") val title: String,
    @SerialName("artist_name") val artist: String,
    @SerialName("duration") val duration: Int,
    @SerialName("image") val thumbnail: String,
    @SerialName("audio") val audioUrl: String
)