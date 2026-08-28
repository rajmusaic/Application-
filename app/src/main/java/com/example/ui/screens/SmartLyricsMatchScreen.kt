package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VibeBackground
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueLight
import com.example.ui.theme.VibeBorder
import com.example.ui.theme.VibeEmerald
import com.example.ui.theme.VibeSurface
import com.example.ui.theme.VibeSurfaceVariant
import com.example.ui.theme.VibeTextPrimary
import com.example.ui.theme.VibeTextSecondary
import com.example.ui.theme.VibeYellow
import com.example.ui.theme.VibeYellowContainer

@Composable
fun SmartLyricsMatchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBackground)
            .testTag("smart_lyrics_match_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = VibeTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Smart Lyric Match",
                        color = VibeTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Zero-setup local file pairing",
                        color = VibeTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero Concept Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibeBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = VibeSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VibeBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = VibeBlue, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "How Smart Matching Works",
                            color = VibeTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VibePlay searches your music directories for corresponding .lrc or .txt files. When file names or metadata align, lyrics are matched automatically for offline synchronized display.",
                        color = VibeTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Visual Example Demonstration Card
        item {
            Text(
                text = "Match Demonstration",
                color = VibeTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibeBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = VibeSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Step 1: Music File
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VibeBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, tint = VibeBlue, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Music File Detected", color = VibeTextSecondary, fontSize = 11.sp)
                            Text("SongName.mp3", color = VibeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Matching Indicator Arrow
                    Box(
                        modifier = Modifier
                            .padding(start = 17.dp, top = 6.dp, bottom = 6.dp)
                            .width(2.dp)
                            .height(24.dp)
                            .background(VibeBlue)
                    )

                    // Step 2: Matched Lyrics File
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VibeYellowContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Lyrics, contentDescription = null, tint = Color(0xFF854D0E), modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Matched Lyrics File", color = VibeTextSecondary, fontSize = 11.sp)
                            Text("SongName.lrc", color = Color(0xFF854D0E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status & Confidence Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = VibeYellowContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF854D0E), modifier = Modifier.size(16.dp))
                                Text("Status: ✓ Automatically matched", color = Color(0xFF854D0E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("100% Match", color = Color(0xFF854D0E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Engine Capabilities Highlights
        item {
            Text(
                text = "Key Engine Features",
                color = VibeTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EngineFeatureRow(
                    title = "Exact & Fuzzy Filename Normalization",
                    desc = "Matches files even when track numbers, suffixes, or file naming differ slightly.",
                    icon = Icons.Default.Check
                )
                EngineFeatureRow(
                    title = "High-Precision LRC Timestamps",
                    desc = "Parses standard [mm:ss.xx] timestamps with millisecond accuracy for live sync.",
                    icon = Icons.Default.Speed
                )
                EngineFeatureRow(
                    title = "100% Offline & Private",
                    desc = "No audio or lyrics are ever sent to servers. Zero internet data required.",
                    icon = Icons.Default.Lock
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibeBlue)
            ) {
                Text("Got it", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EngineFeatureRow(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VibeBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = VibeSurface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(VibeBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = VibeBlue, modifier = Modifier.size(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = VibeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, color = VibeTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}
