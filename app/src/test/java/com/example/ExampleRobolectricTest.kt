package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.LrcParser
import com.example.util.YouTubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Zenith Music", appName)
    }

    @Test
    fun `test lrc parser and active index lookup`() {
        val sampleLrc = """
            [ti:Test Track]
            [ar:Test Artist]
            [00:05.50]First line
            [00:12.00]Second line
            [00:20.500]Third line
        """.trimIndent()

        val parsed = LrcParser.parse(sampleLrc)
        assertEquals(3, parsed.size)
        assertEquals(5500L, parsed[0].timestampMs)
        assertEquals("First line", parsed[0].text)
        assertEquals(12000L, parsed[1].timestampMs)
        assertEquals(20500L, parsed[2].timestampMs)

        // Test active lyric index lookup
        assertEquals(-1, LrcParser.getActiveIndex(parsed, 1000L))
        assertEquals(0, LrcParser.getActiveIndex(parsed, 6000L))
        assertEquals(1, LrcParser.getActiveIndex(parsed, 15000L))
        assertEquals(2, LrcParser.getActiveIndex(parsed, 25000L))
    }

    @Test
    fun `test youtube url parser with various formats`() {
        // Standard watch
        assertEquals("jfKfPfyJRdk", YouTubeUrlParser.extractVideoId("https://www.youtube.com/watch?v=jfKfPfyJRdk"))
        // Short url
        assertEquals("TURbeWK2wwg", YouTubeUrlParser.extractVideoId("https://youtu.be/TURbeWK2wwg?si=12345"))
        // Shorts url
        assertEquals("5qap5aO4i9A", YouTubeUrlParser.extractVideoId("https://www.youtube.com/shorts/5qap5aO4i9A"))
        // Direct ID
        assertEquals("DWcJFNfaw9c", YouTubeUrlParser.extractVideoId("DWcJFNfaw9c"))
    }
}
