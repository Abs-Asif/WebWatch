package web.watch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import web.watch.network.WebFetcher
import web.watch.util.TimeUtils

class WebWatchUnitTest {

    @Test
    fun testContentHash() {
        val content = "Hello WebWatch"
        val hash1 = WebFetcher.hashContent(content)
        val hash2 = WebFetcher.hashContent(content)
        val hash3 = WebFetcher.hashContent("Hello WebWatch Changed")

        assertEquals(hash1, hash2)
        assertTrue(hash1 != hash3)
    }

    @Test
    fun testRelativeTimeFormatting() {
        val now = System.currentTimeMillis()
        assertEquals("Just now", TimeUtils.formatRelativeTime(now - 1000))
        assertEquals("5 minutes ago", TimeUtils.formatRelativeTime(now - 5 * 60 * 1000))
        assertEquals("2 hours ago", TimeUtils.formatRelativeTime(now - 2 * 3600 * 1000))
        assertEquals("3 days ago", TimeUtils.formatRelativeTime(now - 3 * 86400 * 1000L))
    }
}
