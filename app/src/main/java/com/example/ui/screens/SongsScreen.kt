package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.SongListItem
import com.example.ui.theme.VibeBackground
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueDark
import com.example.ui.theme.VibeBlueLight
import com.example.ui.theme.VibeBorder
import com.example.ui.theme.VibeEmerald
import com.example.ui.theme.VibeSurface
import com.example.ui.theme.VibeSurfaceVariant
import com.example.ui.theme.VibeTextPrimary
import com.example.ui.theme.VibeTextSecondary
import com.example.ui.theme.VibeYellow
import com.example.ui.theme.VibeYellowContainer
import com.example.viewmodel.SongFilter
import com.example.viewmodel.SortOption

@Composable
fun SongsScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    searchQuery: String,
    activeSort: SortOption,
    activeFilter: SongFilter,
    isGridView: Boolean,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSmartMatch: (Song) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onFilterChange: (SongFilter) -> Unit,
    onToggleGridView: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredSongs = remember(songs, searchQuery, activeSort, activeFilter) {
        var list = songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
        }

        list = when (activeFilter) {
            SongFilter.ALL -> list
            SongFilter.WITH_LYRICS -> list.filter { it.hasLyrics }
            SongFilter.HI_RES -> list.filter { it.format.contains("FLAC", ignoreCase = true) }
            SongFilter.FAVORITES -> list.filter { it.isFavorite }
        }

        when (activeSort) {
            SortOption.RECENTLY_ADDED -> list
            SortOption.RECENTLY_PLAYED -> list.sortedByDescending { it.playCount }
            SortOption.TITLE -> list.sortedBy { it.title }
            SortOption.ARTIST -> list.sortedBy { it.artist }
            SortOption.ALBUM -> list.sortedBy { it.album }
            SortOption.DURATION -> list.sortedByDescending { it.durationMs }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBackground)
            .testTag("songs_screen_container")
    ) {
        // Screen Title & Search Bar
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Songs",
                color = VibeTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (filteredSongs.isEmpty()) "No matching audio files" else "${filteredSongs.size} tracks on device",
                color = VibeTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onUpdateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("songs_search_input"),
                placeholder = { Text("Search songs, artists, formats...", color = VibeTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = VibeBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onUpdateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = VibeTextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibeBlue,
                    unfocusedBorderColor = VibeBorder,
                    focusedContainerColor = VibeSurface,
                    unfocusedContainerColor = VibeSurfaceVariant,
                    cursorColor = VibeBlue
                )
            )
        }

        // Sort & Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sort Dropdown Button
            Box {
                Surface(
                    modifier = Modifier
                        .border(1.dp, VibeBorder, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSortMenu = true }
                        .testTag("sort_dropdown_button"),
                    color = VibeSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = null,
                            tint = VibeBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = activeSort.displayName,
                            color = VibeTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = VibeTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(VibeSurface)
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.displayName,
                                    color = if (activeSort == option) VibeBlue else VibeTextPrimary,
                                    fontWeight = if (activeSort == option) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (activeSort == option) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = VibeBlue)
                                }
                            },
                            onClick = {
                                onSortChange(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // Grid / List Toggle
            IconButton(
                onClick = onToggleGridView,
                modifier = Modifier.size(36.dp).testTag("grid_toggle_button")
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle Grid/List",
                    tint = VibeTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SongFilter.values()) { filter ->
                val isSelected = activeFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            text = filter.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (filter == SongFilter.WITH_LYRICS) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) Color.White else Color(0xFF854D0E)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibeBlue,
                        selectedLabelColor = Color.White,
                        containerColor = VibeSurfaceVariant,
                        labelColor = VibeTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Empty state when filter yields 0 songs
        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No songs found for \"$searchQuery\"" else "No songs found",
                    color = VibeTextSecondary,
                    fontSize = 14.sp
                )
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("songs_grid_view"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSongs) { song ->
                    val isCurrent = song.id == currentSong?.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isCurrent) VibeBlue else VibeBorder, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSongClick(song) }
                            .testTag("grid_song_${song.id}"),
                        color = if (isCurrent) VibeBlueLight else VibeSurface,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AlbumArtwork(
                                    song = song,
                                    size = 140.dp,
                                    cornerRadius = 10.dp,
                                    showGlow = false,
                                    isPlaying = isCurrent && isPlaying
                                )
                                if (song.hasLyrics) {
                                    Box(
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(VibeYellowContainer)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("LRC", color = Color(0xFF854D0E), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = song.title,
                                color = if (isCurrent) VibeBlue else VibeTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                color = VibeTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("songs_list_view"),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredSongs) { song ->
                    val isCurrent = song.id == currentSong?.id
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrentSong = isCurrent,
                        onClick = { onSongClick(song) },
                        onToggleFavorite = { onToggleFavorite(song.id) },
                        onOpenSmartMatch = { onOpenSmartMatch(song) },
                        onAddToPlaylist = { /* Handled in playlist dialog */ }
                    )
                }
            }
        }
    }
}
