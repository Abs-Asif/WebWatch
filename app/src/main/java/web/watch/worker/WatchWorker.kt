package web.watch.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import web.watch.R
import web.watch.data.AppDatabase
import web.watch.data.HistoryRecord
import web.watch.data.WatchStatus
import web.watch.network.WebFetcher
import java.util.concurrent.TimeUnit

class WatchWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(appContext)
        val dao = db.watchDao()

        val items = dao.getAllWatchItemsSync()
        val currentTime = System.currentTimeMillis()

        for (item in items) {
            if (item.isPaused) continue

            try {
                val res = WebFetcher.checkCompatibility(item.url)
                if (res.isCompatible && res.htmlContent != null) {
                    val newHash = WebFetcher.hashContent(res.htmlContent)
                    val oldHash = item.lastContentHash
                    val oldText = item.lastContentText ?: ""

                    if (oldHash == null) {
                        // First check baseline
                        val updated = item.copy(
                            lastCheckTime = currentTime,
                            lastStatus = WatchStatus.NO_CHANGES,
                            lastContentHash = newHash,
                            lastContentText = res.htmlContent
                        )
                        dao.updateWatchItem(updated)
                    } else if (oldHash != newHash) {
                        // Changes detected!
                        val updated = item.copy(
                            lastCheckTime = currentTime,
                            lastStatus = WatchStatus.CHANGED,
                            lastContentHash = newHash,
                            lastContentText = res.htmlContent
                        )
                        dao.updateWatchItem(updated)

                        val historyRecord = HistoryRecord(
                            watchItemId = item.id,
                            title = "Changes detected",
                            timestamp = currentTime,
                            isError = false,
                            oldContent = oldText,
                            newContent = res.htmlContent
                        )
                        dao.insertHistoryRecord(historyRecord)

                        sendNotification(
                            item.title,
                            "Changes detected on ${item.url}",
                            item.notificationPriority,
                            item.id.toInt()
                        )
                    } else {
                        // No changes
                        val updated = item.copy(
                            lastCheckTime = currentTime,
                            lastStatus = WatchStatus.NO_CHANGES
                        )
                        dao.updateWatchItem(updated)
                    }
                } else {
                    // Failed check
                    val updated = item.copy(
                        lastCheckTime = currentTime,
                        lastStatus = WatchStatus.FAILED
                    )
                    dao.updateWatchItem(updated)

                    val historyRecord = HistoryRecord(
                        watchItemId = item.id,
                        title = "Failed",
                        timestamp = currentTime,
                        isError = true,
                        oldContent = item.lastContentText ?: "",
                        newContent = item.lastContentText ?: "",
                        errorMessage = res.message
                    )
                    dao.insertHistoryRecord(historyRecord)

                    if (item.notificationPriority) {
                        sendNotification(
                            item.title,
                            "Failed to check website: ${res.message}",
                            true,
                            item.id.toInt() + 100000
                        )
                    }
                }
            } catch (e: Exception) {
                val updated = item.copy(
                    lastCheckTime = currentTime,
                    lastStatus = WatchStatus.FAILED
                )
                dao.updateWatchItem(updated)

                val historyRecord = HistoryRecord(
                    watchItemId = item.id,
                    title = "Failed",
                    timestamp = currentTime,
                    isError = true,
                    oldContent = item.lastContentText ?: "",
                    newContent = item.lastContentText ?: "",
                    errorMessage = e.localizedMessage
                )
                dao.insertHistoryRecord(historyRecord)
            }
        }

        return Result.success()
    }

    private fun sendNotification(title: String, text: String, highPriority: Boolean, notificationId: Int) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (highPriority) "webwatch_high_priority" else "webwatch_normal"
        val channelName = if (highPriority) "High Priority Alerts" else "Normal Updates"
        val importance = if (highPriority) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            if (highPriority) {
                channel.enableVibration(true)
                channel.setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(if (highPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        fun scheduleBackgroundWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WatchWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "WebWatchBackgroundWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
