package com.example.musicplayerapp

import com.example.musicplayerapp.data.model.JamendoResponse
import com.example.musicplayerapp.data.model.Track
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MusicApiService {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val clientId = "88c0572f" // JAMENDO CLIENT ID
    private val baseUrl = "https://api.jamendo.com/v3.0/tracks/?client_id=$clientId&format=json&limit=20"

    suspend fun fetchTracks(): List<Track> {
        val response: JamendoResponse = httpClient.get(baseUrl).body()
        return response.results
    }
}