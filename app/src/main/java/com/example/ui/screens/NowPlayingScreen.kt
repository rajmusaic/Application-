package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.LyricsView
import com.example.ui.theme.VibeBackground
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueDark
import com.example.ui.theme.VibeBlueLight
import com.example.ui.theme.VibeBorder
import com.example.ui.theme.VibeEmerald
import com.example.ui.theme.VibeSurface
import com.example.ui.theme.VibeSurfaceVariant
import com.example.ui.theme.VibeTextMuted
import com.example.ui.theme.VibeTextPrimary
import com.example.ui.theme.VibeTextSecondary
import com.example.ui.theme.VibeYellow
import com.example.ui.theme.VibeYellowContainer
import com.example.viewmodel.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    currentSong: Song?,
    isPlaying: Boolean,
    playbackPositionMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isLyricsMode: Boolean,
    autoScrollLyrics: Boolean,
    lyricsFontSize: Float,
    queueCount: Int,
    onCollapse: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleLyricsMode: () -> Unit,
    onOpenQueueSheet: () -> Unit,
    onOpenSmartMatchInspector: () -> Unit,
    onChooseLyricsFile: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onChangeFontSize: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    var isUserDraggingSlider by remember { mutableStateOf(false) }
    var sliderTempPosition by remember { mutableFloatStateOf(0f) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val currentPositionFormatted = remember(playbackPositionMs, isUserDraggingSlider, sliderTempPosition) {
        val ms = if (isUserDraggingSlider) sliderTempPosition.toLong() else playbackPositionMs
        val totalSec = ms / 1000
        String.format("%d:%02d", totalSec / 60, totalSec % 60)
    }

    val artworkSize by animateDpAsState(
        targetValue = if (isLyricsMode) 80.dp else 260.dp,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "artwork_size"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBackground)
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.size(44.dp).testTag("now_playing_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Minimize Player",
                        tint = VibeTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isLyricsMode) "LYRICS" else "NOW PLAYING",
                        color = VibeBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = currentSong.album,
                        color = VibeTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(44.dp).testTag("now_playing_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = VibeTextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(VibeSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Smart Lyrics Match Info", color = VibeTextPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.Lyrics, contentDescription = null, tint = VibeBlue) },
                            onClick = {
                                showMoreMenu = false
                                onOpenSmartMatchInspector()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Audio Equalizer", color = VibeTextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Equalizer, contentDescription = null, tint = VibeBlue) },
                            onClick = { showMoreMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep Timer", color = VibeTextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = VibeTextSecondary) },
                            onClick = { showMoreMenu = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Visual Area (Album Art & Track Info OR Lyrics Mode)
            if (!isLyricsMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Large Album Artwork with soft shadow
                    AlbumArtwork(
                        song = currentSong,
                        size = artworkSize,
                        cornerRadius = 24.dp,
                        showGlow = false,
                        isPlaying = isPlaying
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Track Title & Favorite Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSong.title,
                                color = VibeTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentSong.artist,
                                color = VibeTextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(44.dp).testTag("now_playing_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (currentSong.isFavorite) Color(0xFFEF4444) else VibeTextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Smart Match Pill & Audio Specs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Smart Match Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(onClick = onOpenSmartMatchInspector)
                                .testTag("now_playing_smart_match_pill"),
                            color = if (currentSong.hasLyrics) VibeYellowContainer else VibeSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lyrics,
                                    contentDescription = null,
                                    tint = if (currentSong.hasLyrics) Color(0xFF854D0E) else VibeTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (currentSong.hasLyrics) "Lyrics auto-matched" else "No lyrics matched",
                                    color = if (currentSong.hasLyrics) Color(0xFF854D0E) else VibeTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = currentSong.format,
                            color = VibeTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Dedicated Lyrics Mode
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Compact header with small artwork + song title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AlbumArtwork(
                            song = currentSong,
                            size = 50.dp,
                            cornerRadius = 10.dp,
                            showGlow = false,
                            isPlaying = isPlaying
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSong.title,
                                color = VibeTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentSong.artist,
                                color = VibeTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Lyrics interactive view
                    LyricsView(
                        song = currentSong,
                        playbackPositionMs = playbackPositionMs,
                        autoScroll = autoScrollLyrics,
                        fontSizeSp = lyricsFontSize,
                        onSeekToTimestamp = onSeekTo,
                        onOpenSmartMatch = onOpenSmartMatchInspector,
                        onChooseLyricsFile = onChooseLyricsFile,
                        onToggleAutoScroll = onToggleAutoScroll,
                        onChangeFontSize = onChangeFontSize,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Playback Progress Slider & Timestamps
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                val currentProgress = if (currentSong.durationMs > 0) {
                    if (isUserDraggingSlider) sliderTempPosition else playbackPositionMs.toFloat()
                } else 0f

                Slider(
                    value = currentProgress,
                    onValueChange = {
                        isUserDraggingSlider = true
                        sliderTempPosition = it
                    },
                    onValueChangeFinished = {
                        isUserDraggingSlider = false
                        onSeekTo(sliderTempPosition.toLong())
                    },
                    valueRange = 0f..(currentSong.durationMs.toFloat().coerceAtLeast(1f)),
                    colors = SliderDefaults.colors(
                        thumbColor = VibeBlue,
                        activeTrackColor = VibeBlue,
                        inactiveTrackColor = VibeSurfaceVariant
                    ),
                    modifier = Modifier.testTag("playback_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentPositionFormatted,
                        color = VibeTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currentSong.durationFormatted,
                        color = VibeTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Playback Controls Row (White / Blue / Yellow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.size(44.dp).testTag("shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) VibeBlue else VibeTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.size(52.dp).testTag("previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Song",
                        tint = VibeTextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Primary Large Play/Pause Button (Royal Blue with soft shadow)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = VibeBlue
                        )
                        .clip(CircleShape)
                        .background(VibeBlue)
                        .clickable(onClick = onPlayPauseClick)
                        .testTag("main_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(52.dp).testTag("next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Song",
                        tint = VibeTextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Repeat Button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.size(44.dp).testTag("repeat_button")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) VibeBlue else VibeTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Bottom Actions (Lyrics Mode Toggle, Queue Sheet)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Queue Button with count
                Surface(
                    modifier = Modifier
                        .border(1.dp, VibeBorder, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenQueueSheet)
                        .testTag("open_queue_button"),
                    color = VibeSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Play Queue",
                            tint = VibeBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Queue ($queueCount)",
                            color = VibeTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Lyrics Toggle Button
                Surface(
                    modifier = Modifier
                        .border(1.dp, if (isLyricsMode) VibeBlue else VibeBorder, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onToggleLyricsMode)
                        .testTag("toggle_lyrics_mode_button"),
                    color = if (isLyricsMode) VibeBlue else VibeYellowContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = "Toggle Lyrics",
                            tint = if (isLyricsMode) Color.White else Color(0xFF854D0E),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isLyricsMode) "Artwork View" else "Lyrics",
                            color = if (isLyricsMode) Color.White else Color(0xFF854D0E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
