package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
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
) {
    fun toTrack(): Track {
        return Track(
            id = id,
            youtubeId = youtubeId,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            thumbnailUrl = thumbnailUrl,
            lrcLyrics = lrcLyrics,
            accentColorHex = accentColorHex,
            isFavorite = isFavorite,
            isCustomImported = isCustomImported,
            isDownloaded = isDownloaded,
            localFilePath = localFilePath,
            downloadProgress = downloadProgress,
            dateAdded = dateAdded
        )
    }

    companion object {
        fun fromTrack(track: Track): TrackEntity {
            return TrackEntity(
                id = track.id,
                youtubeId = track.youtubeId,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                thumbnailUrl = track.thumbnailUrl,
                lrcLyrics = track.lrcLyrics,
                accentColorHex = track.accentColorHex,
                isFavorite = track.isFavorite,
                isCustomImported = track.isCustomImported,
                isDownloaded = track.isDownloaded,
                localFilePath = track.localFilePath,
                downloadProgress = track.downloadProgress,
                dateAdded = track.dateAdded
            )
        }
    }
}
