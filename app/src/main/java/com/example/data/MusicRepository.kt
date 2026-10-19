package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.TrackEntity
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MusicRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()
    val downloader = OfflineAudioDownloader(context, trackDao)

    companion object {
        val PRELOADED_TRACKS: List<Track> = listOf(
            Track(
                id = "preloaded_1",
                youtubeId = "jfKfPfyJRdk",
                title = "Midnight Rain in Tokyo",
                artist = "Lofi Dreamer",
                album = "Neon Rain",
                durationMs = 175_000L,
                thumbnailUrl = "https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg",
                accentColorHex = "#6366F1",
                isFavorite = true,
                isCustomImported = false,
                lrcLyrics = """
                    [00:00.00]♪ Lo-fi rainfall on neon pavement ♪
                    [00:07.50]Quiet footsteps in the Shibuya mist
                    [00:15.20]Coffee steam rising in the late night glow
                    [00:23.00]Thoughts wandering where the night trains go
                    [00:31.50]Watching reflections of the city lights
                    [00:40.00]Soft piano echoes through the twilight
                    [00:48.50]Take a breath, let the world fade out
                    [00:57.00]Peace is what tonight is all about
                    [01:06.50]Midnight raindrops tapping on the glass
                    [01:15.50]Moments of stillness as the hours pass
                    [01:25.00]Deep inside the rhythm, gentle and slow
                    [01:34.50]Nowhere else you ever need to go
                    [01:44.00]Soft vinyl static spinning round and round
                    [01:54.00]Lost in the comfort of this cozy sound
                    [02:04.50]Midnight rain, take the stress away
                    [02:15.00]Drifting calmly till the break of day
                    [02:28.00]♪ Ambient rain & piano fading out ♪
                """.trimIndent()
            ),
            Track(
                id = "preloaded_2",
                youtubeId = "TURbeWK2wwg",
                title = "Starry Night Drive",
                artist = "CyberSynth Chill",
                album = "Retrowave 84",
                durationMs = 195_000L,
                thumbnailUrl = "https://img.youtube.com/vi/TURbeWK2wwg/hqdefault.jpg",
                accentColorHex = "#38BDF8",
                isFavorite = false,
                isCustomImported = false,
                lrcLyrics = """
                    [00:00.00]♪ Synth arpeggio intro ♪
                    [00:08.50]Cruising down the coastal highway
                    [00:16.80]Distant lights guiding my way
                    [00:25.00]Cool night breeze through the open glass
                    [00:33.20]Watching all the shadows pass
                    [00:42.00]Stars above burning silver and bright
                    [00:50.50]We own the freedom of this midnight flight
                    [01:00.00]Neon horizon calling out my name
                    [01:09.00]The city behind us, never quite the same
                    [01:18.00]Synth bass pulse beating like a heart
                    [01:27.50]This is where the dreams all start
                    [01:37.00]Accelerate into the open blue
                    [01:46.50]Nothing between the sky and you
                    [01:56.00]Starry night drive till the morning dawn
                    [02:07.50]All our worries forever gone
                    [02:20.00]♪ Cosmic synthwave outro ♪
                """.trimIndent()
            ),
            Track(
                id = "preloaded_3",
                youtubeId = "DWcJFNfaw9c",
                title = "Acoustic Forest Sunrise",
                artist = "Elysian Folk",
                album = "Pines & River",
                durationMs = 210_000L,
                thumbnailUrl = "https://img.youtube.com/vi/DWcJFNfaw9c/hqdefault.jpg",
                accentColorHex = "#10B981",
                isFavorite = true,
                isCustomImported = false,
                lrcLyrics = """
                    [00:00.00]♪ Gentle acoustic guitar strumming ♪
                    [00:09.50]First golden light through the cedar trees
                    [00:18.00]A whispered song in the morning breeze
                    [00:27.00]Dewdrops glistening like diamonds on green
                    [00:36.50]The calmest morning you have ever seen
                    [00:46.00]River rushing over polished stone
                    [00:55.00]Never feeling more at home, never alone
                    [01:05.00]Warmth on your face as the day begins
                    [01:15.00]Letting go of where you have been
                    [01:25.00]Step by step along the forest trail
                    [01:35.00]Peaceful spirits never fail
                    [01:46.00]Sunlight dancing across the pines
                    [01:57.00]Resting your weary mind
                    [02:10.00]Golden dawn, carry us home
                    [02:25.00]♪ Warm harmonic guitar outro ♪
                """.trimIndent()
            ),
            Track(
                id = "preloaded_4",
                youtubeId = "5qap5aO4i9A",
                title = "Golden Hour Waves",
                artist = "Aura Ocean",
                album = "Sunset Tides",
                durationMs = 175_000L,
                thumbnailUrl = "https://img.youtube.com/vi/5qap5aO4i9A/hqdefault.jpg",
                accentColorHex = "#F59E0B",
                isFavorite = false,
                isCustomImported = false,
                lrcLyrics = """
                    [00:00.00]♪ Ocean waves & warm electric piano ♪
                    [00:07.50]Sun sinking low on the quiet shore
                    [00:15.50]Couldn't ask for anything more
                    [00:24.00]Golden hues washing over the sand
                    [00:33.00]Time slipping slowly through our hand
                    [00:42.50]Tides roll in, washing burdens away
                    [00:52.00]Goodbye to the noise of yesterday
                    [01:02.00]Breathe with the ocean, calm and deep
                    [01:12.00]Promises the evening keeps
                    [01:23.00]Orange sky turning into indigo
                    [01:34.00]Just let everything go
                    [01:46.00]Golden hour waves beneath our feet
                    [01:58.00]Harmony so pure and sweet
                    [02:12.00]♪ Fading surf and warm sunset chords ♪
                """.trimIndent()
            )
        )
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    private suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        if (trackDao.getTrackCount() == 0) {
            val entities = PRELOADED_TRACKS.map { TrackEntity.fromTrack(it) }
            trackDao.insertTracks(entities)

            // Create initial playlist "Late Night Chill"
            val playlistId = "playlist_default_chill"
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    id = playlistId,
                    title = "Late Night Chill",
                    description = "Warm beats, ambient soundscapes, and acoustic melodies for deep focus.",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            // Add tracks to the default playlist
            PRELOADED_TRACKS.forEachIndexed { index, track ->
                playlistDao.addSongToPlaylist(
                    PlaylistSongCrossRef(
                        playlistId = playlistId,
                        trackId = track.id,
                        sortOrder = index
                    )
                )
            }
        }
    }

    fun getAllTracksFlow(): Flow<List<Track>> {
        return trackDao.getAllTracksFlow().map { entities ->
            entities.map { it.toTrack() }
        }
    }

    fun getFavoriteTracksFlow(): Flow<List<Track>> {
        return trackDao.getFavoriteTracksFlow().map { entities ->
            entities.map { it.toTrack() }
        }
    }

    fun getDownloadedTracksFlow(): Flow<List<Track>> {
        return trackDao.getDownloadedTracksFlow().map { entities ->
            entities.map { it.toTrack() }
        }
    }

    fun searchTracksFlow(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { entities ->
            entities.map { it.toTrack() }
        }
    }

    suspend fun getTrackById(id: String): Track? = withContext(Dispatchers.IO) {
        trackDao.getTrackById(id)?.toTrack()
    }

    suspend fun addTrack(track: Track) = withContext(Dispatchers.IO) {
        trackDao.insertTrack(TrackEntity.fromTrack(track))
    }

    suspend fun deleteTrack(trackId: String) = withContext(Dispatchers.IO) {
        downloader.removeDownload(trackId)
        playlistDao.deleteSongFromAllPlaylists(trackId)
        trackDao.deleteTrackById(trackId)
    }

    suspend fun toggleFavorite(trackId: String) = withContext(Dispatchers.IO) {
        val track = trackDao.getTrackById(trackId)
        if (track != null) {
            val newFav = !track.isFavorite
            trackDao.updateFavorite(trackId, newFav)
        }
    }

    suspend fun downloadTrack(track: Track) = withContext(Dispatchers.IO) {
        downloader.downloadTrack(track)
    }

    suspend fun removeDownload(trackId: String) = withContext(Dispatchers.IO) {
        downloader.removeDownload(trackId)
    }

    // Playlist Management
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAllPlaylistsFlow()
    }

    fun getSongsForPlaylistFlow(playlistId: String): Flow<List<Track>> {
        return playlistDao.getSongsForPlaylistFlow(playlistId).map { entities ->
            entities.map { it.toTrack() }
        }
    }

    suspend fun createPlaylist(title: String, description: String = ""): String = withContext(Dispatchers.IO) {
        val id = "playlist_" + UUID.randomUUID().toString().take(8)
        val playlist = PlaylistEntity(
            id = id,
            title = title.trim().ifEmpty { "My Playlist" },
            description = description.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist)
        id
    }

    suspend fun renamePlaylist(playlistId: String, newTitle: String, newDesc: String) = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        if (playlist != null) {
            playlistDao.updatePlaylist(
                playlist.copy(
                    title = newTitle.trim().ifEmpty { playlist.title },
                    description = newDesc.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistAndSongs(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        val maxOrder = playlistDao.getMaxSortOrder(playlistId) ?: -1
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                sortOrder = maxOrder + 1
            )
        )
        val playlist = playlistDao.getPlaylistById(playlistId)
        if (playlist != null) {
            playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, trackId)
        val playlist = playlistDao.getPlaylistById(playlistId)
        if (playlist != null) {
            playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun reorderPlaylistSongs(playlistId: String, orderedTrackIds: List<String>) = withContext(Dispatchers.IO) {
        playlistDao.reorderPlaylistSongs(playlistId, orderedTrackIds)
        val playlist = playlistDao.getPlaylistById(playlistId)
        if (playlist != null) {
            playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
        }
    }
}
