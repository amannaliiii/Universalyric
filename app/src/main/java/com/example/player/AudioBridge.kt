package com.example.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface

/**
 * JavaScript interface bridge transmitting events and real-time playback position (ms)
 * to the Android application every 250ms.
 */
class AudioBridge(
    private val onReady: () -> Unit,
    private val onStateChange: (Int) -> Unit,
    private val onTimeUpdate: (Long, Long) -> Unit,
    private val onTrackEnded: () -> Unit,
    private val onError: (Int) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun notifyReady() {
        mainHandler.post {
            Log.d("AudioBridge", "YouTube Player ready")
            onReady()
        }
    }

    @JavascriptInterface
    fun notifyStateChange(state: Int) {
        mainHandler.post {
            // YT.PlayerState: UNSTARTED (-1), ENDED (0), PLAYING (1), PAUSED (2), BUFFERING (3), CUED (5)
            Log.d("AudioBridge", "Player state changed: $state")
            onStateChange(state)
        }
    }

    @JavascriptInterface
    fun notifyTimeUpdate(currentTimeMs: Long, durationMs: Long) {
        mainHandler.post {
            onTimeUpdate(currentTimeMs, durationMs)
        }
    }

    @JavascriptInterface
    fun notifyTrackEnded() {
        mainHandler.post {
            Log.d("AudioBridge", "Track playback ended")
            onTrackEnded()
        }
    }

    @JavascriptInterface
    fun notifyError(errorCode: Int) {
        mainHandler.post {
            Log.e("AudioBridge", "YouTube Player error code: $errorCode")
            onError(errorCode)
        }
    }

    @JavascriptInterface
    fun log(message: String) {
        Log.d("AudioBridge_JS", message)
    }
}
