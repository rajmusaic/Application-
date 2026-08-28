package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.Album
import com.example.model.Artist
import com.example.model.LyricLine
import com.example.model.LyricSyncType
import com.example.model.MatchStatus
import com.example.model.SmartMatchDetails
import com.example.model.Song
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueDark
import com.example.ui.theme.VibeYellow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class ScanResult(
    val songs: List<Song>,
    val albums: List<Album>,
    val artists: List<Artist>,
    val lyricsFoundCount: Int
)

class LocalMusicScanner(private val context: Context) {

    private val lrcTimestampPattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?\\](.*)")

    suspend fun scanLocalLibrary(
        onProgress: (current: Int, total: Int, currentPath: String, songsFound: Int, lyricsFound: Int) -> Unit
    ): ScanResult {
        val songs = mutableListOf<Song>()
        var lyricsFoundCount = 0

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val totalCount = it.count
                var processed = 0

                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val yearColumn = it.getColumnIndex(MediaStore.Audio.Media.YEAR)

                while (it.moveToNext()) {
                    processed++
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val artist = it.getString(artistColumn)?.takeIf { a -> a != "<unknown>" } ?: "Unknown Artist"
                    val album = it.getString(albumColumn)?.takeIf { a -> a != "<unknown>" } ?: "Unknown Album"
                    val duration = it.getLong(durationColumn)
                    val filePath = it.getString(dataColumn) ?: ""
                    val dateAddedSec = it.getLong(dateAddedColumn)
                    val year = if (yearColumn != -1 && !it.isNull(yearColumn)) it.getInt(yearColumn) else 0

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val audioFile = if (filePath.isNotEmpty()) File(filePath) else null
                    val format = audioFile?.extension?.uppercase() ?: "AUDIO"

                    // Try to auto-match local .lrc or lyric files
                    val (lrcLines, rawText, syncType, smartMatch) = searchAndMatchLocalLyrics(audioFile, title, artist)

                    if (lrcLines.isNotEmpty() || !rawText.isNullOrBlank()) {
                        lyricsFoundCount++
                    }

                    val dateAddedFormatted = if (dateAddedSec > 0) {
                        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                        sdf.format(Date(dateAddedSec * 1000L))
                    } else "Recent"

                    val song = Song(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = duration,
                        contentUri = contentUri,
                        filePath = filePath,
                        format = format,
                        bitrate = if (format == "FLAC") "Lossless" else "Stereo Audio",
                        albumGradientStart = VibeBlue,
                        albumGradientEnd = VibeBlueDark,
                        accentColor = VibeYellow,
                        hasLyrics = lrcLines.isNotEmpty() || !rawText.isNullOrBlank(),
                        lyricSyncType = syncType,
                        smartMatch = smartMatch,
                        lyrics = lrcLines,
                        rawLyricsText = rawText,
                        dateAdded = dateAddedFormatted,
                        year = year
                    )

                    songs.add(song)

                    onProgress(
                        processed,
                        totalCount,
                        filePath.ifEmpty { title },
                        songs.size,
                        lyricsFoundCount
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or querying exception - gracefully return whatever was gathered
        }

        // Group into Albums and Artists dynamically
        val albums = songs.groupBy { it.album to it.artist }.entries.mapIndexed { index, entry ->
            val albumSongs = entry.value
            val totalDuration = albumSongs.sumOf { it.durationMs }
            val albumYear = albumSongs.map { it.year }.filter { it > 0 }.maxOrNull() ?: 0
            Album(
                id = "album_$index",
                title = entry.key.first,
                artist = entry.key.second,
                year = albumYear,
                songCount = albumSongs.size,
                durationTotalMs = totalDuration,
                gradientStart = VibeBlue,
                gradientEnd = VibeBlueDark,
                songIds = albumSongs.map { it.id }
            )
        }

        val artists = songs.groupBy { it.artist }.entries.mapIndexed { index, entry ->
            val artistSongs = entry.value
            val distinctAlbums = artistSongs.map { it.album }.distinct().size
            Artist(
                id = "artist_$index",
                name = entry.key,
                songCount = artistSongs.size,
                albumCount = distinctAlbums,
                gradientStart = VibeBlue,
                gradientEnd = VibeBlueDark,
                songIds = artistSongs.map { it.id }
            )
        }

        return ScanResult(
            songs = songs,
            albums = albums,
            artists = artists,
            lyricsFoundCount = lyricsFoundCount
        )
    }

    fun parseLrcContent(content: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        content.lines().forEach { line ->
            val trimmed = line.trim()
            val matcher = lrcTimestampPattern.matcher(trimmed)
            if (matcher.find()) {
                try {
                    val minutes = matcher.group(1)?.toLong() ?: 0L
                    val seconds = matcher.group(2)?.toLong() ?: 0L
                    val millisStr = matcher.group(3)
                    val millis = when {
                        millisStr == null -> 0L
                        millisStr.length == 1 -> millisStr.toLong() * 100
                        millisStr.length == 2 -> millisStr.toLong() * 10
                        else -> millisStr.take(3).toLong()
                    }
                    val timestampMs = (minutes * 60 + seconds) * 1000 + millis
                    val text = matcher.group(4)?.trim() ?: ""
                    if (text.isNotEmpty() || lines.isNotEmpty()) {
                        lines.add(LyricLine(timestampMs = timestampMs, text = text))
                    }
                } catch (_: Exception) {}
            }
        }
        return lines.sortedBy { it.timestampMs }
    }

    private fun searchAndMatchLocalLyrics(
        audioFile: File?,
        title: String,
        artist: String
    ): LyricMatchOutcome {
        if (audioFile == null || !audioFile.exists()) {
            return LyricMatchOutcome(emptyList(), null, LyricSyncType.NONE, null)
        }

        val parentDir = audioFile.parentFile
        val baseName = audioFile.nameWithoutExtension

        // 1. Check exact match: SongName.lrc in same directory
        val exactLrc = File(parentDir, "$baseName.lrc")
        if (exactLrc.exists() && exactLrc.canRead()) {
            val content = exactLrc.readText()
            val parsed = parseLrcContent(content)
            val syncType = if (parsed.isNotEmpty()) LyricSyncType.SYNCHRONIZED_LRC else LyricSyncType.UNSYNCED_TEXT
            val details = SmartMatchDetails(
                isAutoMatched = true,
                musicFileName = audioFile.name,
                lyricFileName = exactLrc.name,
                confidenceScore = 100,
                matchStatus = MatchStatus.STRONG_MATCH,
                matchReason = "Exact filename match (.${audioFile.extension} <-> .lrc)",
                musicDirectory = parentDir?.absolutePath ?: "",
                lyricDirectory = exactLrc.parent ?: "",
                matchedTimeFormatted = "Auto-matched locally"
            )
            return LyricMatchOutcome(parsed, content, syncType, details)
        }

        // 2. Check /Lyrics subdirectory
        val lyricsDir = File(parentDir, "Lyrics")
        if (lyricsDir.exists() && lyricsDir.isDirectory) {
            val lrcInSub = File(lyricsDir, "$baseName.lrc")
            if (lrcInSub.exists() && lrcInSub.canRead()) {
                val content = lrcInSub.readText()
                val parsed = parseLrcContent(content)
                val syncType = if (parsed.isNotEmpty()) LyricSyncType.SYNCHRONIZED_LRC else LyricSyncType.UNSYNCED_TEXT
                val details = SmartMatchDetails(
                    isAutoMatched = true,
                    musicFileName = audioFile.name,
                    lyricFileName = lrcInSub.name,
                    confidenceScore = 98,
                    matchStatus = MatchStatus.STRONG_MATCH,
                    matchReason = "Matched inside /Lyrics directory",
                    musicDirectory = parentDir?.absolutePath ?: "",
                    lyricDirectory = lyricsDir.absolutePath,
                    matchedTimeFormatted = "Auto-matched locally"
                )
                return LyricMatchOutcome(parsed, content, syncType, details)
            }
        }

        // 3. Normalized matching in same directory (case-insensitive, whitespace normalized)
        val normalizedBase = baseName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")
        parentDir?.listFiles { file -> file.extension.equals("lrc", ignoreCase = true) }?.forEach { lrcFile ->
            val lrcBase = lrcFile.nameWithoutExtension.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")
            if (lrcBase == normalizedBase || (normalizedBase.isNotEmpty() && lrcBase.contains(normalizedBase))) {
                val content = lrcFile.readText()
                val parsed = parseLrcContent(content)
                val syncType = if (parsed.isNotEmpty()) LyricSyncType.SYNCHRONIZED_LRC else LyricSyncType.UNSYNCED_TEXT
                val details = SmartMatchDetails(
                    isAutoMatched = true,
                    musicFileName = audioFile.name,
                    lyricFileName = lrcFile.name,
                    confidenceScore = 92,
                    matchStatus = MatchStatus.POSSIBLE_MATCH,
                    matchReason = "Normalized name match",
                    musicDirectory = parentDir.absolutePath,
                    lyricDirectory = lrcFile.parent ?: "",
                    matchedTimeFormatted = "Auto-matched locally"
                )
                return LyricMatchOutcome(parsed, content, syncType, details)
            }
        }

        return LyricMatchOutcome(emptyList(), null, LyricSyncType.NONE, null)
    }

    private data class LyricMatchOutcome(
        val lines: List<LyricLine>,
        val rawText: String?,
        val syncType: LyricSyncType,
        val details: SmartMatchDetails?
    )
}
