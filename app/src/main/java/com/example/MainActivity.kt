package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.player.HeadlessAudioPlayer
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.ImportScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.theme.ZenithBgDark
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithIndigo
import com.example.ui.theme.ZenithMusicTheme
import com.example.ui.theme.ZenithSurface
import com.example.ui.theme.ZenithTextMuted
import com.example.ui.theme.ZenithTextSecondary
import com.example.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenithMusicTheme {
                ZenithMusicApp()
            }
        }
    }
}

@Composable
fun ZenithMusicApp(
    viewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collect UI States
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackStatus by viewModel.playbackStatus.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val totalDurationMs by viewModel.totalDurationMs.collectAsStateWithLifecycle()
    val parsedLyrics by viewModel.parsedLyrics.collectAsStateWithLifecycle()
    val activeLyricIndex by viewModel.activeLyricIndex.collectAsStateWithLifecycle()
    val isLyricsViewOpen by viewModel.isLyricsViewOpen.collectAsStateWithLifecycle()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var currentNavTab by remember { mutableStateOf(0) } // 0: Library, 1: Import, 2: Playlists
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ZenithBgDark)) {
        // Headless Audio Player (1x1 dp hidden WebView streaming YouTube IFrame audio)
        HeadlessAudioPlayer(
            controller = viewModel.playerController,
            onReady = { viewModel.onPlayerReady() },
            onStateChange = { state -> viewModel.onPlayerStateChange(state) },
            onTimeUpdate = { curr, dur -> viewModel.onTimeUpdate(curr, dur) },
            onTrackEnded = { viewModel.handleTrackEnded() },
            onError = { code -> viewModel.onPlayerError(code) }
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ZenithBgDark,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Mini Player Bar (shown whenever fullscreen now playing is closed)
                    if (!isNowPlayingExpanded && currentTrack != null) {
                        MiniPlayerBar(
                            track = currentTrack,
                            isPlaying = isPlaying,
                            playbackStatus = playbackStatus,
                            currentPositionMs = currentPositionMs,
                            totalDurationMs = totalDurationMs,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.nextTrack() },
                            onToggleFavorite = {
                                currentTrack?.let { viewModel.toggleFavorite(it.id) }
                            },
                            onClick = { viewModel.setNowPlayingExpanded(true) }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = ZenithSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth().testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentNavTab == 0 && selectedPlaylist == null,
                            onClick = {
                                currentNavTab = 0
                                viewModel.setSelectedPlaylist(null)
                                viewModel.setSelectedCategory("All")
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = "Library"
                                )
                            },
                            label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ZenithIndigo,
                                unselectedIconColor = ZenithTextSecondary,
                                unselectedTextColor = ZenithTextMuted
                            ),
                            modifier = Modifier.testTag("nav_library")
                        )

                        NavigationBarItem(
                            selected = currentNavTab == 1 && selectedPlaylist == null,
                            onClick = {
                                currentNavTab = 1
                                viewModel.setSelectedPlaylist(null)
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Import"
                                )
                            },
                            label = { Text("Import", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ZenithIndigo,
                                unselectedIconColor = ZenithTextSecondary,
                                unselectedTextColor = ZenithTextMuted
                            ),
                            modifier = Modifier.testTag("nav_import")
                        )

                        NavigationBarItem(
                            selected = currentNavTab == 2 || selectedCategory == "Playlists",
                            onClick = {
                                currentNavTab = 2
                                viewModel.setSelectedCategory("Playlists")
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = "Playlists"
                                )
                            },
                            label = { Text("Playlists", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ZenithIndigo,
                                unselectedIconColor = ZenithTextSecondary,
                                unselectedTextColor = ZenithTextMuted
                            ),
                            modifier = Modifier.testTag("nav_playlists")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                // If a playlist is selected for viewing
                if (selectedPlaylist != null) {
                    PlaylistDetailScreen(
                        playlist = selectedPlaylist!!,
                        playlistTracks = selectedPlaylistTracks,
                        allAvailableTracks = allTracks,
                        currentTrackId = currentTrack?.id,
                        isPlaying = isPlaying,
                        onBack = { viewModel.setSelectedPlaylist(null) },
                        onPlayTrack = { track -> viewModel.selectTrack(track, autoPlay = true) },
                        onAddSongToPlaylist = { plId, trId -> viewModel.addSongToPlaylist(plId, trId) },
                        onRemoveSongFromPlaylist = { plId, trId -> viewModel.removeSongFromPlaylist(plId, trId) },
                        onReorderSongs = { plId, ids -> viewModel.reorderPlaylist(plId, ids) },
                        onRenamePlaylist = { plId, title, desc -> viewModel.renamePlaylist(plId, title, desc) },
                        onDownloadPlaylist = { plId -> viewModel.downloadEntirePlaylist(plId) }
                    )
                } else {
                    when (currentNavTab) {
                        0, 2 -> {
                            LibraryScreen(
                                tracks = filteredTracks,
                                playlists = allPlaylists,
                                currentTrackId = currentTrack?.id,
                                isPlaying = isPlaying,
                                searchQuery = searchQuery,
                                selectedCategory = if (currentNavTab == 2) "Playlists" else selectedCategory,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onCategorySelected = { cat ->
                                    if (cat == "Playlists") {
                                        currentNavTab = 2
                                    } else {
                                        currentNavTab = 0
                                    }
                                    viewModel.setSelectedCategory(cat)
                                },
                                onTrackSelected = { track -> viewModel.selectTrack(track, autoPlay = true) },
                                onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
                                onDownloadTrack = { track -> viewModel.downloadTrack(track) },
                                onRemoveDownload = { id -> viewModel.removeDownload(id) },
                                onDeleteTrack = { id -> viewModel.deleteTrack(id) },
                                onCreatePlaylist = { title, desc -> viewModel.createPlaylist(title, desc) },
                                onSelectPlaylist = { pl -> viewModel.setSelectedPlaylist(pl) },
                                onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                            )
                        }
                        1 -> {
                            ImportScreen(
                                onImportTrack = { url, title, artist, album, lrc ->
                                    viewModel.importTrack(url, title, artist, album, lrc)
                                },
                                onNavigateToLibrary = { currentNavTab = 0 }
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Now Playing Overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                track = currentTrack,
                isPlaying = isPlaying,
                playbackStatus = playbackStatus,
                currentPositionMs = currentPositionMs,
                totalDurationMs = totalDurationMs,
                lyrics = parsedLyrics,
                activeLyricIndex = activeLyricIndex,
                isLyricsViewOpen = isLyricsViewOpen,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                volume = volume,
                playlists = allPlaylists,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.nextTrack() },
                onPrevious = { viewModel.previousTrack() },
                onSeekTo = { pos -> viewModel.seekTo(pos) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleLyricsView = { viewModel.toggleLyricsView() },
                onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
                onDownloadTrack = { tr -> viewModel.downloadTrack(tr) },
                onRemoveDownload = { id -> viewModel.removeDownload(id) },
                onAddToPlaylist = { plId, trId -> viewModel.addSongToPlaylist(plId, trId) },
                onSetVolume = { vol -> viewModel.setVolume(vol) },
                onCollapse = { viewModel.setNowPlayingExpanded(false) }
            )
        }
    }
}
