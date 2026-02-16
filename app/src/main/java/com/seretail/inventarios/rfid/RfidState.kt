package com.seretail.inventarios.rfid

sealed class RfidState {
    data object Disconnected : RfidState()
    data object Connecting : RfidState()
    data object Connected : RfidState()
    data object Scanning : RfidState()
    data class Error(val message: String) : RfidState()
}
