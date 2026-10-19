package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithIndigo
import com.example.ui.theme.ZenithRose
import com.example.ui.theme.ZenithSurface
import com.example.ui.theme.ZenithSurfaceVariant
import com.example.ui.theme.ZenithTextMuted
import com.example.ui.theme.ZenithTextSecondary
import com.example.util.YouTubeUrlParser

@Composable
fun ImportScreen(
    onImportTrack: (String, String, String, String, String) -> Boolean,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var artistInput by remember { mutableStateOf("") }
    var albumInput by remember { mutableStateOf("Imported") }
    var lrcInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val extractedVideoId by remember(urlInput) {
        derivedStateOf { YouTubeUrlParser.extractVideoId(urlInput) }
    }

    val thumbnailUrl by remember(extractedVideoId) {
        derivedStateOf {
            extractedVideoId?.let { YouTubeUrlParser.getThumbnailUrl(it) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("import_screen")
    ) {
        Text(
            text = "Import Audio Stream",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Enter any YouTube link (watch, youtu.be, shorts) or 11-char Video ID to stream audio and sync lyrics.",
            color = ZenithTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // URL Input Field
        OutlinedTextField(
            value = urlInput,
            onValueChange = {
                urlInput = it
                errorMessage = null
            },
            label = { Text("YouTube URL or Video ID", color = ZenithTextSecondary) },
            placeholder = { Text("https://www.youtube.com/watch?v=...", color = ZenithTextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = if (extractedVideoId != null) ZenithEmerald else ZenithTextSecondary
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (extractedVideoId != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid URL",
                            tint = ZenithEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let { clipText ->
                                urlInput = clipText
                                errorMessage = null
                            }
                        },
                        modifier = Modifier.testTag("paste_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from clipboard",
                            tint = ZenithCyan
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (extractedVideoId != null) ZenithEmerald else ZenithIndigo,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = ZenithSurfaceVariant,
                unfocusedContainerColor = ZenithSurface
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("import_url_input")
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = ZenithRose,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thumbnail & Live Preview Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ZenithSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF192033)),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnailUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(thumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Fetched Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = ZenithTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (extractedVideoId != null) "Video ID: $extractedVideoId" else "Enter a valid YouTube URL",
                        color = if (extractedVideoId != null) ZenithEmerald else ZenithTextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (extractedVideoId != null) "Ready for audio-only background streaming & offline download." else "Thumbnail and audio will load automatically.",
                        color = ZenithTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metadata Fields
        OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = { Text("Song Title", color = ZenithTextSecondary) },
            placeholder = { Text("e.g. Chill Sunset Memories", color = ZenithTextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZenithIndigo,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = ZenithSurfaceVariant,
                unfocusedContainerColor = ZenithSurface
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("import_title_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = artistInput,
            onValueChange = { artistInput = it },
            label = { Text("Artist Name", color = ZenithTextSecondary) },
            placeholder = { Text("e.g. Ambient Chillout", color = ZenithTextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZenithIndigo,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = ZenithSurfaceVariant,
                unfocusedContainerColor = ZenithSurface
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("import_artist_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = albumInput,
            onValueChange = { albumInput = it },
            label = { Text("Album Name (Optional)", color = ZenithTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZenithIndigo,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = ZenithSurfaceVariant,
                unfocusedContainerColor = ZenithSurface
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // LRC Lyrics Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Synchronized Lyrics (.lrc)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedButton(
                onClick = {
                    val t = titleInput.ifBlank { "Chill Track" }
                    val a = artistInput.ifBlank { "YouTube" }
                    lrcInput = YouTubeUrlParser.generateSampleLrc(t, a, 180)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("generate_lrc_button")
            ) {
                Text("Generate Template", fontSize = 12.sp, color = ZenithCyan)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = lrcInput,
            onValueChange = { lrcInput = it },
            placeholder = {
                Text(
                    text = "[00:00.00]♪ Intro ♪\n[00:15.00]First line of lyrics...\n[00:30.00]Second line of lyrics...",
                    color = ZenithTextMuted,
                    fontSize = 12.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZenithIndigo,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = ZenithSurfaceVariant,
                unfocusedContainerColor = ZenithSurface
            ),
            maxLines = 6,
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("import_lrc_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save & Play Button
        Button(
            onClick = {
                val success = onImportTrack(
                    urlInput,
                    titleInput,
                    artistInput,
                    albumInput,
                    lrcInput
                )
                if (success) {
                    onNavigateToLibrary()
                } else {
                    errorMessage = "Please enter a valid YouTube URL or 11-char Video ID."
                }
            },
            enabled = extractedVideoId != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = ZenithIndigo,
                disabledContainerColor = ZenithSurfaceVariant
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_import_button")
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add to Library & Play",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
