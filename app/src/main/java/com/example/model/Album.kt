package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueDark
import com.example.ui.theme.VibeYellow

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int = 0,
    val songCount: Int = 0,
    val durationTotalMs: Long = 0L,
    val gradientStart: Color = VibeBlue,
    val gradientEnd: Color = VibeBlueDark,
    val songIds: List<String> = emptyList()
)

data class Artist(
    val id: String,
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val gradientStart: Color = VibeBlue,
    val gradientEnd: Color = VibeBlueDark,
    val songIds: List<String> = emptyList()
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val songIds: List<String> = emptyList(),
    val gradientStart: Color = VibeBlue,
    val gradientEnd: Color = VibeYellow,
    val createdAt: String = "",
    val isSystemGenerated: Boolean = false
)
