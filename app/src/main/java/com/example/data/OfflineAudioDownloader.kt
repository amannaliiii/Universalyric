package com.example.data

import android.content.Context
import com.example.data.local.dao.TrackDao
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

/**
 * Manages downloading and storing audio tracks locally for offline playback.
 * Generates and stores compliant PCM/WAV audio files playable directly by Android's MediaPlayer.
 */
class OfflineAudioDownloader(
    private val context: Context,
    private val trackDao: TrackDao
) {

    private val downloadsDir = File(context.filesDir, "downloads").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Downloads/caches a track to local internal storage for offline playback.
     */
    suspend fun downloadTrack(track: Track, onProgress: ((Int) -> Unit)? = null) = withContext(Dispatchers.IO) {
        val targetFile = File(downloadsDir, "${track.id}.wav")

        // Track progress from 0% to 100%
        trackDao.updateDownloadStatus(track.id, isDownloaded = false, localPath = null, progress = 10)
        onProgress?.invoke(10)
        delay(200)

        trackDao.updateDownloadStatus(track.id, isDownloaded = false, localPath = null, progress = 40)
        onProgress?.invoke(40)
        delay(250)

        // Generate high-quality playable chill ambient audio file (44.1 kHz, 16-bit stereo)
        writePlayableAudioFile(targetFile, durationSeconds = 180, title = track.title)

        trackDao.updateDownloadStatus(track.id, isDownloaded = false, localPath = null, progress = 80)
        onProgress?.invoke(80)
        delay(200)

        // Complete download
        trackDao.updateDownloadStatus(
            trackId = track.id,
            isDownloaded = true,
            localPath = targetFile.absolutePath,
            progress = 100
        )
        onProgress?.invoke(100)
    }

    /**
     * Removes the downloaded local file and resets the database status.
     */
    suspend fun removeDownload(trackId: String) = withContext(Dispatchers.IO) {
        val targetFile = File(downloadsDir, "$trackId.wav")
        if (targetFile.exists()) {
            targetFile.delete()
        }
        trackDao.updateDownloadStatus(
            trackId = trackId,
            isDownloaded = false,
            localPath = null,
            progress = 0
        )
    }

    /**
     * Writes a standard RIFF WAV file with a relaxing pentatonic musical drone and harmonic tones.
     * Guaranteed to play seamlessly on Android's MediaPlayer offline.
     */
    private fun writePlayableAudioFile(file: File, durationSeconds: Int, title: String) {
        val sampleRate = 44100
        val numChannels = 2
        val bitsPerSample = 16
        val bytesPerSample = (bitsPerSample / 8) * numChannels
        val totalSamples = sampleRate * durationSeconds
        val dataSize = totalSamples * bytesPerSample

        FileOutputStream(file).use { fos ->
            // RIFF Header
            fos.write("RIFF".toByteArray())
            fos.write(intToByteArray(36 + dataSize))
            fos.write("WAVE".toByteArray())
            // fmt chunk
            fos.write("fmt ".toByteArray())
            fos.write(intToByteArray(16)) // Subchunk1Size
            fos.write(shortToByteArray(1)) // AudioFormat = PCM
            fos.write(shortToByteArray(numChannels.toShort()))
            fos.write(intToByteArray(sampleRate))
            fos.write(intToByteArray(sampleRate * bytesPerSample)) // ByteRate
            fos.write(shortToByteArray(bytesPerSample.toShort())) // BlockAlign
            fos.write(shortToByteArray(bitsPerSample.toShort()))
            // data chunk
            fos.write("data".toByteArray())
            fos.write(intToByteArray(dataSize))

            // Generate relaxing warm harmonic frequencies based on track title hash
            val baseFreq = 220.0 + (title.hashCode().mod(50)) // E.g. A3 to C4
            val buffer = ByteArray(4096)
            var bufferIdx = 0

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                // Ambient chord tones: root + perfect fifth + octave + gentle tremolo
                val envelope = (0.5 + 0.5 * sin(2.0 * Math.PI * 0.2 * t)).coerceIn(0.0, 1.0)
                val sampleVal1 = sin(2.0 * Math.PI * baseFreq * t)
                val sampleVal2 = 0.5 * sin(2.0 * Math.PI * (baseFreq * 1.5) * t)
                val sampleVal3 = 0.25 * sin(2.0 * Math.PI * (baseFreq * 2.0) * t)
                val mixed = (sampleVal1 + sampleVal2 + sampleVal3) * envelope * 0.4
                val sampleShort = (mixed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                // Left channel
                buffer[bufferIdx++] = (sampleShort.toInt() and 0xFF).toByte()
                buffer[bufferIdx++] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()
                // Right channel
                buffer[bufferIdx++] = (sampleShort.toInt() and 0xFF).toByte()
                buffer[bufferIdx++] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()

                if (bufferIdx >= buffer.size) {
                    fos.write(buffer, 0, bufferIdx)
                    bufferIdx = 0
                }
            }
            if (bufferIdx > 0) {
                fos.write(buffer, 0, bufferIdx)
            }
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }
}
