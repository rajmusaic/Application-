package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueDark
import com.example.ui.theme.VibeYellow

data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val translation: String? = null
)

enum class LyricSyncType {
    SYNCHRONIZED_LRC,
    UNSYNCED_TEXT,
    NONE
}

enum class MatchStatus(val label: String, val badgeText: String) {
    SEARCHING("Searching for lyrics", "Searching..."),
    MATCHING("Matching lyrics", "Matching..."),
    MATCHED("Lyrics matched", "LRC Matched"),
    STRONG_MATCH("Strong match", "100% Match"),
    POSSIBLE_MATCH("Possible match", "Matched"),
    NO_MATCH("No matching lyrics found", "No Lyrics")
}

data class SmartMatchDetails(
    val isAutoMatched: Boolean = true,
    val musicFileName: String,
    val lyricFileName: String,
    val confidenceScore: Int, // e.g. 100
    val matchStatus: MatchStatus = MatchStatus.STRONG_MATCH,
    val matchReason: String, // e.g. "Exact filename match (.mp3 <-> .lrc)"
    val musicDirectory: String = "",
    val lyricDirectory: String = "",
    val matchedTimeFormatted: String = "Auto-detected"
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String? = null,
    val filePath: String? = null,
    val format: String = "MP3",
    val bitrate: String = "Local Audio",
    val albumGradientStart: Color = VibeBlue,
    val albumGradientEnd: Color = VibeBlueDark,
    val accentColor: Color = VibeYellow,
    val hasLyrics: Boolean = false,
    val lyricSyncType: LyricSyncType = LyricSyncType.NONE,
    val smartMatch: SmartMatchDetails? = null,
    val lyrics: List<LyricLine> = emptyList(),
    val rawLyricsText: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: String = "",
    val year: Int = 0
) {
    val durationFormatted: String
        get() {
            if (durationMs <= 0) return "0:00"
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}
