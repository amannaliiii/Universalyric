package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PlaylistEntity
import com.example.model.LyricLine
import com.example.model.Track
import com.example.ui.components.AlbumArtCard
import com.example.ui.components.PlaybackControls
import com.example.ui.components.SyncedLyricsView
import com.example.ui.theme.ZenithBgDark
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithIndigo
import com.example.ui.theme.ZenithRose
import com.example.ui.theme.ZenithSurfaceVariant
import com.example.ui.theme.ZenithTextSecondary
import com.example.viewmodel.PlaybackStatus
import com.example.viewmodel.RepeatMode

@Composable
fun NowPlayingScreen(
    track: Track?,
    isPlaying: Boolean,
    playbackStatus: PlaybackStatus,
    currentPositionMs: Long,
    totalDurationMs: Long,
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    isLyricsViewOpen: Boolean,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    volume: Float,
    playlists: List<PlaylistEntity>,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLyricsView: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onRemoveDownload: (String) -> Unit,
    onAddToPlaylist: (String, String) -> Unit,
    onSetVolume: (Float) -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onCollapse()
    }

    var showPlaylistDialog by remember { mutableStateOf(false) }

    val accentColor = try {
        Color(android.graphics.Color.parseColor(track?.accentColorHex ?: "#6366F1"))
    } catch (e: Exception) {
        ZenithIndigo
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.35f),
                        ZenithBgDark.copy(alpha = 0.95f),
                        ZenithBgDark
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        color = ZenithTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = track?.album ?: "Zenith Music",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Toggle Lyrics / Artwork Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLyricsViewOpen) ZenithIndigo else ZenithSurfaceVariant,
                    modifier = Modifier.testTag("toggle_lyrics_button")
                ) {
                    IconButton(
                        onClick = onToggleLyricsView,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = if (isLyricsViewOpen) "View Artwork" else "View Synced Lyrics",
                            tint = if (isLyricsViewOpen) Color.White else ZenithTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Center: Switch between Album Art and Synced Lyrics
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isLyricsViewOpen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "centerView"
                ) { showLyrics ->
                    if (showLyrics) {
                        SyncedLyricsView(
                            lyrics = lyrics,
                            activeLyricIndex = activeLyricIndex,
                            onSeekToTimestamp = onSeekTo
                        )
                    } else {
                        AlbumArtCard(
                            track = track,
                            isPlaying = isPlaying
                        )
                    }
                }
            }

            // Bottom Section: Info + Controls + Volume
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // Track Info & Actions (Favorite, Download, Add to Playlist)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track?.title ?: "Select a Track",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = track?.artist ?: "Zenith",
                            color = ZenithTextSecondary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Download Offline Button
                    if (track != null) {
                        IconButton(
                            onClick = {
                                if (track.isDownloaded) {
                                    onRemoveDownload(track.id)
                                } else {
                                    onDownloadTrack(track)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("download_track_button")
                        ) {
                            Icon(
                                imageVector = if (track.isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                contentDescription = if (track.isDownloaded) "Downloaded (Tap to remove)" else "Download for offline listening",
                                tint = if (track.isDownloaded) ZenithEmerald else ZenithTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Add to Playlist Button
                        IconButton(
                            onClick = { showPlaylistDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("add_to_playlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = "Add to playlist",
                                tint = ZenithTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Favorite Heart
                        IconButton(
                            onClick = { onToggleFavorite(track.id) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("favorite_button")
                        ) {
                            Icon(
                                imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (track.isFavorite) ZenithRose else ZenithTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Playback Controls (Seekbar + Buttons)
                PlaybackControls(
                    isPlaying = isPlaying,
                    playbackStatus = playbackStatus,
                    currentPositionMs = currentPositionMs,
                    totalDurationMs = totalDurationMs,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    onTogglePlayPause = onTogglePlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeekTo = onSeekTo,
                    onToggleShuffle = onToggleShuffle,
                    onToggleRepeat = onToggleRepeat
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Volume Slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = "Volume down",
                        tint = ZenithTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volume,
                        onValueChange = onSetVolume,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = ZenithIndigo,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("volume_slider")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume up",
                        tint = ZenithTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Add to Playlist Dialog
        if (showPlaylistDialog && track != null) {
            AlertDialog(
                onDismissRequest = { showPlaylistDialog = false },
                title = {
                    Text(
                        text = "Add to Playlist",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (playlists.isEmpty()) {
                        Text(
                            text = "No playlists found. Create one from the Library tab!",
                            color = ZenithTextSecondary
                        )
                    } else {
                        Column {
                            playlists.forEach { pl ->
                                TextButton(
                                    onClick = {
                                        onAddToPlaylist(pl.id, track.id)
                                        showPlaylistDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = pl.title,
                                        color = ZenithCyan,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPlaylistDialog = false }) {
                        Text("Close", color = Color.White)
                    }
                },
                containerColor = ZenithSurfaceVariant
            )
        }
    }
}
