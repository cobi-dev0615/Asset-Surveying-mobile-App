package com.seretail.inventarios.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            mgr.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun playDecode() {
        playTone(ToneGenerator.TONE_PROP_BEEP, 150)
        vibrate(100)
    }

    fun playSuccess() {
        playTone(ToneGenerator.TONE_PROP_ACK, 200)
        vibrate(50)
    }

    fun playError() {
        playTone(ToneGenerator.TONE_PROP_NACK, 300)
        vibrate(500)
    }

    fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGenerator.startTone(toneType, durationMs)
            // Release after tone plays
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                toneGenerator.release()
            }, durationMs.toLong() + 100)
        } catch (_: Exception) {}
    }
}
