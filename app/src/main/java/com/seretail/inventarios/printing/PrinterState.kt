package com.seretail.inventarios.printing

sealed class PrinterState {
    data object Disconnected : PrinterState()
    data object Connecting : PrinterState()
    data object Connected : PrinterState()
    data object Printing : PrinterState()
    data class Error(val message: String) : PrinterState()
}
