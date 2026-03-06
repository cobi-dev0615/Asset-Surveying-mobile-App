package com.seretail.inventarios

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SERApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Install global crash handler to log before system kills the app
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SERApp", "FATAL CRASH on thread ${thread.name}", throwable)
            // Let the default handler finish (shows crash dialog / kills app)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
