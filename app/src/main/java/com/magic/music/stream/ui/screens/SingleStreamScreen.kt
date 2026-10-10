package com.magic.music.stream.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magic.music.stream.ui.components.MiniPlayer
import com.magic.music.stream.ui.components.PlayerModalSheet
import com.magic.music.stream.ui.components.TrackItem
import com.magic.music.stream.ui.theme.AccentCyan
import com.magic.music.stream.ui.theme.AccentRose
import com.magic.music.stream.ui.theme.BackgroundDark
import com.magic.music.stream.ui.theme.PrimaryGlow
import com.magic.music.stream.ui.theme.PrimaryNeon
import com.magic.music.stream.ui.theme.SurfaceBorder
import com.magic.music.stream.ui.theme.SurfaceDark
import com.magic.music.stream.ui.theme.TextMuted
import com.magic.music.stream.ui.theme.TextPrimary
import com.magic.music.stream.ui.theme.TextSecondary
import com.magic.music.stream.ui.viewmodel.MusicViewModel

@Composable
fun SingleStreamScreen(viewModel: MusicViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTrack by viewModel.playerController.currentTrack.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()
    val currentPos by viewModel.playerController.currentPosition.collectAsState()
    val duration by viewModel.playerController.duration.collectAsState()
    val isRepeat by viewModel.playerController.isRepeat.collectAsState()
    val isShuffle by viewModel.playerController.isShuffle.collectAsState()

    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = BackgroundDark,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Top App Header
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryNeon, AccentCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Logo",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MAGIC STREAM",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Instant YouTube Audio Player",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modern Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    placeholder = {
                        Text(
                            text = "Cari lagu, artis, atau album...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AccentCyan
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Genre Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(viewModel.genres) { genre ->
                        val isSelected = uiState.selectedGenre == genre && !uiState.isSearching
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryNeon else SurfaceDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryGlow else SurfaceBorder,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.onGenreSelected(genre) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = genre,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Song List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.isSearching) "Hasil Pencarian" else "Top Trending Hits",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!uiState.isSearching) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.loadTrending() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = AccentRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live Charts",
                                color = AccentRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Track List & State
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else if (uiState.tracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada lagu ditemukan. Silakan cari lagu lain.",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = if (currentTrack != null) 90.dp else 16.dp)
                    ) {
                        items(uiState.tracks, key = { it.id }) { track ->
                            val isCurrent = currentTrack?.id == track.id
                            TrackItem(
                                track = track,
                                isPlaying = isPlaying,
                                isCurrent = isCurrent,
                                onClick = { viewModel.playTrack(track) }
                            )
                        }
                    }
                }
            }

            // Bottom Floating MiniPlayer
            AnimatedVisibility(
                visible = currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                currentTrack?.let { track ->
                    MiniPlayer(
                        track = track,
                        isPlaying = isPlaying,
                        currentPosition = currentPos,
                        duration = duration,
                        onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                        onNext = { viewModel.playerController.playNext() },
                        onClick = { viewModel.setFullPlayerVisible(true) }
                    )
                }
            }
        }

        // Full Player Modal Sheet
        if (uiState.isFullPlayerVisible) {
            PlayerModalSheet(
                track = currentTrack,
                isPlaying = isPlaying,
                currentPosition = currentPos,
                duration = duration,
                isRepeat = isRepeat,
                isShuffle = isShuffle,
                onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                onNext = { viewModel.playerController.playNext() },
                onPrevious = { viewModel.playerController.playPrevious() },
                onSeek = { viewModel.playerController.seekTo(it) },
                onToggleRepeat = { viewModel.playerController.toggleRepeat() },
                onToggleShuffle = { viewModel.playerController.toggleShuffle() },
                onDismiss = { viewModel.setFullPlayerVisible(false) }
            )
        }
    }
}
