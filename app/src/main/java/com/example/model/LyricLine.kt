package com.example.model

/**
 * Represents a single timed lyric line in a song.
 * [timestampMs] is the time in milliseconds when the line starts being sung.
 */
data class LyricLine(
    val timestampMs: Long,
    val text: String
)
