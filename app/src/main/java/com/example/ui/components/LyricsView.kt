package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueLight
import com.example.ui.theme.VibeBorder
import com.example.ui.theme.VibeSurface
import com.example.ui.theme.VibeSurfaceVariant
import com.example.ui.theme.VibeTextPrimary
import com.example.ui.theme.VibeTextSecondary
import com.example.ui.theme.VibeTextTertiary
import com.example.ui.theme.VibeYellow
import com.example.ui.theme.VibeYellowContainer

@Composable
fun LyricsView(
    song: Song?,
    playbackPositionMs: Long,
    autoScroll: Boolean,
    fontSizeSp: Float,
    onSeekToTimestamp: (Long) -> Unit,
    onOpenSmartMatch: () -> Unit,
    onChooseLyricsFile: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onChangeFontSize: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val listState = rememberLazyListState()

    // Find active lyric line index based on current playback position
    val activeIndex = remember(song.lyrics, playbackPositionMs) {
        if (song.lyrics.isEmpty()) -1
        else {
            val idx = song.lyrics.indexOfLast { it.timestampMs <= playbackPositionMs }
            if (idx >= 0) idx else 0
        }
    }

    // Auto-scroll to active line smoothly
    LaunchedEffect(activeIndex, autoScroll) {
        if (autoScroll && activeIndex >= 0 && song.lyrics.isNotEmpty()) {
            val targetIndex = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("lyrics_view_container")
    ) {
        // Lyrics Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Indicator (Auto Matched from local file / Manual / Unsynced)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenSmartMatch)
                    .testTag("lyrics_status_pill"),
                color = if (song.hasLyrics) VibeYellowContainer else VibeSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = null,
                        tint = if (song.hasLyrics) Color(0xFF854D0E) else VibeTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (song.hasLyrics) "Matched from local file" else "No lyrics found",
                        color = if (song.hasLyrics) Color(0xFF854D0E) else VibeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controls: Font Size Cycle and Auto-Scroll Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Font Size button
                IconButton(
                    onClick = {
                        val next = when {
                            fontSizeSp < 18f -> 20f
                            fontSizeSp < 23f -> 24f
                            else -> 16f
                        }
                        onChangeFontSize(next)
                    },
                    modifier = Modifier.size(36.dp).testTag("lyrics_font_size_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "Adjust Lyric Size",
                        tint = VibeTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Auto Scroll toggle
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleAutoScroll)
                        .testTag("auto_scroll_toggle_button"),
                    color = if (autoScroll) VibeBlueLight else VibeSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Auto-scroll",
                            tint = if (autoScroll) VibeBlue else VibeTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (autoScroll) "Auto-Sync" else "Manual",
                            color = if (autoScroll) VibeBlue else VibeTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Lyrics Content / Empty State
        if (song.lyrics.isEmpty() || !song.hasLyrics) {
            // Clean Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(VibeBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = null,
                            tint = VibeBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No lyrics found",
                        color = VibeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Place a matching .lrc file in your music folder, or pick one manually below.",
                        color = VibeTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onChooseLyricsFile,
                            modifier = Modifier.fillMaxWidth().testTag("choose_lyrics_file_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = VibeBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Lyrics File (.lrc)", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onOpenSmartMatch,
                            modifier = Modifier.fillMaxWidth().testTag("search_local_lyrics_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = VibeBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search Local Lyrics", color = VibeTextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        } else {
            // Synced / Scrollable Lyrics List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("lyrics_list"),
                contentPadding = PaddingValues(vertical = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(song.lyrics) { index, line ->
                    val isActive = index == activeIndex
                    val textColor by animateColorAsState(
                        targetValue = if (isActive) VibeBlue else VibeTextTertiary,
                        animationSpec = tween(200),
                        label = "lyric_color"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSeekToTimestamp(line.timestampMs) }
                            .padding(vertical = 6.dp, horizontal = 10.dp)
                            .then(
                                if (isActive) {
                                    Modifier.background(VibeBlueLight)
                                } else Modifier
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(VibeYellow)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = line.text,
                                color = textColor,
                                fontSize = if (isActive) (fontSizeSp + 2).sp else fontSizeSp.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                lineHeight = (fontSizeSp * 1.35f).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
