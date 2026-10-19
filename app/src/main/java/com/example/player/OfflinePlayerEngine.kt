package com.example.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

/**
 * Native Android MediaPlayer engine for offline audio playback.
 * Used when a song has been downloaded locally or when device is offline.
 */
class OfflinePlayerEngine(
    private val onTimeUpdate: (Long, Long) -> Unit,
    private val onTrackEnded: () -> Unit,
    private val onStateChange: (Int) -> Unit
) {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var updateRunnable: Runnable? = null

    fun playLocalFile(filePath: String, autoPlay: Boolean = true): Boolean {
        stop()
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("OfflinePlayer", "File does not exist: $filePath")
            return false
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener { mp ->
                    onStateChange(3) // Buffering / Prepared
                    if (autoPlay) {
                        mp.start()
                        this@OfflinePlayerEngine.isPlaying = true
                        onStateChange(1) // Playing
                        startProgressTimer()
                    } else {
                        this@OfflinePlayerEngine.isPlaying = false
                        onStateChange(2) // Paused
                    }
                }
                setOnCompletionListener {
                    this@OfflinePlayerEngine.isPlaying = false
                    stopProgressTimer()
                    onStateChange(0) // Ended
                    onTrackEnded()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("OfflinePlayer", "Error: what=$what extra=$extra")
                    stopProgressTimer()
                    false
                }
                prepareAsync()
            }
            return true
        } catch (e: Exception) {
            Log.e("OfflinePlayer", "Failed to start media player", e)
            return false
        }
    }

    fun play() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                isPlaying = true
                onStateChange(1)
                startProgressTimer()
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                isPlaying = false
                onStateChange(2)
                stopProgressTimer()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            val clamped = positionMs.coerceIn(0L, mp.duration.toLong()).toInt()
            mp.seekTo(clamped)
            onTimeUpdate(clamped.toLong(), mp.duration.toLong())
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clamped, clamped)
    }

    fun stop() {
        stopProgressTimer()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {}
        mediaPlayer = null
        isPlaying = false
    }

    private fun startProgressTimer() {
        stopProgressTimer()
        updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val currentMs = mp.currentPosition.toLong()
                        val durationMs = mp.duration.toLong()
                        onTimeUpdate(currentMs, durationMs)
                        handler.postDelayed(this, 250)
                    }
                }
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopProgressTimer() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }
}
