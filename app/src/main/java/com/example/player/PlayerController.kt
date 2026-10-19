package com.example.player

import android.webkit.WebView
import java.lang.ref.WeakReference

/**
 * Controller allowing the ViewModel and UI to invoke playback commands on the headless WebView.
 */
class PlayerController {

    private var webViewRef: WeakReference<WebView>? = null
    private var isPlayerReady = false
    private var pendingVideoId: String? = null
    private var pendingAutoPlay: Boolean = true

    fun attachWebView(webView: WebView) {
        webViewRef = WeakReference(webView)
    }

    fun detachWebView() {
        webViewRef = null
        isPlayerReady = false
    }

    fun markReady() {
        isPlayerReady = true
        pendingVideoId?.let { videoId ->
            loadVideo(videoId, pendingAutoPlay)
            pendingVideoId = null
        }
    }

    fun loadVideo(videoId: String, autoPlay: Boolean = true) {
        if (!isPlayerReady) {
            pendingVideoId = videoId
            pendingAutoPlay = autoPlay
            return
        }
        val js = "initPlayer('$videoId', $autoPlay);"
        executeJs(js)
    }

    fun play() {
        executeJs("play();")
    }

    fun pause() {
        executeJs("pause();")
    }

    fun seekTo(seconds: Double) {
        executeJs("seekTo($seconds);")
    }

    fun setVolume(volume: Int) {
        val clamped = volume.coerceIn(0, 100)
        executeJs("setVolume($clamped);")
    }

    private fun executeJs(script: String) {
        webViewRef?.get()?.let { webView ->
            webView.post {
                webView.evaluateJavascript(script, null)
            }
        }
    }
}
