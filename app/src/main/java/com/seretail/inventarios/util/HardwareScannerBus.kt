package com.seretail.inventarios.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object HardwareScannerBus {
    private val _barcodes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val barcodes: SharedFlow<String> = _barcodes.asSharedFlow()

    fun emit(barcode: String) {
        _barcodes.tryEmit(barcode)
    }
}
