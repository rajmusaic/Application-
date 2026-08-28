package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.VibeBlue
import com.example.ui.theme.VibeBlueDark
import com.example.ui.theme.VibeYellow

@Composable
fun AlbumArtwork(
    song: Song?,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    cornerRadius: Dp = 20.dp,
    showGlow: Boolean = true,
    isPlaying: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "artwork_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showGlow && isPlaying) {
                    Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    VibeBlue.copy(alpha = glowAlpha),
                                    VibeYellow.copy(alpha = glowAlpha * 0.4f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.toPx() * 0.72f
                            )
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Main Album Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isPlaying) 16.dp else 6.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = VibeBlue.copy(alpha = 0.35f),
                    ambientColor = Color(0x1A000000)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(VibeBlue, VibeBlueDark),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            // Stylized background graphics (Vinyl concentric rings with yellow accent dot)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height

                // Subtle white vinyl groove arcs
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = canvasWidth * 0.55f,
                    center = Offset(canvasWidth * 0.85f, canvasHeight * 0.15f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = canvasWidth * 0.8f,
                    center = Offset(canvasWidth * 0.85f, canvasHeight * 0.15f),
                    style = Stroke(width = 2.dp.toPx())
                )
                
                // Yellow accent glow disc
                drawCircle(
                    color = VibeYellow.copy(alpha = 0.25f),
                    radius = canvasWidth * 0.22f,
                    center = Offset(canvasWidth * 0.2f, canvasHeight * 0.8f)
                )
            }

            // Central Music Note Icon
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music Artwork",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.38f)
                )
            }

            // Format Badge at bottom corner
            if (song != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = song.format,
                        color = VibeYellow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
