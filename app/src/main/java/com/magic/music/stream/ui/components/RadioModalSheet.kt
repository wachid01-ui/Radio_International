package com.magic.music.stream.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.magic.music.stream.data.model.RadioStation
import com.magic.music.stream.ui.theme.AccentCyan
import com.magic.music.stream.ui.theme.AccentRose
import com.magic.music.stream.ui.theme.BackgroundDark
import com.magic.music.stream.ui.theme.PrimaryNeon
import com.magic.music.stream.ui.theme.SurfaceBorder
import com.magic.music.stream.ui.theme.SurfaceDark
import com.magic.music.stream.ui.theme.SurfaceVariantDark
import com.magic.music.stream.ui.theme.TextMuted
import com.magic.music.stream.ui.theme.TextPrimary
import com.magic.music.stream.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioModalSheet(
    station: RadioStation?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    liveTitle: String,
    sleepTimerMinutes: Int,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (station == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SurfaceDark, BackgroundDark)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Live On Air Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentRose.copy(alpha = 0.2f))
                        .border(1.dp, AccentRose.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentRose)
                            .alpha(if (isPlaying) pulseAlpha else 0.4f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE ON AIR",
                        color = AccentRose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Sleep Timer Toggle Button
                val nextTimer = when (sleepTimerMinutes) {
                    0 -> 15
                    15 -> 30
                    30 -> 60
                    else -> 0
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sleepTimerMinutes > 0) PrimaryNeon else SurfaceVariantDark)
                        .clickable { onSetSleepTimer(nextTimer) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerMinutes > 0) TextPrimary else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        if (sleepTimerMinutes > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${sleepTimerMinutes}m",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large Station Logo Box
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .shadow(20.dp, RoundedCornerShape(28.dp), ambientColor = PrimaryNeon, spotColor = AccentCyan)
                    .background(SurfaceVariantDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (station.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = station.logoUrl,
                        contentDescription = station.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Station Name with Favorite Heart Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = station.name,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Hapus Favorit" else "Tambah Favorit",
                        tint = if (isFavorite) AccentRose else TextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = station.cleanTags.ifBlank { "${station.country} • Online Radio" },
                color = AccentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Now Playing (Live ICY Song Title) Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark.copy(alpha = 0.8f))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (liveTitle.isNotBlank()) "🎵 $liveTitle" else "Siaran Langsung Radio Digital",
                    color = if (liveTitle.isNotBlank()) TextPrimary else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = if (liveTitle.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Player Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Station",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause Circle Button
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(16.dp, CircleShape, ambientColor = PrimaryNeon, spotColor = PrimaryNeon)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryNeon, AccentRose)
                            ),
                            shape = CircleShape
                        )
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = TextPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Station",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tech Specs Badge (Codec & Bitrate)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${station.codec.uppercase()} ${station.bitrate.coerceAtLeast(64)} kbps • Low Latency Stream",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
