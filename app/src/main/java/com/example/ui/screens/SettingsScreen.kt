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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VibeBackground
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueDark
import com.example.ui.theme.VibeBlueLight
import com.example.ui.theme.VibeBorder
import com.example.ui.theme.VibeSurface
import com.example.ui.theme.VibeSurfaceVariant
import com.example.ui.theme.VibeTextPrimary
import com.example.ui.theme.VibeTextSecondary
import com.example.ui.theme.VibeYellow
import com.example.ui.theme.VibeYellowContainer

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onRescanClick: () -> Unit,
    onOpenSmartMatchInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gaplessPlayback by remember { mutableStateOf(true) }
    var crossfadeSeconds by remember { mutableFloatStateOf(3f) }
    var smartLyricMatch by remember { mutableStateOf(true) }
    var autoScrollLyrics by remember { mutableStateOf(true) }
    var preferLrcFormat by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBackground)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = VibeTextPrimary, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    color = VibeTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Section: Smart Lyrics Matching Engine
        item {
            SettingsCategoryHeader(title = "SMART LYRICS ENGINE")
            SettingsCard {
                SettingsSwitchRow(
                    title = "Automatic Local Lyric Match",
                    subtitle = "Automatically pair .lrc files with local songs with matching names",
                    checked = smartLyricMatch,
                    onCheckedChange = { smartLyricMatch = it },
                    icon = Icons.Rounded.AutoAwesome,
                    iconTint = VibeBlue
                )

                HorizontalDivider(color = VibeBorder, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Auto-Scroll Synced Lyrics",
                    subtitle = "Scroll lyrics smoothly in real-time as the song plays",
                    checked = autoScrollLyrics,
                    onCheckedChange = { autoScrollLyrics = it },
                    icon = Icons.Rounded.Lyrics,
                    iconTint = Color(0xFF854D0E)
                )

                HorizontalDivider(color = VibeBorder, thickness = 1.dp)

                SettingsSwitchRow(
                    title = "Prefer .LRC Timestamps",
                    subtitle = "Prioritize synchronized LRC timestamp files over plain text",
                    checked = preferLrcFormat,
                    onCheckedChange = { preferLrcFormat = it },
                    icon = Icons.Default.GraphicEq,
                    iconTint = VibeBlue
                )

                HorizontalDivider(color = VibeBorder, thickness = 1.dp)

                SettingsActionRow(
                    title = "Smart Match Diagnostic Inspector",
                    subtitle = "View matching rules and verify .lrc associations",
                    onClick = onOpenSmartMatchInspector,
                    icon = Icons.Default.Info,
                    iconTint = VibeBlue
                )
            }
        }

        // Section: Playback
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SettingsCategoryHeader(title = "PLAYBACK & AUDIO")
            SettingsCard {
                SettingsSwitchRow(
                    title = "Gapless Playback",
                    subtitle = "Seamless track transitions with no silence gaps",
                    checked = gaplessPlayback,
                    onCheckedChange = { gaplessPlayback = it },
                    icon = Icons.Default.GraphicEq,
                    iconTint = VibeBlue
                )

                HorizontalDivider(color = VibeBorder, thickness = 1.dp)

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Crossfade Duration", color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("${crossfadeSeconds.toInt()}s", color = VibeBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = crossfadeSeconds,
                        onValueChange = { crossfadeSeconds = it },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = VibeBlue,
                            activeTrackColor = VibeBlue,
                            inactiveTrackColor = VibeSurfaceVariant
                        )
                    )
                }
            }
        }

        // Section: Library & Storage
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SettingsCategoryHeader(title = "LOCAL LIBRARY & FOLDERS")
            SettingsCard {
                SettingsActionRow(
                    title = "Rescan Device Storage",
                    subtitle = "Index new songs and match newly added .lrc files",
                    onClick = onRescanClick,
                    icon = Icons.Default.Sync,
                    iconTint = VibeBlue
                )

                HorizontalDivider(color = VibeBorder, thickness = 1.dp)

                SettingsInfoRow(
                    title = "Music Folder",
                    value = "/storage/emulated/0/Music",
                    icon = Icons.Default.Folder,
                    iconTint = VibeTextSecondary
                )

                HorizontalDivider(color = VibeBorder, thickness = 1.dp)

                SettingsInfoRow(
                    title = "Lyrics Folder",
                    value = "/storage/emulated/0/Music/Lyrics",
                    icon = Icons.Default.Storage,
                    iconTint = VibeTextSecondary
                )
            }
        }

        // Section: Privacy & About
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SettingsCategoryHeader(title = "PRIVACY & ABOUT")
            SettingsCard {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VibeBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = VibeBlue, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("100% Offline & Private", color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("VibePlay operates completely offline. No tracking, no streaming, no account required.", color = VibeTextSecondary, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = VibeBorder, thickness = 1.dp)

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("VibePlay", color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Version 1.0.0", color = VibeTextSecondary, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VibeYellowContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("OFFLINE ONLY", color = Color(0xFF854D0E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        color = VibeBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, VibeBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = VibeSurface
    ) {
        Column { content() }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = VibeTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = VibeBlue,
                uncheckedTrackColor = VibeSurfaceVariant,
                uncheckedThumbColor = VibeTextSecondary
            )
        )
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = VibeTextSecondary, fontSize = 12.sp)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VibeTextSecondary)
    }
}

@Composable
fun SettingsInfoRow(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = VibeBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
