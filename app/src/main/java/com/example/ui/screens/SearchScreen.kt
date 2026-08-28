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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Lyrics
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.SongListItem
import com.example.ui.theme.VibeBackground
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueLight
import com.example.ui.theme.VibeBorder
import com.example.ui.theme.VibeSurface
import com.example.ui.theme.VibeSurfaceVariant
import com.example.ui.theme.VibeTextPrimary
import com.example.ui.theme.VibeTextSecondary
import com.example.ui.theme.VibeYellow
import com.example.ui.theme.VibeYellowContainer

enum class SearchCategory {
    ALL, SONGS, ARTISTS, ALBUMS, PLAYLISTS, LYRICS
}

@Composable
fun SearchScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onOpenSmartMatch: (Song) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }

    val matchingSongs = remember(query, songs) {
        if (query.isBlank()) emptyList()
        else songs.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
    }

    val matchingAlbums = remember(query, albums) {
        if (query.isBlank()) emptyList()
        else albums.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
    }

    val matchingArtists = remember(query, artists) {
        if (query.isBlank()) emptyList()
        else artists.filter { it.name.contains(query, ignoreCase = true) }
    }

    val matchingLyricsSongs = remember(query, songs) {
        if (query.isBlank()) emptyList()
        else songs.filter { song ->
            song.lyrics.any { line -> line.text.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBackground)
            .testTag("search_screen_container")
    ) {
        // Search Top Header & Input
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Search Library",
                color = VibeTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_text_input"),
                placeholder = { Text("Search songs, artists, albums, or lyrics...", color = VibeTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = VibeBlue
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = VibeTextSecondary)
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

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SearchCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
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
        }

        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Type a title, artist name, or lyric line to search offline",
                    color = VibeTextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            // Search Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Songs Section
                if ((selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SONGS) && matchingSongs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Songs (${matchingSongs.size})",
                            color = VibeTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(matchingSongs) { song ->
                        val isCurrent = song.id == currentSong?.id
                        SongListItem(
                            song = song,
                            isPlaying = isPlaying,
                            isCurrentSong = isCurrent,
                            onClick = { onSongClick(song) },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            onOpenSmartMatch = { onOpenSmartMatch(song) },
                            onAddToPlaylist = {}
                        )
                    }
                }

                // Lyric Content Matches (Dedicated subsection for lyrics search)
                if ((selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.LYRICS) && matchingLyricsSongs.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Lyrics, contentDescription = null, tint = Color(0xFF854D0E), modifier = Modifier.size(18.dp))
                            Text(
                                text = "Matched in Lyrics (${matchingLyricsSongs.size})",
                                color = Color(0xFF854D0E),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(matchingLyricsSongs) { song ->
                        val matchingLine = song.lyrics.firstOrNull { it.text.contains(query, ignoreCase = true) }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, VibeBorder, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSongClick(song) },
                            color = VibeSurface
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AlbumArtwork(song = song, size = 40.dp, cornerRadius = 8.dp, showGlow = false)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(song.title, color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(song.artist, color = VibeTextSecondary, fontSize = 12.sp)
                                    }
                                }
                                if (matchingLine != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = VibeYellowContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "\"${matchingLine.text}\"",
                                            color = Color(0xFF854D0E),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Albums Section
                if ((selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.ALBUMS) && matchingAlbums.isNotEmpty()) {
                    item {
                        Text(
                            text = "Albums (${matchingAlbums.size})",
                            color = VibeTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(matchingAlbums) { album ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, VibeBorder, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onAlbumClick(album) },
                            color = VibeSurface
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VibeBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Album, contentDescription = null, tint = VibeBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album.title, color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${album.artist} • ${album.songCount} tracks", color = VibeTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Artists Section
                if ((selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.ARTISTS) && matchingArtists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Artists (${matchingArtists.size})",
                            color = VibeTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(matchingArtists) { artist ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, VibeBorder, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onArtistClick(artist) },
                            color = VibeSurface
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(VibeBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = VibeBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(artist.name, color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${artist.songCount} tracks", color = VibeTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
