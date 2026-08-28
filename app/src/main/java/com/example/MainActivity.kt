package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.ManualLyricsPickerDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.QueueBottomSheet
import com.example.ui.components.SmartMatchSheet
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.EmptyLibraryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScanScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartLyricsMatchScreen
import com.example.ui.screens.SongsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VibeBackground
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueLight
import com.example.ui.theme.VibeBorder
import com.example.ui.theme.VibeSurface
import com.example.ui.theme.VibeTextSecondary
import com.example.viewmodel.MusicPlayerViewModel

sealed class ScreenDestination {
    object MainTabs : ScreenDestination()
    object Search : ScreenDestination()
    object LibraryScan : ScreenDestination()
    object SmartMatchExplainer : ScreenDestination()
    object Settings : ScreenDestination()
    data class AlbumDetail(val album: Album) : ScreenDestination()
    data class PlaylistDetail(val playlist: Playlist) : ScreenDestination()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VibePlayApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibePlayApp(viewModel: MusicPlayerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var currentTab by remember { mutableIntStateOf(0) }
    var currentDestination by remember { mutableStateOf<ScreenDestination>(ScreenDestination.MainTabs) }

    val smartMatchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Permission request launcher for local media read
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.startLibraryScan()
        }
    }

    fun requestPermissionsAndScan() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val hasPermission = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermission) {
            viewModel.startLibraryScan()
        } else {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // Auto-scan on initial launch if permission already granted
    LaunchedEffect(Unit) {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startLibraryScan()
        }
    }

    // Handle Back Press
    BackHandler(
        enabled = uiState.isNowPlayingExpanded ||
                currentDestination != ScreenDestination.MainTabs ||
                uiState.isSmartMatchSheetVisible ||
                uiState.isQueueSheetVisible ||
                uiState.isManualLyricPickerVisible
    ) {
        when {
            uiState.isManualLyricPickerVisible -> viewModel.closeManualLyricPicker()
            uiState.isSmartMatchSheetVisible -> viewModel.closeSmartMatchInspector()
            uiState.isQueueSheetVisible -> viewModel.setQueueSheetVisible(false)
            uiState.isNowPlayingExpanded -> viewModel.setNowPlayingExpanded(false)
            currentDestination != ScreenDestination.MainTabs -> currentDestination = ScreenDestination.MainTabs
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VibeBackground)) {
        // Main Screen Scaffold
        Scaffold(
            bottomBar = {
                if (currentDestination == ScreenDestination.MainTabs && !uiState.isNowPlayingExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // Persistent Floating MiniPlayer (Above Navigation Bar)
                        val nowPlayingSong = uiState.currentSong
                        if (nowPlayingSong != null) {
                            MiniPlayer(
                                currentSong = nowPlayingSong,
                                isPlaying = uiState.isPlaying,
                                playbackPositionMs = uiState.playbackPositionMs,
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onNextClick = { viewModel.nextSong() },
                                onClick = { viewModel.setNowPlayingExpanded(true) },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        // Bottom Navigation Bar
                        NavigationBar(
                            containerColor = VibeSurface,
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .border(1.dp, VibeBorder)
                                .testTag("bottom_nav_bar")
                        ) {
                            val items = listOf(
                                Triple(0, "Home", Icons.Filled.Home to Icons.Outlined.Home),
                                Triple(1, "Songs", Icons.Filled.MusicNote to Icons.Outlined.MusicNote),
                                Triple(2, "Albums", Icons.Filled.Album to Icons.Outlined.Album),
                                Triple(3, "Playlists", Icons.Filled.PlaylistPlay to Icons.Outlined.PlaylistPlay)
                            )

                            items.forEach { (index, title, iconPair) ->
                                val selected = currentTab == index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = index },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) iconPair.first else iconPair.second,
                                            contentDescription = title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = VibeBlue,
                                        selectedTextColor = VibeBlue,
                                        unselectedIconColor = VibeTextSecondary,
                                        unselectedTextColor = VibeTextSecondary,
                                        indicatorColor = VibeBlueLight
                                    ),
                                    modifier = Modifier.testTag("nav_tab_$index")
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (currentDestination == ScreenDestination.MainTabs && !uiState.isNowPlayingExpanded) paddingValues.calculateBottomPadding() else 0.dp)
            ) {
                when (val dest = currentDestination) {
                    is ScreenDestination.MainTabs -> {
                        if (uiState.isLibraryEmpty) {
                            EmptyLibraryScreen(
                                onScanAgain = { requestPermissionsAndScan() },
                                onPickFolder = { requestPermissionsAndScan() }
                            )
                        } else {
                            when (currentTab) {
                                0 -> HomeScreen(
                                    songs = uiState.songs,
                                    albums = uiState.albums,
                                    artists = uiState.artists,
                                    playlists = uiState.playlists,
                                    recentlyPlayed = uiState.recentlyPlayed,
                                    currentSong = uiState.currentSong,
                                    isPlaying = uiState.isPlaying,
                                    onSongClick = { viewModel.playSong(it) },
                                    onAlbumClick = { currentDestination = ScreenDestination.AlbumDetail(it) },
                                    onArtistClick = { /* Artist filter */ },
                                    onPlaylistClick = { currentDestination = ScreenDestination.PlaylistDetail(it) },
                                    onSearchClick = { currentDestination = ScreenDestination.Search },
                                    onScanClick = { currentDestination = ScreenDestination.LibraryScan },
                                    onSettingsClick = { currentDestination = ScreenDestination.Settings },
                                    onSmartMatchInspectorClick = { viewModel.openSmartMatchInspector(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onNavigateToTab = { currentTab = it }
                                )
                                1 -> SongsScreen(
                                    songs = uiState.songs,
                                    currentSong = uiState.currentSong,
                                    isPlaying = uiState.isPlaying,
                                    searchQuery = uiState.searchQuery,
                                    activeSort = uiState.activeSortOption,
                                    activeFilter = uiState.activeFilter,
                                    isGridView = uiState.isGridView,
                                    onSongClick = { viewModel.playSong(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onOpenSmartMatch = { viewModel.openSmartMatchInspector(it) },
                                    onUpdateSearchQuery = { viewModel.updateSearchQuery(it) },
                                    onSortChange = { viewModel.setSortOption(it) },
                                    onFilterChange = { viewModel.setFilter(it) },
                                    onToggleGridView = { viewModel.toggleGridView() }
                                )
                                2 -> AlbumsScreen(
                                    albums = uiState.albums,
                                    onAlbumClick = { currentDestination = ScreenDestination.AlbumDetail(it) }
                                )
                                3 -> PlaylistsScreen(
                                    playlists = uiState.playlists,
                                    onPlaylistClick = { currentDestination = ScreenDestination.PlaylistDetail(it) },
                                    onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                                    onDeletePlaylist = { viewModel.deletePlaylist(it) }
                                )
                            }
                        }
                    }

                    is ScreenDestination.Search -> {
                        SearchScreen(
                            songs = uiState.songs,
                            albums = uiState.albums,
                            artists = uiState.artists,
                            playlists = uiState.playlists,
                            currentSong = uiState.currentSong,
                            isPlaying = uiState.isPlaying,
                            onSongClick = { viewModel.playSong(it) },
                            onAlbumClick = { currentDestination = ScreenDestination.AlbumDetail(it) },
                            onArtistClick = { /* Filter */ },
                            onPlaylistClick = { currentDestination = ScreenDestination.PlaylistDetail(it) },
                            onOpenSmartMatch = { viewModel.openSmartMatchInspector(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }

                    is ScreenDestination.LibraryScan -> {
                        LibraryScanScreen(
                            isScanning = uiState.isScanning,
                            progress = uiState.scanProgress,
                            stats = uiState.scanStats,
                            onStartListening = { currentDestination = ScreenDestination.MainTabs }
                        )
                    }

                    is ScreenDestination.SmartMatchExplainer -> {
                        SmartLyricsMatchScreen(
                            onBackClick = { currentDestination = ScreenDestination.MainTabs }
                        )
                    }

                    is ScreenDestination.Settings -> {
                        SettingsScreen(
                            onBackClick = { currentDestination = ScreenDestination.MainTabs },
                            onRescanClick = { currentDestination = ScreenDestination.LibraryScan },
                            onOpenSmartMatchInspector = { currentDestination = ScreenDestination.SmartMatchExplainer }
                        )
                    }

                    is ScreenDestination.AlbumDetail -> {
                        AlbumDetailScreen(
                            album = dest.album,
                            allSongs = uiState.songs,
                            currentSong = uiState.currentSong,
                            isPlaying = uiState.isPlaying,
                            onBackClick = { currentDestination = ScreenDestination.MainTabs },
                            onSongClick = { viewModel.playSong(it) },
                            onPlayAll = {
                                val albumSongs = uiState.songs.filter { dest.album.songIds.contains(it.id) || it.album == dest.album.title }
                                albumSongs.firstOrNull()?.let { viewModel.playSong(it) }
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onOpenSmartMatch = { viewModel.openSmartMatchInspector(it) }
                        )
                    }

                    is ScreenDestination.PlaylistDetail -> {
                        PlaylistDetailScreen(
                            playlist = dest.playlist,
                            allSongs = uiState.songs,
                            currentSong = uiState.currentSong,
                            isPlaying = uiState.isPlaying,
                            onBackClick = { currentDestination = ScreenDestination.MainTabs },
                            onSongClick = { viewModel.playSong(it) },
                            onPlayAll = {
                                val playlistSongs = uiState.songs.filter { dest.playlist.songIds.contains(it.id) }
                                playlistSongs.firstOrNull()?.let { viewModel.playSong(it) }
                            },
                            onShuffleAll = {
                                viewModel.toggleShuffle()
                                val playlistSongs = uiState.songs.filter { dest.playlist.songIds.contains(it.id) }
                                playlistSongs.shuffled().firstOrNull()?.let { viewModel.playSong(it) }
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onOpenSmartMatch = { viewModel.openSmartMatchInspector(it) },
                            onRenamePlaylist = { viewModel.renamePlaylist(dest.playlist.id, it) },
                            onDeletePlaylist = {
                                viewModel.deletePlaylist(dest.playlist.id)
                                currentDestination = ScreenDestination.MainTabs
                            }
                        )
                    }
                }
            }
        }

        // Fullscreen Animated Expandable Now Playing Screen
        AnimatedVisibility(
            visible = uiState.isNowPlayingExpanded && uiState.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                currentSong = uiState.currentSong,
                isPlaying = uiState.isPlaying,
                playbackPositionMs = uiState.playbackPositionMs,
                isShuffle = uiState.isShuffle,
                repeatMode = uiState.repeatMode,
                isLyricsMode = uiState.isLyricsMode,
                autoScrollLyrics = uiState.autoScrollLyrics,
                lyricsFontSize = uiState.lyricsFontSize,
                queueCount = uiState.queue.size,
                onCollapse = { viewModel.setNowPlayingExpanded(false) },
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onPreviousClick = { viewModel.previousSong() },
                onNextClick = { viewModel.nextSong() },
                onSeekTo = { viewModel.seekTo(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleFavorite = { uiState.currentSong?.id?.let { viewModel.toggleFavorite(it) } },
                onToggleLyricsMode = { viewModel.toggleLyricsMode() },
                onOpenQueueSheet = { viewModel.setQueueSheetVisible(true) },
                onOpenSmartMatchInspector = { viewModel.openSmartMatchInspector(uiState.currentSong) },
                onChooseLyricsFile = { viewModel.openManualLyricPicker() },
                onToggleAutoScroll = { viewModel.toggleAutoScrollLyrics() },
                onChangeFontSize = { viewModel.setLyricsFontSize(it) }
            )
        }

        // Smart Lyrics Match Bottom Sheet
        if (uiState.isSmartMatchSheetVisible) {
            SmartMatchSheet(
                song = uiState.inspectedSongForMatch ?: uiState.currentSong,
                sheetState = smartMatchSheetState,
                onDismiss = { viewModel.closeSmartMatchInspector() },
                onChangeLyricsFileClick = { viewModel.openManualLyricPicker() }
            )
        }

        // Play Queue Bottom Sheet
        if (uiState.isQueueSheetVisible) {
            QueueBottomSheet(
                queue = uiState.queue,
                currentSong = uiState.currentSong,
                isPlaying = uiState.isPlaying,
                sheetState = queueSheetState,
                onSongClick = {
                    viewModel.playSong(it)
                    viewModel.setQueueSheetVisible(false)
                },
                onDismiss = { viewModel.setQueueSheetVisible(false) }
            )
        }

        // Manual Lyric Picker Dialog
        if (uiState.isManualLyricPickerVisible) {
            ManualLyricsPickerDialog(
                song = uiState.inspectedSongForMatch ?: uiState.currentSong,
                onDismiss = { viewModel.closeManualLyricPicker() },
                onFileSelected = { fileName, content ->
                    val target = uiState.inspectedSongForMatch ?: uiState.currentSong
                    target?.let { viewModel.attachManualLyricFile(it.id, fileName, content) }
                }
            )
        }
    }
}
