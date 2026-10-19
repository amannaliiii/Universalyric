package com.example.util

import com.example.model.LyricLine

object LrcParser {

    // Regex matching timestamp tags like [01:23.45], [01:23.456], [01:23:45], [01:23]
    private val TIMESTAMP_REGEX = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?\]""")

    /**
     * Parses standard LRC formatted string into an ordered list of [LyricLine].
     * Supports multiple timestamps per line and filters out metadata tags.
     */
    fun parse(lrcContent: String): List<LyricLine> {
        if (lrcContent.isBlank()) return emptyList()

        val result = mutableListOf<LyricLine>()
        val lines = lrcContent.lines()

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            // Find all timestamps in this line
            val matches = TIMESTAMP_REGEX.findAll(line).toList()
            if (matches.isEmpty()) continue

            // Extract the lyric text after the last timestamp tag
            val lastMatch = matches.last()
            val text = line.substring(lastMatch.range.last + 1).trim()

            for (match in matches) {
                val minStr = match.groupValues[1]
                val secStr = match.groupValues[2]
                val fracStr = match.groupValues.getOrNull(3).orEmpty()

                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L

                val millis = when (fracStr.length) {
                    1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
                    2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
                    3 -> fracStr.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val totalMs = (minutes * 60_000L) + (seconds * 1_000L) + millis
                result.add(LyricLine(timestampMs = totalMs, text = text))
            }
        }

        return result.sortedBy { it.timestampMs }
    }

    /**
     * Returns the index of the currently active lyric line for [currentTimeMs].
     * Returns -1 if playback is before the first lyric line or if the list is empty.
     */
    fun getActiveIndex(lyrics: List<LyricLine>, currentTimeMs: Long): Int {
        if (lyrics.isEmpty()) return -1
        if (currentTimeMs < lyrics.first().timestampMs) return -1

        var low = 0
        var high = lyrics.size - 1
        var candidate = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lyrics[mid].timestampMs <= currentTimeMs) {
                candidate = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return candidate
    }

    /**
     * Formats milliseconds into standard mm:ss string.
     */
    fun formatTime(millis: Long): String {
        if (millis <= 0L) return "0:00"
        val totalSec = millis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%d:%02d", min, sec)
    }
}
