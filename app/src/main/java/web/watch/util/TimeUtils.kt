package web.watch.util

import android.content.Context
import android.text.format.DateUtils

object TimeUtils {
    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Never"
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp
        if (diffMillis < 60_000) {
            return "Just now"
        }
        val diffMinutes = diffMillis / (60_000)
        if (diffMinutes < 60) {
            return "$diffMinutes minute${if (diffMinutes > 1) "s" else ""} ago"
        }
        val diffHours = diffMinutes / 60
        if (diffHours < 24) {
            return "$diffHours hour${if (diffHours > 1) "s" else ""} ago"
        }
        val diffDays = diffHours / 24
        if (diffDays < 30) {
            return "$diffDays day${if (diffDays > 1) "s" else ""} ago"
        }
        val diffMonths = diffDays / 30
        return "$diffMonths month${if (diffMonths > 1) "s" else ""} ago"
    }

    fun formatHistoryTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val isToday = DateUtils.isToday(timestamp)
        val isYesterday = DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)

        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val dateFormat = java.text.SimpleDateFormat("dd MMMM hh:mm a", java.util.Locale.US)

        return when {
            isToday -> "Today ${timeFormat.format(timestamp)}"
            isYesterday -> "Yesterday ${timeFormat.format(timestamp)}"
            else -> dateFormat.format(timestamp)
        }
    }

    fun formatExactDateTime(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        return dateFormat.format(timestamp)
    }
}
