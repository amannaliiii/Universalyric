package com.example.model

/**
 * Model representing an audio track in the music player.
 */
data class Track(
    val id: String,
    val youtubeId: String,
    val title: String,
    val artist: String,
    val album: String = "Single",
    val durationMs: Long = 180_000L,
    val thumbnailUrl: String = "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg",
    val lrcLyrics: String = "",
    val accentColorHex: String = "#6366F1",
    val isFavorite: Boolean = false,
    val isCustomImported: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val downloadProgress: Int = 0, // 0 to 100
    val dateAdded: Long = System.currentTimeMillis()
)
