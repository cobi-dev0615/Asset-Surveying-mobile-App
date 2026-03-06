package com.seretail.inventarios

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.seretail.inventarios.navigation.AppNavigation
import com.seretail.inventarios.ui.theme.SERTheme
import com.seretail.inventarios.util.HardwareScannerBus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val barcodeBuffer = StringBuilder()
    private var lastKeyTime = 0L
    private var keyCount = 0
    private val scannerSpeedThreshold = 50L
    private val minBarcodeLength = 3

    private var crashError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if a previous crash was saved
        val prefs = getSharedPreferences("crash_log", MODE_PRIVATE)
        val savedCrash = prefs.getString("last_crash", null)
        if (savedCrash != null) {
            crashError = savedCrash
            prefs.edit().remove("last_crash").apply()
        }

        setContent {
            if (crashError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(24.dp),
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        Text("App Crash Log", color = Color.Red, fontSize = 20.sp)
                        Text(
                            text = crashError ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        Text(
                            text = "\n\nTap back to continue to app →",
                            color = Color.Gray,
                            fontSize = 14.sp,
                        )
                    }
                }
            } else {
                SERTheme {
                    AppNavigation()
                }
            }
        }
    }

    @Deprecated("Use onBackPressedDispatcher", ReplaceWith(""))
    override fun onBackPressed() {
        if (crashError != null) {
            crashError = null // Dismiss crash log and show normal app
            return
        }
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Only process hardware key-down events (not from soft keyboard)
        if (event.action != KeyEvent.ACTION_DOWN || event.device == null) {
            return super.dispatchKeyEvent(event)
        }

        // Ignore system keys, volume, navigation, etc.
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_BACK ||
            keyCode == KeyEvent.KEYCODE_HOME ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_MENU ||
            keyCode == KeyEvent.KEYCODE_TAB
        ) {
            return super.dispatchKeyEvent(event)
        }

        val now = System.currentTimeMillis()
        val timeSinceLastKey = now - lastKeyTime

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            val barcode = barcodeBuffer.toString().trim()
            // Only emit if we collected enough characters at scanner speed
            if (barcode.length >= minBarcodeLength && keyCount >= minBarcodeLength) {
                HardwareScannerBus.emit(barcode)
                barcodeBuffer.clear()
                keyCount = 0
                lastKeyTime = 0L
                return true
            }
            // Not a scanner barcode — reset buffer and let the event pass through
            barcodeBuffer.clear()
            keyCount = 0
            lastKeyTime = 0L
            return super.dispatchKeyEvent(event)
        }

        val char = event.unicodeChar.toChar()
        if (char.isLetterOrDigit() || char == '-' || char == '.' || char == '/') {
            if (barcodeBuffer.isEmpty()) {
                // First character — start buffering, but let it pass through
                barcodeBuffer.append(char)
                keyCount = 1
                lastKeyTime = now
                return super.dispatchKeyEvent(event)
            }

            if (timeSinceLastKey > 300) {
                // Too slow — this is a new input sequence, reset
                barcodeBuffer.clear()
                barcodeBuffer.append(char)
                keyCount = 1
                lastKeyTime = now
                return super.dispatchKeyEvent(event)
            }

            if (timeSinceLastKey <= scannerSpeedThreshold) {
                // Fast input — likely hardware scanner
                barcodeBuffer.append(char)
                keyCount++
                lastKeyTime = now
                // Consume the event — scanner is typing, don't double-enter in TextField
                return true
            }

            // Medium speed — could be fast human typing, let it through but still buffer
            barcodeBuffer.append(char)
            keyCount++
            lastKeyTime = now
            return super.dispatchKeyEvent(event)
        }

        return super.dispatchKeyEvent(event)
    }
}
