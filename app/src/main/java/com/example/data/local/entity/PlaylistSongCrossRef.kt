package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "trackId"],
    indices = [Index(value = ["playlistId"]), Index(value = ["trackId"])]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val trackId: String,
    val sortOrder: Int = 0
)
