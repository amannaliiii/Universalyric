package com.example.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val PLAYER_HTML = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
        #player { width: 100%; height: 100%; }
    </style>
</head>
<body>
    <div id="player"></div>
    <script>
        var tag = document.createElement('script');
        tag.src = "https://www.youtube.com/iframe_api";
        var firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

        var player = null;
        var progressTimer = null;
        var isApiReady = false;
        var currentVideoId = "";

        function onYouTubeIframeAPIReady() {
            isApiReady = true;
            if (window.AudioBridge) {
                window.AudioBridge.log("YouTube IFrame API loaded");
                window.AudioBridge.notifyReady();
            }
        }

        function initPlayer(videoId, autoPlay) {
            currentVideoId = videoId;
            if (player && player.loadVideoById) {
                player.loadVideoById({
                    videoId: videoId,
                    startSeconds: 0
                });
                if (!autoPlay) {
                    player.pauseVideo();
                }
                return;
            }

            player = new YT.Player('player', {
                height: '100%',
                width: '100%',
                videoId: videoId,
                playerVars: {
                    'autoplay': autoPlay ? 1 : 0,
                    'controls': 0,
                    'rel': 0,
                    'modestbranding': 1,
                    'playsinline': 1,
                    'fs': 0,
                    'disablekb': 1,
                    'enablejsapi': 1,
                    'origin': 'https://www.youtube.com'
                },
                events: {
                    'onReady': onPlayerReady,
                    'onStateChange': onPlayerStateChange,
                    'onError': onPlayerError
                }
            });
        }

        function onPlayerReady(event) {
            if (window.AudioBridge) {
                window.AudioBridge.log("Player instance ready");
                window.AudioBridge.notifyReady();
            }
            try {
                event.target.unMute();
                event.target.setVolume(100);
            } catch (e) {}
            startProgressTimer();
        }

        function onPlayerStateChange(event) {
            if (window.AudioBridge) {
                window.AudioBridge.notifyStateChange(event.data);
            }
            if (event.data === 1) { // PLAYING
                startProgressTimer();
            } else if (event.data === 0) { // ENDED
                if (window.AudioBridge) {
                    window.AudioBridge.notifyTrackEnded();
                }
            }
        }

        function onPlayerError(event) {
            if (window.AudioBridge) {
                window.AudioBridge.notifyError(event.data);
            }
        }

        function startProgressTimer() {
            if (progressTimer) clearInterval(progressTimer);
            progressTimer = setInterval(function() {
                if (player && player.getCurrentTime) {
                    var currSec = player.getCurrentTime() || 0;
                    var durSec = player.getDuration() || 0;
                    var currMs = Math.round(currSec * 1000);
                    var durMs = Math.round(durSec * 1000);
                    if (window.AudioBridge) {
                        window.AudioBridge.notifyTimeUpdate(currMs, durMs);
                    }
                }
            }, 250);
        }

        function play() {
            if (player && player.playVideo) {
                player.playVideo();
            }
        }

        function pause() {
            if (player && player.pauseVideo) {
                player.pauseVideo();
            }
        }

        function seekTo(seconds) {
            if (player && player.seekTo) {
                player.seekTo(seconds, true);
            }
        }

        function setVolume(volume) {
            if (player && player.setVolume) {
                player.setVolume(volume);
            }
        }
    </script>
</body>
</html>
"""

/**
 * Headless 1x1 dp WebView hosting the YouTube IFrame API audio stream.
 * Hidden from the visual hierarchy with no video overlay.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HeadlessAudioPlayer(
    controller: PlayerController,
    onReady: () -> Unit,
    onStateChange: (Int) -> Unit,
    onTimeUpdate: (Long, Long) -> Unit,
    onTrackEnded: () -> Unit,
    onError: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bridge = remember {
        AudioBridge(
            onReady = onReady,
            onStateChange = onStateChange,
            onTimeUpdate = onTimeUpdate,
            onTrackEnded = onTrackEnded,
            onError = onError
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.detachWebView()
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(1, 1)
                visibility = View.VISIBLE
                setBackgroundColor(Color.TRANSPARENT)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                    allowContentAccess = true
                    loadsImagesAutomatically = false
                    blockNetworkImage = true // Audio-only optimization!
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        controller.attachWebView(this@apply)
                    }
                }

                addJavascriptInterface(bridge, "AudioBridge")
                loadDataWithBaseURL("https://www.youtube.com", PLAYER_HTML, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
            .size(1.dp)
            .alpha(0.01f)
    )
}
