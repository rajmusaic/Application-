package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// White / Light Canvas Surfaces (Primary Light Theme)
val VibeBackground = Color(0xFFFFFFFF)
val VibeBackgroundAlt = Color(0xFFF8FAFC)
val VibeSurface = Color(0xFFFFFFFF)
val VibeSurfaceVariant = Color(0xFFF1F5F9)
val VibeSurfaceElevated = Color(0xFFFFFFFF)
val VibeBorder = Color(0xFFE2E8F0)
val VibeBorderSubtle = Color(0xFFF1F5F9)

// Primary Brand Color — Modern Deep Blue
val VibeBlue = Color(0xFF2563EB)
val VibeBlueDark = Color(0xFF1D4ED8)
val VibeBlueLight = Color(0xFFEFF6FF)
val VibeBlueContainer = Color(0xFFDBEAFE)

// Secondary Accent Color — Bright Energetic Yellow
val VibeYellow = Color(0xFFFACC15)
val VibeYellowWarm = Color(0xFFEAB308)
val VibeYellowLight = Color(0xFFFEF08A)
val VibeYellowContainer = Color(0xFFFEF9C3)
val VibeYellowDark = Color(0xFFCA8A04)

// Supporting Accent Colors
val VibeRose = Color(0xFFF43F5E)
val VibeEmerald = Color(0xFF10B981)
val VibeEmeraldLight = Color(0xFFD1FAE5)
val VibeIndigo = Color(0xFF4F46E5)

// High-Contrast Light-Mode Text Colors
val VibeTextPrimary = Color(0xFF0F172A)
val VibeTextSecondary = Color(0xFF475569)
val VibeTextTertiary = Color(0xFF94A3B8)
val VibeTextMuted = Color(0xFFCBD5E1)

// Gradient Brushes
val VibeBlueGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
)

val VibeBlueYellowGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFFFACC15))
)

val VibeCardPlaceholderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
)

val VibeYellowBadgeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFEF08A), Color(0xFFFACC15))
)
