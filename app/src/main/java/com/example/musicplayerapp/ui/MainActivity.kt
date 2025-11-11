package com.example.musicplayerapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface

import androidx.compose.ui.draw.clip

import androidx.compose.foundation.layout.defaultMinSize

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.musicplayerapp.data.model.Track

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MusicPlayerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Add this annotation
@Composable
fun MusicPlayerScreen(musicViewModel: MusicViewModel = viewModel()) {
    val uiState by musicViewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Music Player App") },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            if (uiState.currentTrack != null) {
                PlayerControls(
                    track = uiState.currentTrack!!,
                    isPlaying = uiState.isPlaying,
                    isSongLoading = uiState.isSongLoading,
                    currentPosition = uiState.currentPosition,
                    onPlayPauseToggle = {
                        if (uiState.isPlaying) {
                            musicViewModel.pauseTrack()
                        } else {
                            musicViewModel.resumeTrack()
                        }
                    },
                    onSeek = { newPosition -> musicViewModel.seekTo(newPosition) }
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Loading State for the list
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Error State with Retry
            else if (uiState.error != null) {
                ErrorState(
                    errorMessage = uiState.error!!,
                    onRetry = { musicViewModel.fetchMusic() }
                )
            }

            // Content Loaded State
            else {
                // Use Column to hold Sort and List
                Column(modifier = Modifier.fillMaxSize()) {
                    SortControls(
                        selectedType = uiState.sortType,
                        onSortSelected = { musicViewModel.sortTracks(it) }
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.tracks) { track ->
                            TrackItem(
                                track = track,
                                isSelected = (track.id == uiState.currentTrack?.id),
                                onClick = { musicViewModel.playTrack(track) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = "No Internet",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItem(track: Track, isSelected: Boolean, onClick: () -> Unit) {

    val cardColors = CardDefaults.cardColors(
        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "Text Color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = cardColors
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(track.thumbnail),
                contentDescription = track.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = textColor
                )
                Text(
                    track.artist,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f), // Make artist less prominent
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(16.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Now Playing",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = formatDuration(track.duration),
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortControls(selectedType: SortType, onSortSelected: (SortType) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedType == SortType.BY_NAME,
                onClick = { onSortSelected(SortType.BY_NAME) },
                label = { Text("Sort by Name (A-Z)") }
            )
        }
        item {
            FilterChip(
                selected = selectedType == SortType.BY_DURATION,
                onClick = { onSortSelected(SortType.BY_DURATION) },
                label = { Text("Sort by Duration (Shortest)") }
            )
        }
    }
}

@Composable
fun PlayerControls(
    track: Track,
    isPlaying: Boolean,
    isSongLoading: Boolean,
    currentPosition: Int,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Float) -> Unit
) {

    var sliderPosition by remember { mutableStateOf(currentPosition.toFloat()) }
    var isUserSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition) {
        if (!isUserSeeking) {
            sliderPosition = currentPosition.toFloat()
        }
    }

    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(track.title, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(track.artist, fontSize = 14.sp)

            Slider(
                value = sliderPosition,
                onValueChange = { newValue ->
                    isUserSeeking = true
                    sliderPosition = newValue
                },
                onValueChangeFinished = {
                    onSeek(sliderPosition)
                    isUserSeeking = false
                },
                valueRange = 0f..track.duration.toFloat(),
                enabled = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatDuration(sliderPosition.toInt()))

                Box(
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSongLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Buffering...",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        IconButton(onClick = onPlayPauseToggle) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(48.dp) // Button is still 48dp
                            )
                        }
                    }
                }

                Text(formatDuration(track.duration))
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}