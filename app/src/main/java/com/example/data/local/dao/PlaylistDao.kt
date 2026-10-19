package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: String)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun deletePlaylistSongs(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeSongFromPlaylist(playlistId: String, trackId: String)

    @Query("DELETE FROM playlist_song_cross_ref WHERE trackId = :trackId")
    suspend fun deleteSongFromAllPlaylists(trackId: String)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_song_cross_ref r ON t.id = r.trackId
        WHERE r.playlistId = :playlistId
        ORDER BY r.sortOrder ASC
    """)
    fun getSongsForPlaylistFlow(playlistId: String): Flow<List<TrackEntity>>

    @Query("SELECT MAX(sortOrder) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxSortOrder(playlistId: String): Int?

    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    fun getSongCountForPlaylist(playlistId: String): Flow<Int>

    @Query("UPDATE playlist_song_cross_ref SET sortOrder = :sortOrder WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun updateSongSortOrder(playlistId: String, trackId: String, sortOrder: Int)

    @Transaction
    suspend fun reorderPlaylistSongs(playlistId: String, orderedTrackIds: List<String>) {
        orderedTrackIds.forEachIndexed { index, trackId ->
            updateSongSortOrder(playlistId, trackId, index)
        }
    }

    @Transaction
    suspend fun deletePlaylistAndSongs(playlistId: String) {
        deletePlaylistSongs(playlistId)
        deletePlaylistById(playlistId)
    }
}
