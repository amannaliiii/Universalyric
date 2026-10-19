package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicRepository
import com.example.data.local.entity.PlaylistEntity
import com.example.model.LyricLine
import com.example.model.Track
import com.example.player.OfflinePlayerEngine
import com.example.player.PlayerController
import com.example.util.LrcParser
import com.example.util.YouTubeUrlParser
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class PlaybackStatus {
    IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR
}

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class ActivePlayerType {
    HEADLESS_YOUTUBE,
    OFFLINE_LOCAL
}

@OptIn(FlowPreview::class)
class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    val playerController = PlayerController()

    private val offlinePlayer = OfflinePlayerEngine(
        onTimeUpdate = { currMs, durMs ->
            onTimeUpdate(currMs, durMs)
        },
        onTrackEnded = {
            handleTrackEnded()
        },
        onStateChange = { state ->
            onPlayerStateChange(state)
        }
    )

    private var activePlayerType = ActivePlayerType.HEADLESS_YOUTUBE

    // Tracks & Playlists flows from Room
    val allTracks: StateFlow<List<Track>> = repository.getAllTracksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistEntity>> = repository.getAllPlaylistsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _playbackStatus = MutableStateFlow(PlaybackStatus.IDLE)
    val playbackStatus: StateFlow<PlaybackStatus> = _playbackStatus.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(180_000L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _parsedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val parsedLyrics: StateFlow<List<LyricLine>> = _parsedLyrics.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(-1)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    private val _isLyricsViewOpen = MutableStateFlow(false)
    val isLyricsViewOpen: StateFlow<Boolean> = _isLyricsViewOpen.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Search with Debouncing (300ms)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _debouncedSearchQuery = _searchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Playlist viewing state
    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylist
        .flatMapLatest { playlist ->
            if (playlist != null) {
                repository.getSongsForPlaylistFlow(playlist.id)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Filtered tracks with debounced search query
    val filteredTracks: StateFlow<List<Track>> = combine(
        allTracks,
        _debouncedSearchQuery,
        _selectedCategory
    ) { tracks, query, category ->
        var list = tracks
        when (category) {
            "Favorites" -> list = list.filter { it.isFavorite }
            "Downloaded" -> list = list.filter { it.isDownloaded }
            "Imported" -> list = list.filter { it.isCustomImported }
            "Chill" -> list = list.filter { !it.isCustomImported }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe tracks and select first if none selected
        viewModelScope.launch {
            allTracks.collect { tracks ->
                if (_currentTrack.value == null && tracks.isNotEmpty()) {
                    selectTrack(tracks.first(), autoPlay = false)
                }
            }
        }
    }

    fun selectTrack(track: Track, autoPlay: Boolean = true) {
        _currentTrack.value = track
        _currentPositionMs.value = 0L
        _totalDurationMs.value = if (track.durationMs > 0) track.durationMs else 180_000L
        val lyrics = LrcParser.parse(track.lrcLyrics)
        _parsedLyrics.value = lyrics
        _activeLyricIndex.value = -1

        val hasOfflineFile = track.isDownloaded && track.localFilePath != null && File(track.localFilePath).exists()

        if (hasOfflineFile) {
            activePlayerType = ActivePlayerType.OFFLINE_LOCAL
            playerController.pause()
            val started = offlinePlayer.playLocalFile(track.localFilePath!!, autoPlay = autoPlay)
            if (started) {
                _playbackStatus.value = if (autoPlay) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                _isPlaying.value = autoPlay
            } else {
                fallbackToOnlineStream(track, autoPlay)
            }
        } else {
            fallbackToOnlineStream(track, autoPlay)
        }
    }

    private fun fallbackToOnlineStream(track: Track, autoPlay: Boolean) {
        activePlayerType = ActivePlayerType.HEADLESS_YOUTUBE
        offlinePlayer.stop()
        if (autoPlay) {
            _playbackStatus.value = PlaybackStatus.BUFFERING
            _isPlaying.value = true
            playerController.loadVideo(track.youtubeId, autoPlay = true)
        } else {
            _playbackStatus.value = PlaybackStatus.PAUSED
            _isPlaying.value = false
            playerController.loadVideo(track.youtubeId, autoPlay = false)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            if (activePlayerType == ActivePlayerType.OFFLINE_LOCAL) {
                offlinePlayer.pause()
            } else {
                playerController.pause()
            }
            _isPlaying.value = false
            _playbackStatus.value = PlaybackStatus.PAUSED
        } else {
            val current = _currentTrack.value
            if (current != null) {
                if (activePlayerType == ActivePlayerType.OFFLINE_LOCAL) {
                    offlinePlayer.play()
                } else {
                    playerController.play()
                }
                _isPlaying.value = true
                _playbackStatus.value = PlaybackStatus.PLAYING
            } else if (allTracks.value.isNotEmpty()) {
                selectTrack(allTracks.value.first(), autoPlay = true)
            }
        }
    }

    fun nextTrack() {
        val tracks = if (_selectedPlaylist.value != null && selectedPlaylistTracks.value.isNotEmpty()) {
            selectedPlaylistTracks.value
        } else {
            allTracks.value
        }
        if (tracks.isEmpty()) return
        val current = _currentTrack.value

        if (_isShuffle.value) {
            val candidates = tracks.filter { it.id != current?.id }
            val next = if (candidates.isNotEmpty()) candidates.random() else tracks.first()
            selectTrack(next, autoPlay = true)
            return
        }

        val currentIndex = tracks.indexOfFirst { it.id == current?.id }
        val nextIndex = if (currentIndex != -1 && currentIndex < tracks.size - 1) {
            currentIndex + 1
        } else {
            0
        }
        selectTrack(tracks[nextIndex], autoPlay = true)
    }

    fun previousTrack() {
        val tracks = if (_selectedPlaylist.value != null && selectedPlaylistTracks.value.isNotEmpty()) {
            selectedPlaylistTracks.value
        } else {
            allTracks.value
        }
        if (tracks.isEmpty()) return
        val current = _currentTrack.value

        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val currentIndex = tracks.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            tracks.size - 1
        }
        selectTrack(tracks[prevIndex], autoPlay = true)
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _totalDurationMs.value)
        _currentPositionMs.value = clamped
        _activeLyricIndex.value = LrcParser.getActiveIndex(_parsedLyrics.value, clamped)

        if (activePlayerType == ActivePlayerType.OFFLINE_LOCAL) {
            offlinePlayer.seekTo(clamped)
        } else {
            val seconds = clamped / 1000.0
            playerController.seekTo(seconds)
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleLyricsView() {
        _isLyricsViewOpen.value = !_isLyricsViewOpen.value
    }

    fun setLyricsView(open: Boolean) {
        _isLyricsViewOpen.value = open
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        offlinePlayer.setVolume(clamped)
        playerController.setVolume((clamped * 100).toInt())
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    fun toggleFavorite(trackId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId)
            _currentTrack.value?.let { current ->
                if (current.id == trackId) {
                    _currentTrack.value = current.copy(isFavorite = !current.isFavorite)
                }
            }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            _userMessage.value = "Downloading \"${track.title}\" for offline listening..."
            try {
                repository.downloadTrack(track)
                _userMessage.value = "\"${track.title}\" downloaded successfully for offline listening!"
                if (_currentTrack.value?.id == track.id) {
                    val updated = repository.getTrackById(track.id)
                    if (updated != null) _currentTrack.value = updated
                }
            } catch (e: Exception) {
                _userMessage.value = "Failed to download track: ${e.localizedMessage}"
            }
        }
    }

    fun removeDownload(trackId: String) {
        viewModelScope.launch {
            repository.removeDownload(trackId)
            _userMessage.value = "Removed offline download."
            if (_currentTrack.value?.id == trackId) {
                val updated = repository.getTrackById(trackId)
                if (updated != null) _currentTrack.value = updated
            }
        }
    }

    // Playlist Operations
    fun createPlaylist(title: String, description: String = "") {
        viewModelScope.launch {
            val id = repository.createPlaylist(title, description)
            _userMessage.value = "Playlist created!"
        }
    }

    fun renamePlaylist(playlistId: String, newTitle: String, newDesc: String) {
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newTitle, newDesc)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = _selectedPlaylist.value?.copy(
                    title = newTitle,
                    description = newDesc
                )
            }
            _userMessage.value = "Playlist updated!"
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
            _userMessage.value = "Playlist deleted."
        }
    }

    fun addSongToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, trackId)
            _userMessage.value = "Added to playlist!"
        }
    }

    fun removeSongFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, trackId)
            _userMessage.value = "Removed from playlist."
        }
    }

    fun reorderPlaylist(playlistId: String, orderedTrackIds: List<String>) {
        viewModelScope.launch {
            repository.reorderPlaylistSongs(playlistId, orderedTrackIds)
        }
    }

    fun downloadEntirePlaylist(playlistId: String) {
        viewModelScope.launch {
            val tracks = selectedPlaylistTracks.value
            _userMessage.value = "Downloading ${tracks.size} tracks for offline playback..."
            for (track in tracks) {
                if (!track.isDownloaded) {
                    repository.downloadTrack(track)
                }
            }
            _userMessage.value = "Playlist offline download completed!"
        }
    }

    fun importTrack(
        urlOrId: String,
        title: String,
        artist: String,
        album: String,
        customLrc: String
    ): Boolean {
        val videoId = YouTubeUrlParser.extractVideoId(urlOrId)
        if (videoId == null) {
            _userMessage.value = "Invalid YouTube URL or Video ID. Please check and try again."
            return false
        }

        val finalTitle = title.trim().ifEmpty { "Imported Track $videoId" }
        val finalArtist = artist.trim().ifEmpty { "YouTube Artist" }
        val finalAlbum = album.trim().ifEmpty { "Imported" }
        val finalLrc = if (customLrc.isNotBlank()) {
            customLrc.trim()
        } else {
            YouTubeUrlParser.generateSampleLrc(finalTitle, finalArtist, 180)
        }

        val track = Track(
            id = "custom_" + System.currentTimeMillis(),
            youtubeId = videoId,
            title = finalTitle,
            artist = finalArtist,
            album = finalAlbum,
            durationMs = 180_000L,
            thumbnailUrl = YouTubeUrlParser.getThumbnailUrl(videoId),
            lrcLyrics = finalLrc,
            accentColorHex = "#A855F7",
            isFavorite = false,
            isCustomImported = true
        )

        viewModelScope.launch {
            repository.addTrack(track)
            selectTrack(track, autoPlay = true)
        }
        _userMessage.value = "Successfully imported \"$finalTitle\"!"
        return true
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            repository.deleteTrack(trackId)
            if (_currentTrack.value?.id == trackId) {
                val remaining = allTracks.value.filter { it.id != trackId }
                if (remaining.isNotEmpty()) {
                    selectTrack(remaining.first(), autoPlay = false)
                } else {
                    _currentTrack.value = null
                    _isPlaying.value = false
                }
            }
            _userMessage.value = "Track removed from library."
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Bridge callbacks
    fun onPlayerReady() {
        playerController.markReady()
    }

    fun onPlayerStateChange(state: Int) {
        when (state) {
            1 -> {
                _isPlaying.value = true
                _playbackStatus.value = PlaybackStatus.PLAYING
            }
            2 -> {
                _isPlaying.value = false
                _playbackStatus.value = PlaybackStatus.PAUSED
            }
            3 -> {
                _playbackStatus.value = PlaybackStatus.BUFFERING
            }
            0 -> {
                handleTrackEnded()
            }
            -1 -> {
                _playbackStatus.value = PlaybackStatus.IDLE
            }
        }
    }

    fun onTimeUpdate(currentTimeMs: Long, durationMs: Long) {
        _currentPositionMs.value = currentTimeMs
        if (durationMs > 0 && durationMs != _totalDurationMs.value) {
            _totalDurationMs.value = durationMs
        }
        _activeLyricIndex.value = LrcParser.getActiveIndex(_parsedLyrics.value, currentTimeMs)
    }

    fun handleTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0L)
                if (activePlayerType == ActivePlayerType.OFFLINE_LOCAL) {
                    offlinePlayer.play()
                } else {
                    playerController.play()
                }
            }
            RepeatMode.ALL -> {
                nextTrack()
            }
            RepeatMode.OFF -> {
                val tracks = if (_selectedPlaylist.value != null && selectedPlaylistTracks.value.isNotEmpty()) {
                    selectedPlaylistTracks.value
                } else {
                    allTracks.value
                }
                val currentIndex = tracks.indexOfFirst { it.id == _currentTrack.value?.id }
                if (currentIndex != -1 && currentIndex < tracks.size - 1) {
                    nextTrack()
                } else {
                    _isPlaying.value = false
                    _playbackStatus.value = PlaybackStatus.ENDED
                }
            }
        }
    }

    fun onPlayerError(errorCode: Int) {
        _playbackStatus.value = PlaybackStatus.ERROR
        _isPlaying.value = false
        val message = when (errorCode) {
            2 -> "Invalid video ID parameter."
            5 -> "HTML5 player error."
            100 -> "Video not found or removed."
            101, 150 -> "Owner does not allow embedded playback."
            else -> "Playback error (code $errorCode)"
        }
        _userMessage.value = message
    }

    override fun onCleared() {
        super.onCleared()
        offlinePlayer.stop()
    }
}
