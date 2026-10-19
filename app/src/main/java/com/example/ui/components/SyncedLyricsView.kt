package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LyricLine

@Composable
fun SyncedLyricsView(
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    onSeekToTimestamp: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Smooth auto-scroll to the active sung line
    LaunchedEffect(activeLyricIndex) {
        if (activeLyricIndex >= 0 && activeLyricIndex < lyrics.size) {
            val targetIndex = (activeLyricIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(index = targetIndex, scrollOffset = 0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("synced_lyrics_view"),
        contentAlignment = Alignment.Center
    ) {
        if (lyrics.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "♪ No Lyrics Available ♪",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Import a track with .lrc lyrics or chill to the beat.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 100.dp, bottom = 220.dp, start = 24.dp, end = 24.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeLyricIndex
                    val isPast = index < activeLyricIndex

                    val textColor by animateColorAsState(
                        targetValue = when {
                            isActive -> Color.White
                            isPast -> Color.White.copy(alpha = 0.50f)
                            else -> Color.White.copy(alpha = 0.30f)
                        },
                        animationSpec = tween(durationMillis = 300),
                        label = "textColor"
                    )

                    val textScale by animateFloatAsState(
                        targetValue = if (isActive) 1.05f else 1.0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "textScale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .scale(textScale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onSeekToTimestamp(line.timestampMs)
                            }
                            .testTag("lyric_line_$index")
                    ) {
                        Text(
                            text = line.text.ifBlank { "♪ ♪ ♪" },
                            color = textColor,
                            fontSize = if (isActive) 24.sp else 19.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                            lineHeight = if (isActive) 32.sp else 26.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}
