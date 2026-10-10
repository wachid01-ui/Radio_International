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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magic.music.stream.ui.components.RadioMiniPlayer
import com.magic.music.stream.ui.components.RadioModalSheet
import com.magic.music.stream.ui.components.StationItem
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
import com.magic.music.stream.ui.viewmodel.RadioViewModel

@Composable
fun SingleRadioScreen(viewModel: RadioViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStation by viewModel.radioPlayer.currentStation.collectAsState()
    val isPlaying by viewModel.radioPlayer.isPlaying.collectAsState()
    val isBuffering by viewModel.radioPlayer.isBuffering.collectAsState()
    val liveTitle by viewModel.radioPlayer.liveTitle.collectAsState()
    val sleepTimer by viewModel.radioPlayer.sleepTimerMinutes.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

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
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryNeon, AccentCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = "Logo",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MAGIC RADIO",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Live Indonesia & Global Stations",
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
                            text = "Cari radio (Prambors, Gen FM, Delta, Dakwah...)",
                            color = TextMuted,
                            fontSize = 13.sp
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

                // Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(viewModel.categories) { category ->
                        val isSelected = uiState.selectedCategory == category && !uiState.isSearching
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryNeon else SurfaceDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryGlow else SurfaceBorder,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.onCategorySelected(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Station List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val headerTitle = when {
                        uiState.isSearching -> "Hasil Pencarian Radio"
                        uiState.selectedCategory == "⭐ Favorit" -> "Stasiun Favorit Saya (${uiState.stations.size})"
                        else -> "Stasiun Radio Terpopuler"
                    }
                    Text(
                        text = headerTitle,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!uiState.isSearching && uiState.selectedCategory != "⭐ Favorit") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.loadTopStations() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = AccentRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "FM Live",
                                color = AccentRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stations LazyColumn
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
                } else if (uiState.stations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.selectedCategory == "⭐ Favorit" && !uiState.isSearching) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = AccentRose.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum Ada Radio Favorit",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ketuk ikon hati ❤️ pada stasiun radio pilihan Anda untuk menyimpannya di sini.",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Text(
                                text = "Tidak ada stasiun radio ditemukan.",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = if (currentStation != null) 90.dp else 16.dp)
                    ) {
                        items(uiState.stations, key = { it.id }) { station ->
                            val isCurrent = currentStation?.id == station.id
                            val isFav = favoriteIds.contains(station.id)
                            StationItem(
                                station = station,
                                isPlaying = isPlaying,
                                isCurrent = isCurrent,
                                isBuffering = isBuffering && isCurrent,
                                isFavorite = isFav,
                                onToggleFavorite = { viewModel.toggleFavorite(station) },
                                onClick = { viewModel.playStation(station) }
                            )
                        }
                    }
                }
            }

            // Bottom Floating MiniPlayer
            AnimatedVisibility(
                visible = currentStation != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                currentStation?.let { station ->
                    RadioMiniPlayer(
                        station = station,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        liveTitle = liveTitle,
                        onTogglePlayPause = { viewModel.radioPlayer.togglePlayPause() },
                        onNext = { viewModel.radioPlayer.playNext() },
                        onClick = { viewModel.setFullPlayerVisible(true) }
                    )
                }
            }
        }

        // Full Player Modal Sheet
        if (uiState.isFullPlayerVisible) {
            val isCurrentFav = currentStation?.let { favoriteIds.contains(it.id) } ?: false
            RadioModalSheet(
                station = currentStation,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                liveTitle = liveTitle,
                sleepTimerMinutes = sleepTimer,
                isFavorite = isCurrentFav,
                onToggleFavorite = { currentStation?.let { viewModel.toggleFavorite(it) } },
                onTogglePlayPause = { viewModel.radioPlayer.togglePlayPause() },
                onNext = { viewModel.radioPlayer.playNext() },
                onPrevious = { viewModel.radioPlayer.playPrevious() },
                onSetSleepTimer = { viewModel.radioPlayer.setSleepTimer(it) },
                onDismiss = { viewModel.setFullPlayerVisible(false) }
            )
        }
    }
}
