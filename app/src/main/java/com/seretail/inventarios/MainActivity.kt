package com.seretail.inventarios

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.seretail.inventarios.navigation.AppNavigation
import com.seretail.inventarios.ui.theme.SERTheme
import com.seretail.inventarios.util.HardwareScannerBus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val barcodeBuffer = StringBuilder()
    private var lastKeyTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SERTheme {
                AppNavigation()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()
            if (now - lastKeyTime > 300) barcodeBuffer.clear()
            lastKeyTime = now

            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val barcode = barcodeBuffer.toString().trim()
                if (barcode.isNotEmpty()) {
                    HardwareScannerBus.emit(barcode)
                    barcodeBuffer.clear()
                }
                return true
            }

            val char = event.unicodeChar.toChar()
            if (char.isLetterOrDigit() || char == '-' || char == '.') {
                barcodeBuffer.append(char)
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
