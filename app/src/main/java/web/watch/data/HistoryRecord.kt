package web.watch.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_records",
    foreignKeys = [
        ForeignKey(
            entity = WatchItem::class,
            parentColumns = ["id"],
            childColumns = ["watchItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["watchItemId"])]
)
data class HistoryRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val watchItemId: Long,
    val title: String, // "Changes detected" or "Failed"
    val timestamp: Long,
    val isError: Boolean = false,
    val oldContent: String = "",
    val newContent: String = "",
    val errorMessage: String? = null
)
