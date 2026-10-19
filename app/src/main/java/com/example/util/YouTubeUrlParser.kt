package com.example.util

object YouTubeUrlParser {

    // Regex for direct 11-character video ID
    private val DIRECT_ID_REGEX = Regex("^[a-zA-Z0-9_-]{11}$")

    // Regex patterns for various YouTube URL forms
    private val URL_PATTERNS = listOf(
        // Standard watch: youtube.com/watch?v=...
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/watch\?(?:.*&)?v=([a-zA-Z0-9_-]{11})(?:&.*)?"""),
        // Short URL: youtu.be/...
        Regex("""(?:https?://)?youtu\.be/([a-zA-Z0-9_-]{11})(?:\?.*)?"""),
        // YouTube Shorts: youtube.com/shorts/...
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/shorts/([a-zA-Z0-9_-]{11})(?:\?.*)?"""),
        // Embedded: youtube.com/embed/...
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/embed/([a-zA-Z0-9_-]{11})(?:\?.*)?"""),
        // Live stream: youtube.com/live/...
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/live/([a-zA-Z0-9_-]{11})(?:\?.*)?""")
    )

    /**
     * Extracts an 11-character YouTube video ID from a URL or raw ID string.
     * Returns null if no valid ID could be extracted.
     */
    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // Check if raw 11-char ID
        if (DIRECT_ID_REGEX.matches(trimmed)) {
            return trimmed
        }

        // Check URL patterns
        for (pattern in URL_PATTERNS) {
            val match = pattern.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                val candidate = match.groupValues[1]
                if (DIRECT_ID_REGEX.matches(candidate)) {
                    return candidate
                }
            }
        }

        return null
    }

    /**
     * Constructs the standard YouTube thumbnail URL for a given video ID.
     */
    fun getThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    /**
     * Generates a sample timed LRC template for a given duration in seconds.
     */
    fun generateSampleLrc(title: String, artist: String, durationSec: Int = 180): String {
        val sb = StringBuilder()
        sb.append("[ti:$title]\n")
        sb.append("[ar:$artist]\n")
        sb.append("[00:00.00]♪ Intro ♪\n")
        var currentSec = 6
        var lineNum = 1
        while (currentSec < durationSec - 15) {
            val min = currentSec / 60
            val sec = currentSec % 60
            val timeTag = String.format("[%02d:%02d.00]", min, sec)
            sb.append("$timeTag Line $lineNum - $title ($artist)\n")
            currentSec += 8
            lineNum++
        }
        val outMin = (durationSec - 10) / 60
        val outSec = (durationSec - 10) % 60
        sb.append(String.format("[%02d:%02d.00]♪ Outro ♪\n", outMin, outSec))
        return sb.toString()
    }
}
