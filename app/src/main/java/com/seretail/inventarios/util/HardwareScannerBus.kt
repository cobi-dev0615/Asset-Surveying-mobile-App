package com.seretail.inventarios.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global event bus for hardware barcode scanner input.
 * Hardware scanners (like Rugline PDA) send barcodes as rapid keyboard input
 * terminated by ENTER. MainActivity intercepts these and emits here.
 * ViewModels subscribe to receive scanned barcodes on any screen.
 */
object HardwareScannerBus {
    private val _barcodes = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 5,
    )
    val barcodes: SharedFlow<String> = _barcodes.asSharedFlow()

    fun emit(barcode: String) {
        _barcodes.tryEmit(barcode)
    }
}
