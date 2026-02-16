package com.seretail.inventarios.printing

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothPrinterManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<PrinterState>(PrinterState.Disconnected)
    val state: StateFlow<PrinterState> = _state

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(macAddress: String) {
        scope.launch {
            try {
                _state.value = PrinterState.Connecting
                disconnect()

                val device = bluetoothAdapter?.getRemoteDevice(macAddress)
                    ?: throw IllegalStateException("Dispositivo no encontrado")

                val btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                btSocket.connect()
                socket = btSocket
                outputStream = btSocket.outputStream
                _state.value = PrinterState.Connected
            } catch (e: Exception) {
                _state.value = PrinterState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (_: Exception) {}
        outputStream = null
        socket = null
        _state.value = PrinterState.Disconnected
    }

    suspend fun print(data: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val os = outputStream ?: return@withContext false
                _state.value = PrinterState.Printing
                os.write(data)
                os.flush()
                _state.value = PrinterState.Connected
                true
            } catch (e: Exception) {
                _state.value = PrinterState.Error("Error al imprimir: ${e.message}")
                false
            }
        }
    }

    fun isConnected(): Boolean = socket?.isConnected == true
}
