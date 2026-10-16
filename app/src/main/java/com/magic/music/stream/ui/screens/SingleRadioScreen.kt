package com.magic.music.stream.ui.screens

import android.app.Activity
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magic.music.stream.R
import com.magic.music.stream.ads.AdaptiveBannerAd
import com.magic.music.stream.ads.InterstitialAdManager
import com.magic.music.stream.ui.components.CountryPickerModal
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

private data class NavCategory(
    val key: String,
    val label: String
)

@Composable
fun SingleRadioScreen(viewModel: RadioViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStation by viewModel.radioPlayer.currentStation.collectAsState()
    val isPlaying by viewModel.radioPlayer.isPlaying.collectAsState()
    val isBuffering by viewModel.radioPlayer.isBuffering.collectAsState()
    val liveTitle by viewModel.radioPlayer.liveTitle.collectAsState()
    val sleepTimer by viewModel.radioPlayer.sleepTimerMinutes.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playbackError by viewModel.radioPlayer.playbackError.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    val interstitialAdManager = remember { InterstitialAdManager(context) }

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
                                text = stringResource(R.string.app_header_title),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = stringResource(R.string.app_subtitle),
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Country Switcher Pill Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setCountryPickerVisible(true) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.selectedCountry.flag,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.selectedCountry.code,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.country_picker_title),
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
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
                            text = stringResource(R.string.search_placeholder),
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

                // Category Chips (Dynamic with Country & Global)
                val dynamicCategories = listOf(
                    NavCategory("global", stringResource(R.string.cat_global_hits)),
                    NavCategory("favorites", stringResource(R.string.cat_favorites)),
                    NavCategory("country", "${uiState.selectedCountry.flag} ${uiState.selectedCountry.name}"),
                    NavCategory("pop", stringResource(R.string.cat_pop)),
                    NavCategory("dangdut", stringResource(R.string.cat_dangdut)),
                    NavCategory("news", stringResource(R.string.cat_news)),
                    NavCategory("spiritual", stringResource(R.string.cat_spiritual)),
                    NavCategory("rock", stringResource(R.string.cat_rock)),
                    NavCategory("jazz", stringResource(R.string.cat_jazz)),
                    NavCategory("chill", stringResource(R.string.cat_chill)),
                    NavCategory("dance", stringResource(R.string.cat_dance))
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(dynamicCategories, key = { it.key }) { category ->
                        val isSelected = (uiState.selectedCategory == category.key ||
                                uiState.selectedCategory == category.label) && !uiState.isSearching

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryNeon else SurfaceDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryGlow else SurfaceBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.onCategorySelected(category.key)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category.label,
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
                        uiState.isSearching -> stringResource(R.string.header_search_results)
                        uiState.selectedCategory in listOf("favorites", "⭐ Favorit", "⭐ Favorites") ->
                            stringResource(R.string.header_favorites, uiState.stations.size)
                        uiState.selectedCategory in listOf("country", "🏠 Negara Saya", "🏠 My Country") ->
                            stringResource(R.string.header_country_stations, uiState.selectedCountry.name)
                        uiState.selectedCategory in listOf("global", "🌍 Global Hits") ->
                            stringResource(R.string.header_global_hits)
                        else -> {
                            val catLabel = dynamicCategories.find { it.key == uiState.selectedCategory }?.label ?: uiState.selectedCategory
                            stringResource(R.string.header_category_stations, catLabel)
                        }
                    }
                    Text(
                        text = headerTitle,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (!uiState.isSearching && uiState.selectedCategory !in listOf("favorites", "⭐ Favorit", "⭐ Favorites")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.onCategorySelected(uiState.selectedCategory) }
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
                        if (uiState.selectedCategory in listOf("favorites", "⭐ Favorit", "⭐ Favorites") && !uiState.isSearching) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = AccentRose.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.empty_fav_title),
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.empty_fav_desc),
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.empty_search_title),
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.empty_search_desc),
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = if (currentStation != null) 160.dp else 85.dp)
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
                                onClick = {
                                    if (activity != null) {
                                        interstitialAdManager.showIfReady(
                                            activity = activity,
                                            onAdBeforeShow = {
                                                if (isPlaying) viewModel.radioPlayer.togglePlayPause()
                                            },
                                            onProceedAction = {
                                                viewModel.playStation(station)
                                            }
                                        )
                                    } else {
                                        viewModel.playStation(station)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Playback Error Banner
            AnimatedVisibility(
                visible = playbackError != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (currentStation != null) 160.dp else 85.dp, start = 16.dp, end = 16.dp)
            ) {
                playbackError?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentRose.copy(alpha = 0.25f))
                            .border(1.dp, AccentRose.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AccentRose,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = msg,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }
                        IconButton(
                            onClick = { viewModel.radioPlayer.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Container: Floating MiniPlayer + Adaptive Banner Ad
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // Floating MiniPlayer
                AnimatedVisibility(
                    visible = currentStation != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    currentStation?.let { station ->
                        RadioMiniPlayer(
                            station = station,
                            isPlaying = isPlaying,
                            isBuffering = isBuffering,
                            liveTitle = liveTitle,
                            onTogglePlayPause = { viewModel.radioPlayer.togglePlayPause() },
                            onNext = {
                                if (activity != null) {
                                    interstitialAdManager.showIfReady(
                                        activity = activity,
                                        onAdBeforeShow = { if (isPlaying) viewModel.radioPlayer.togglePlayPause() },
                                        onProceedAction = { viewModel.radioPlayer.playNext() }
                                    )
                                } else {
                                    viewModel.radioPlayer.playNext()
                                }
                            },
                            onClick = { viewModel.setFullPlayerVisible(true) }
                        )
                    }
                }

                // Adaptive Banner Ad (Anchored at the bottom)
                AdaptiveBannerAd(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark)
                )
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
                onNext = {
                    if (activity != null) {
                        interstitialAdManager.showIfReady(
                            activity = activity,
                            onAdBeforeShow = { if (isPlaying) viewModel.radioPlayer.togglePlayPause() },
                            onProceedAction = { viewModel.radioPlayer.playNext() }
                        )
                    } else {
                        viewModel.radioPlayer.playNext()
                    }
                },
                onPrevious = {
                    if (activity != null) {
                        interstitialAdManager.showIfReady(
                            activity = activity,
                            onAdBeforeShow = { if (isPlaying) viewModel.radioPlayer.togglePlayPause() },
                            onProceedAction = { viewModel.radioPlayer.playPrevious() }
                        )
                    } else {
                        viewModel.radioPlayer.playPrevious()
                    }
                },
                onSetSleepTimer = { viewModel.radioPlayer.setSleepTimer(it) },
                onDismiss = { viewModel.setFullPlayerVisible(false) }
            )
        }

        // Country Picker Modal
        if (uiState.isCountryPickerVisible) {
            CountryPickerModal(
                currentCountry = uiState.selectedCountry,
                countries = viewModel.popularCountries,
                onSelectCountry = { viewModel.selectCountry(it) },
                onDismiss = { viewModel.setCountryPickerVisible(false) }
            )
        }
    }
}
