package com.example.musicplayerapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayerapp.MusicApiService
import com.example.musicplayerapp.data.network.NetworkConnectivityService
import com.example.musicplayerapp.data.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MusicPlayerState(
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val sortType: SortType = SortType.NONE,
    val isSongLoading: Boolean = false
)

enum class SortType { NONE, BY_NAME, BY_DURATION }

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = MusicApiService()

    private val _uiState = MutableStateFlow(MusicPlayerState())
    val uiState = _uiState.asStateFlow()

    private val networkService = NetworkConnectivityService(application)

    private var exoPlayer: ExoPlayer? = null
    private var positionTrackerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startPositionTracker()
            } else {
                positionTrackerJob?.cancel()
            }
        }

        // When song ends
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.update { it.copy(isSongLoading = true) }
                }
                Player.STATE_READY -> {
                    _uiState.update { it.copy(isSongLoading = false) }
                }
                Player.STATE_ENDED -> {
                    stopPlayback()
                    _uiState.update { it.copy(isSongLoading = false) }
                }
                Player.STATE_IDLE -> {
                    _uiState.update { it.copy(isSongLoading = false) }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            // Check for a network error
            val errorMessage = if (!networkService.isNetworkAvailable()) {
                "Playback failed: No internet connection."
            } else {
                "Playback error: ${error.message}"
            }
            _uiState.update { it.copy(error = errorMessage, isSongLoading = false) }
            stopPlayback()
        }
    }

    init {
        fetchMusic()
        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(application).build().apply {
            addListener(playerListener)
        }
    }

    fun fetchMusic() {
        // Check for internet BEFORE trying to fetch
        if (!networkService.isNetworkAvailable()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "No internet connection. Please check your network."
                )
            }
            return
        }

        // Launch the coroutine
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Added a try-catch to handle errors from the ApiService
            try {
                val tracks = apiService.fetchTracks()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tracks = tracks,
                        error = if (tracks.isEmpty()) "No tracks found" else null
                    )
                }
            } catch (e: Exception) { // Catches Ktor/Network errors
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load tracks. Please try again later."
                    )
                }
            }
        }
    }

    fun sortTracks(sortType: SortType) {
        _uiState.update { state ->
            val sortedTracks = when (sortType) {
                SortType.BY_NAME -> state.tracks.sortedBy { it.title }
                SortType.BY_DURATION -> state.tracks.sortedBy { it.duration }
                SortType.NONE -> state.tracks
            }
            state.copy(tracks = sortedTracks, sortType = sortType)
        }
    }

    fun playTrack(track: Track) {
        stopPlayback()

        _uiState.update { it.copy(
            currentTrack = track,
            currentPosition = 0,
            isSongLoading = true,
            error = null
        ) }

        exoPlayer?.apply {
            val mediaItem = MediaItem.fromUri(track.audioUrl)
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun pauseTrack() {
        exoPlayer?.pause()
    }

    fun resumeTrack() {
        exoPlayer?.play()
    }

    private fun stopPlayback() {
        exoPlayer?.stop()
        positionTrackerJob?.cancel()
        _uiState.update { it.copy(isPlaying = false, currentPosition = 0) }
    }

    fun seekTo(positionSeconds: Float) {
        val positionMs = (positionSeconds * 1000).toLong()
        exoPlayer?.seekTo(positionMs)
        _uiState.update { it.copy(currentPosition = positionSeconds.toInt()) }
    }

    private fun startPositionTracker() {
        positionTrackerJob?.cancel()
        positionTrackerJob = viewModelScope.launch {
            while (_uiState.value.isPlaying && exoPlayer != null) {
                try {
                    _uiState.update { it.copy(currentPosition = (exoPlayer!!.currentPosition / 1000).toInt()) }
                } catch (e: IllegalStateException) {
                    break
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        exoPlayer = null
    }
}