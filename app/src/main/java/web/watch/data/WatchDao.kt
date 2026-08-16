package web.watch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchDao {
    @Query("SELECT * FROM watch_items ORDER BY id DESC")
    fun getAllWatchItems(): Flow<List<WatchItem>>

    @Query("SELECT * FROM watch_items")
    suspend fun getAllWatchItemsSync(): List<WatchItem>

    @Query("SELECT * FROM watch_items WHERE id = :id")
    fun getWatchItemById(id: Long): Flow<WatchItem?>

    @Query("SELECT * FROM watch_items WHERE id = :id")
    suspend fun getWatchItemByIdSync(id: Long): WatchItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchItem(item: WatchItem): Long

    @Update
    suspend fun updateWatchItem(item: WatchItem)

    @Delete
    suspend fun deleteWatchItem(item: WatchItem)

    @Query("SELECT * FROM history_records WHERE watchItemId = :watchItemId ORDER BY timestamp DESC")
    fun getHistoryForWatchItem(watchItemId: Long): Flow<List<HistoryRecord>>

    @Query("SELECT * FROM history_records WHERE watchItemId = :watchItemId ORDER BY timestamp DESC")
    suspend fun getHistoryForWatchItemSync(watchItemId: Long): List<HistoryRecord>

    @Query("SELECT * FROM history_records WHERE id = :id")
    suspend fun getHistoryRecordByIdSync(id: Long): HistoryRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecord(record: HistoryRecord): Long
}
