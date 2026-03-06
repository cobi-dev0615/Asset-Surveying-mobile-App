package com.seretail.inventarios

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SERApp : Application() {

    companion object {
        var lastCrashError: String? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

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
