package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithIndigo
import com.example.ui.theme.ZenithProgressTrack
import com.example.ui.theme.ZenithTextSecondary
import com.example.util.LrcParser
import com.example.viewmodel.PlaybackStatus
import com.example.viewmodel.RepeatMode

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    playbackStatus: PlaybackStatus,
    currentPositionMs: Long,
    totalDurationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var draggedPositionMs by remember { mutableFloatStateOf(0f) }

    val activeDisplayPosition = if (isDraggingSlider) {
        draggedPositionMs.toLong()
    } else {
        currentPositionMs
    }

    val sliderProgress = if (totalDurationMs > 0L) {
        (activeDisplayPosition.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seek Bar Slider
        Slider(
            value = sliderProgress,
            onValueChange = { frac ->
                isDraggingSlider = true
                draggedPositionMs = frac * totalDurationMs
            },
            onValueChangeFinished = {
                isDraggingSlider = false
                onSeekTo(draggedPositionMs.toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = ZenithIndigo,
                inactiveTrackColor = ZenithProgressTrack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("playback_seekbar")
        )

        // Timestamp Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = LrcParser.formatTime(activeDisplayPosition),
                color = ZenithTextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = LrcParser.formatTime(totalDurationMs),
                color = ZenithTextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            IconButton(
                onClick = onToggleShuffle,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) ZenithCyan else ZenithTextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous Button
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("prev_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play / Pause / Buffering FAB
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier
                    .size(68.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape, spotColor = ZenithIndigo)
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePlayPause)
                    .testTag("play_pause_button")
            ) {
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (playbackStatus == PlaybackStatus.BUFFERING) {
                        CircularProgressIndicator(
                            color = ZenithIndigo,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Next Button
            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("next_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat Button
            IconButton(
                onClick = onToggleRepeat,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("repeat_button")
            ) {
                Icon(
                    imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) ZenithCyan else ZenithTextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
