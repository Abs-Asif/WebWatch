package web.watch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WatchStatus {
    CHANGED,
    NO_CHANGES,
    PAUSED,
    FAILED
}

@Entity(tableName = "watch_items")
data class WatchItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val checkFrequencyMinutes: Int = 15,
    val notificationPriority: Boolean = false,
    val isPaused: Boolean = false,
    val lastCheckTime: Long = 0L,
    val lastStatus: WatchStatus = WatchStatus.NO_CHANGES,
    val lastContentHash: String? = null,
    val lastContentText: String? = null
)
