package com.example.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LocalMusicScanner
import com.example.model.Album
import com.example.model.Artist
import com.example.model.LyricLine
import com.example.model.LyricSyncType
import com.example.model.MatchStatus
import com.example.model.Playlist
import com.example.model.SmartMatchDetails
import com.example.model.Song
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeYellow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ONE, ALL
}

enum class SortOption(val displayName: String) {
    TITLE("Title (A-Z)"),
    ARTIST("Artist (A-Z)"),
    ALBUM("Album"),
    DURATION("Duration"),
    RECENTLY_ADDED("Recently Added"),
    RECENTLY_PLAYED("Recently Played")
}

enum class SongFilter(val displayName: String) {
    ALL("All Songs"),
    WITH_LYRICS("With Lyrics"),
    HI_RES("Lossless / FLAC"),
    FAVORITES("Favorites")
}

data class ScanStats(
    val songsFound: Int = 0,
    val albumsFound: Int = 0,
    val artistsFound: Int = 0,
    val lyricsFound: Int = 0,
    val currentScannedPath: String = ""
)

data class UiState(
    // Real Device Data (Starts clean & empty — no fake data)
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val favoriteSongIds: Set<String> = emptySet(),
    val recentlyPlayed: List<Song> = emptyList(),

    // Playback
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val playbackPositionMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = 0,

    // UI Sheets & Modes
    val isNowPlayingExpanded: Boolean = false,
    val isLyricsMode: Boolean = false,
    val autoScrollLyrics: Boolean = true,
    val lyricsFontSize: Float = 20f,
    val isQueueSheetVisible: Boolean = false,
    val isSmartMatchSheetVisible: Boolean = false,
    val inspectedSongForMatch: Song? = null,
    val isManualLyricPickerVisible: Boolean = false,

    // Search & Filters
    val searchQuery: String = "",
    val activeSortOption: SortOption = SortOption.TITLE,
    val activeFilter: SongFilter = SongFilter.ALL,
    val isGridView: Boolean = false,

    // Library Scanner
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStats: ScanStats = ScanStats(),
    val isScanComplete: Boolean = false,
    val isLibraryEmpty: Boolean = true,

    // Settings
    val gaplessPlayback: Boolean = true,
    val crossfadeDurationSec: Float = 0f,
    val autoMatchLyrics: Boolean = true,
    val preferLrcFiles: Boolean = true,
    val currentThemeMode: String = "Light Mode", // Default Light Mode as requested
    val audioOutputDevice: String = "Internal Speaker / Headset"
)

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scanner = LocalMusicScanner(application)
    private var mediaPlayer: MediaPlayer? = null
    private var progressTickerJob: Job? = null

    init {
        // Automatically start scanning device storage upon initialization
        startLibraryScan()
    }

    fun startLibraryScan() {
        if (_uiState.value.isScanning) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0f,
                    isScanComplete = false,
                    scanStats = ScanStats()
                )
            }

            val result = scanner.scanLocalLibrary { current, total, currentPath, songsFound, lyricsFound ->
                val progress = if (total > 0) current.toFloat() / total else 0f
                _uiState.update { state ->
                    state.copy(
                        scanProgress = progress,
                        scanStats = ScanStats(
                            songsFound = songsFound,
                            albumsFound = state.albums.size,
                            artistsFound = state.artists.size,
                            lyricsFound = lyricsFound,
                            currentScannedPath = currentPath
                        )
                    )
                }
            }

            _uiState.update {
                it.copy(
                    songs = result.songs,
                    albums = result.albums,
                    artists = result.artists,
                    queue = result.songs,
                    isLibraryEmpty = result.songs.isEmpty(),
                    isScanning = false,
                    scanProgress = 1f,
                    isScanComplete = true,
                    scanStats = ScanStats(
                        songsFound = result.songs.size,
                        albumsFound = result.albums.size,
                        artistsFound = result.artists.size,
                        lyricsFound = result.lyricsFoundCount,
                        currentScannedPath = if (result.songs.isNotEmpty()) "Completed" else "No audio files detected"
                    )
                )
            }
        }
    }

    fun playSong(song: Song) {
        val currentQueue = if (_uiState.value.queue.isEmpty()) _uiState.value.songs else _uiState.value.queue
        val index = currentQueue.indexOfFirst { it.id == song.id }

        // Update recently played
        val updatedRecent = (listOf(song) + _uiState.value.recentlyPlayed.filter { it.id != song.id }).take(20)

        _uiState.update {
            it.copy(
                currentSong = song,
                isPlaying = true,
                playbackPositionMs = 0L,
                queue = currentQueue,
                queueIndex = if (index >= 0) index else 0,
                recentlyPlayed = updatedRecent
            )
        }

        initializeAndPlayMedia(song)
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val target = songs.getOrElse(startIndex) { songs.first() }
        _uiState.update {
            it.copy(
                queue = songs,
                queueIndex = startIndex
            )
        }
        playSong(target)
    }

    private fun initializeAndPlayMedia(song: Song) {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val uriStr = song.contentUri
            if (!uriStr.isNullOrEmpty()) {
                val uri = Uri.parse(uriStr)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(getApplication(), uri)
                    prepare()
                    start()
                    setOnCompletionListener {
                        nextSong()
                    }
                }
            }
        } catch (_: Exception) {
            // Media player fallback for preview or unsupported formats
        }
        startProgressTicker()
    }

    fun togglePlayPause() {
        val current = _uiState.value.currentSong
        if (current == null) {
            val first = _uiState.value.songs.firstOrNull()
            if (first != null) playSong(first)
            return
        }

        val wasPlaying = _uiState.value.isPlaying
        if (wasPlaying) {
            try { mediaPlayer?.pause() } catch (_: Exception) {}
            _uiState.update { it.copy(isPlaying = false) }
            stopProgressTicker()
        } else {
            try { mediaPlayer?.start() } catch (_: Exception) {}
            _uiState.update { it.copy(isPlaying = true) }
            startProgressTicker()
        }
    }

    fun nextSong() {
        val queue = _uiState.value.queue
        if (queue.isEmpty()) return

        val nextIndex = when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> _uiState.value.queueIndex
            RepeatMode.ALL -> (_uiState.value.queueIndex + 1) % queue.size
            RepeatMode.OFF -> {
                if (_uiState.value.queueIndex + 1 < queue.size) _uiState.value.queueIndex + 1 else return
            }
        }

        val nextSong = queue[nextIndex]
        playSong(nextSong)
    }

    fun previousSong() {
        val queue = _uiState.value.queue
        if (queue.isEmpty()) return

        if (_uiState.value.playbackPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (_uiState.value.queueIndex - 1 >= 0) {
            _uiState.value.queueIndex - 1
        } else {
            queue.size - 1
        }
        val prevSong = queue[prevIndex]
        playSong(prevSong)
    }

    fun seekTo(positionMs: Long) {
        val duration = _uiState.value.currentSong?.durationMs ?: 0L
        val clamped = positionMs.coerceIn(0L, duration.coerceAtLeast(1L))
        try {
            mediaPlayer?.seekTo(clamped.toInt())
        } catch (_: Exception) {}
        _uiState.update { it.copy(playbackPositionMs = clamped) }
    }

    fun toggleShuffle() {
        _uiState.update { state ->
            val newShuffle = !state.isShuffle
            val newQueue = if (newShuffle) {
                val current = state.currentSong
                if (current != null) {
                    listOf(current) + (state.songs.filter { it.id != current.id }.shuffled())
                } else state.songs.shuffled()
            } else {
                state.songs
            }
            state.copy(isShuffle = newShuffle, queue = newQueue)
        }
    }

    fun toggleRepeat() {
        _uiState.update {
            val nextMode = when (it.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            it.copy(repeatMode = nextMode)
        }
    }

    fun toggleFavorite(songId: String) {
        _uiState.update { state ->
            val favs = state.favoriteSongIds.toMutableSet()
            if (favs.contains(songId)) favs.remove(songId) else favs.add(songId)
            
            val updatedSongs = state.songs.map {
                if (it.id == songId) it.copy(isFavorite = favs.contains(songId)) else it
            }
            val updatedCurrent = if (state.currentSong?.id == songId) {
                state.currentSong.copy(isFavorite = favs.contains(songId))
            } else state.currentSong

            state.copy(
                favoriteSongIds = favs,
                songs = updatedSongs,
                currentSong = updatedCurrent
            )
        }
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isNowPlayingExpanded = expanded) }
    }

    fun toggleLyricsMode() {
        _uiState.update { it.copy(isLyricsMode = !it.isLyricsMode) }
    }

    fun toggleAutoScrollLyrics() {
        _uiState.update { it.copy(autoScrollLyrics = !it.autoScrollLyrics) }
    }

    fun setLyricsFontSize(size: Float) {
        _uiState.update { it.copy(lyricsFontSize = size.coerceIn(14f, 32f)) }
    }

    fun openSmartMatchInspector(song: Song?) {
        _uiState.update {
            it.copy(
                isSmartMatchSheetVisible = true,
                inspectedSongForMatch = song ?: it.currentSong
            )
        }
    }

    fun closeSmartMatchInspector() {
        _uiState.update { it.copy(isSmartMatchSheetVisible = false, inspectedSongForMatch = null) }
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isQueueSheetVisible = visible) }
    }

    fun openManualLyricPicker() {
        _uiState.update { it.copy(isManualLyricPickerVisible = true) }
    }

    fun closeManualLyricPicker() {
        _uiState.update { it.copy(isManualLyricPickerVisible = false) }
    }

    fun attachManualLyricFile(songId: String, fileName: String, content: String) {
        val parsedLines = scanner.parseLrcContent(content)
        val syncType = if (parsedLines.isNotEmpty()) LyricSyncType.SYNCHRONIZED_LRC else LyricSyncType.UNSYNCED_TEXT

        val smartMatch = SmartMatchDetails(
            isAutoMatched = false,
            musicFileName = _uiState.value.songs.find { it.id == songId }?.title ?: "Song",
            lyricFileName = fileName,
            confidenceScore = 100,
            matchStatus = MatchStatus.MATCHED,
            matchReason = "Manually linked by user",
            musicDirectory = "",
            lyricDirectory = "",
            matchedTimeFormatted = "Linked manually"
        )

        _uiState.update { state ->
            val updated = state.songs.map { song ->
                if (song.id == songId) {
                    song.copy(
                        hasLyrics = true,
                        lyricSyncType = syncType,
                        lyrics = parsedLines,
                        rawLyricsText = content,
                        smartMatch = smartMatch
                    )
                } else song
            }
            val currentUpdated = if (state.currentSong?.id == songId) {
                state.currentSong.copy(
                    hasLyrics = true,
                    lyricSyncType = syncType,
                    lyrics = parsedLines,
                    rawLyricsText = content,
                    smartMatch = smartMatch
                )
            } else state.currentSong

            state.copy(
                songs = updated,
                currentSong = currentUpdated,
                isManualLyricPickerVisible = false
            )
        }
    }

    fun unlinkLyrics(songId: String) {
        _uiState.update { state ->
            val updated = state.songs.map { song ->
                if (song.id == songId) {
                    song.copy(
                        hasLyrics = false,
                        lyricSyncType = LyricSyncType.NONE,
                        lyrics = emptyList(),
                        rawLyricsText = null,
                        smartMatch = null
                    )
                } else song
            }
            val currentUpdated = if (state.currentSong?.id == songId) {
                state.currentSong.copy(
                    hasLyrics = false,
                    lyricSyncType = LyricSyncType.NONE,
                    lyrics = emptyList(),
                    rawLyricsText = null,
                    smartMatch = null
                )
            } else state.currentSong

            state.copy(
                songs = updated,
                currentSong = currentUpdated,
                isSmartMatchSheetVisible = false
            )
        }
    }

    // Playlist Management
    fun createPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        val newPlaylist = Playlist(
            id = "playlist_${System.currentTimeMillis()}",
            name = name.trim(),
            description = description.trim(),
            songIds = emptyList(),
            createdAt = "Today"
        )
        _uiState.update { it.copy(playlists = it.playlists + newPlaylist) }
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        if (newName.isBlank()) return
        _uiState.update { state ->
            state.copy(playlists = state.playlists.map {
                if (it.id == playlistId) it.copy(name = newName.trim()) else it
            })
        }
    }

    fun deletePlaylist(playlistId: String) {
        _uiState.update { state ->
            state.copy(playlists = state.playlists.filterNot { it.id == playlistId })
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        _uiState.update { state ->
            state.copy(playlists = state.playlists.map {
                if (it.id == playlistId && !it.songIds.contains(songId)) {
                    it.copy(songIds = it.songIds + songId)
                } else it
            })
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        _uiState.update { state ->
            state.copy(playlists = state.playlists.map {
                if (it.id == playlistId) {
                    it.copy(songIds = it.songIds.filterNot { id -> id == songId })
                } else it
            })
        }
    }

    // Search, Sort, Filter
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { state ->
            val sorted = when (option) {
                SortOption.TITLE -> state.songs.sortedBy { it.title.lowercase() }
                SortOption.ARTIST -> state.songs.sortedBy { it.artist.lowercase() }
                SortOption.ALBUM -> state.songs.sortedBy { it.album.lowercase() }
                SortOption.DURATION -> state.songs.sortedByDescending { it.durationMs }
                SortOption.RECENTLY_ADDED -> state.songs.reversed()
                SortOption.RECENTLY_PLAYED -> state.songs.sortedByDescending { it.playCount }
            }
            state.copy(activeSortOption = option, songs = sorted)
        }
    }

    fun setFilter(filter: SongFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun toggleGridView() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isPlaying) {
                delay(300)
                try {
                    val currentPos = mediaPlayer?.currentPosition?.toLong()
                    if (currentPos != null && currentPos >= 0) {
                        _uiState.update { it.copy(playbackPositionMs = currentPos) }
                    } else {
                        // Simulated increment if playing unsupported file format
                        val cur = _uiState.value.playbackPositionMs
                        val dur = _uiState.value.currentSong?.durationMs ?: 0L
                        if (dur > 0 && cur < dur) {
                            _uiState.update { it.copy(playbackPositionMs = (cur + 300).coerceAtMost(dur)) }
                        } else if (dur > 0 && cur >= dur) {
                            nextSong()
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun stopProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTicker()
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
    }
}
