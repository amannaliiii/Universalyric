package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Track
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithIndigo

@Composable
fun AlbumArtCard(
    track: Track?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(track?.accentColorHex ?: "#6366F1"))
    } catch (e: Exception) {
        ZenithIndigo
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing dynamic shadow behind artwork
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (isPlaying) 0.55f else 0.25f),
                                accentColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        radius = size.maxDimension * (if (isPlaying) pulseScale * 0.65f else 0.55f)
                    )
                }
        )

        // Main Album Card
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), spotColor = accentColor),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF14192B)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (track?.thumbnailUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(accentColor.copy(alpha = 0.6f), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${track.title} artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                }

                // Vinyl ring overlay for aesthetic depth
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        )
                )

                // Status Badge (Offline Downloaded vs Online)
                val isDownloaded = track?.isDownloaded == true
                Surface(
                    shape = CircleShape,
                    color = if (isDownloaded) ZenithEmerald.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Wifi,
                            contentDescription = if (isDownloaded) "Offline Available" else "Online Stream",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (isDownloaded) "Offline Ready" else "YouTube Stream",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
