package web.watch

import android.app.Application
import web.watch.worker.WatchWorker

class WebWatchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WatchWorker.scheduleBackgroundWorker(this)
    }
}
