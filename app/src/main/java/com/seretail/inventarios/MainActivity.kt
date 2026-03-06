package com.seretail.inventarios

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SERTheme {
                AppNavigation()
            }
        }
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
