package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.PlaylistEntity
import com.example.model.Track
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithIndigo
import com.example.ui.theme.ZenithRose
import com.example.ui.theme.ZenithSurface
import com.example.ui.theme.ZenithSurfaceVariant
import com.example.ui.theme.ZenithTextMuted
import com.example.ui.theme.ZenithTextSecondary
import com.example.util.LrcParser

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistEntity,
    playlistTracks: List<Track>,
    allAvailableTracks: List<Track>,
    currentTrackId: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayTrack: (Track) -> Unit,
    onAddSongToPlaylist: (String, String) -> Unit,
    onRemoveSongFromPlaylist: (String, String) -> Unit,
    onReorderSongs: (String, List<String>) -> Unit,
    onRenamePlaylist: (String, String, String) -> Unit,
    onDownloadPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTitle by remember { mutableStateOf(playlist.title) }
    var renameDesc by remember { mutableStateOf(playlist.description) }

    var showAddSongsDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp).testTag("playlist_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playlistTracks.size} songs",
                        color = ZenithTextSecondary,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint = ZenithTextSecondary
                    )
                }
            }

            // Playlist Actions: Play All, Download All, Add Song
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (playlistTracks.isNotEmpty()) {
                            onPlayTrack(playlistTracks.first())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("playlist_play_all")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play All")
                }

                Button(
                    onClick = { onDownloadPlaylist(playlist.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("playlist_download_all")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = ZenithEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", color = Color.White)
                }

                Button(
                    onClick = { showAddSongsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("playlist_add_songs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = ZenithCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = Color.White)
                }
            }

            // Songs List
            if (playlistTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = ZenithTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This playlist is empty",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddSongsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithIndigo)
                        ) {
                            Text("Add Tracks to Playlist")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(playlistTracks, key = { _, track -> track.id }) { index, track ->
                        val isCurrent = track.id == currentTrackId

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onPlayTrack(track) }
                                .testTag("playlist_track_$index"),
                            color = if (isCurrent) ZenithSurfaceVariant else ZenithSurface,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Order number
                                Text(
                                    text = "${index + 1}",
                                    color = ZenithTextMuted,
                                    fontSize = 13.sp,
                                    modifier = Modifier.width(24.dp)
                                )

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(track.thumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = if (isCurrent) ZenithCyan else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist,
                                        color = ZenithTextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Reorder buttons (Move Up / Down)
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val reordered = playlistTracks.map { it.id }.toMutableList()
                                            val temp = reordered[index]
                                            reordered[index] = reordered[index - 1]
                                            reordered[index - 1] = temp
                                            onReorderSongs(playlist.id, reordered)
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (index < playlistTracks.size - 1) {
                                            val reordered = playlistTracks.map { it.id }.toMutableList()
                                            val temp = reordered[index]
                                            reordered[index] = reordered[index + 1]
                                            reordered[index + 1] = temp
                                            onReorderSongs(playlist.id, reordered)
                                        }
                                    },
                                    enabled = index < playlistTracks.size - 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Move Down",
                                        tint = if (index < playlistTracks.size - 1) Color.White else Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Remove from playlist button
                                IconButton(
                                    onClick = { onRemoveSongFromPlaylist(playlist.id, track.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = ZenithRose.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rename Dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = renameTitle,
                            onValueChange = { renameTitle = it },
                            label = { Text("Title", color = ZenithTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZenithIndigo,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = renameDesc,
                            onValueChange = { renameDesc = it },
                            label = { Text("Description", color = ZenithTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZenithIndigo,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onRenamePlaylist(playlist.id, renameTitle, renameDesc)
                            showRenameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithIndigo)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel", color = ZenithTextSecondary)
                    }
                },
                containerColor = ZenithSurfaceVariant
            )
        }

        // Add Songs Dialog
        if (showAddSongsDialog) {
            val existingIds = playlistTracks.map { it.id }.toSet()
            val availableToAdd = allAvailableTracks.filter { !existingIds.contains(it.id) }

            AlertDialog(
                onDismissRequest = { showAddSongsDialog = false },
                title = { Text("Add Songs to Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    if (availableToAdd.isEmpty()) {
                        Text("All available songs are already in this playlist!", color = ZenithTextSecondary)
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items(availableToAdd) { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddSongToPlaylist(playlist.id, track.id)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = track.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.title, color = Color.White, fontSize = 14.sp, maxLines = 1)
                                        Text(track.artist, color = ZenithTextSecondary, fontSize = 12.sp)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = ZenithCyan
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddSongsDialog = false }) {
                        Text("Done", color = Color.White)
                    }
                },
                containerColor = ZenithSurfaceVariant
            )
        }
    }
}
