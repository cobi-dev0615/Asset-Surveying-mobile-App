package com.seretail.inventarios

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.seretail.inventarios.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SERApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        var lastCrashError: String? = null
            private set
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Schedule periodic sync (every 15 min when online)
        SyncScheduler.schedulePeriodicSync(this)

        // Install global crash handler to log before system kills the app
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SERApp", "FATAL CRASH on thread ${thread.name}", throwable)
            // Save crash to shared prefs so we can show it on next launch
            try {
                val prefs = getSharedPreferences("crash_log", MODE_PRIVATE)
                prefs.edit()
                    .putString("last_crash", throwable.stackTraceToString())
                    .commit()
            } catch (_: Exception) {}
            // Let the default handler finish (shows crash dialog / kills app)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
