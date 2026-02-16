package com.seretail.inventarios.rfid

import com.rfid.trans.BaseReader
import com.rfid.trans.ReadTag
import com.rfid.trans.TagCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RfidManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<RfidState>(RfidState.Disconnected)
    val state: StateFlow<RfidState> = _state

    private val _tags = MutableSharedFlow<ReadTag>(extraBufferCapacity = 256)
    val tags: SharedFlow<ReadTag> = _tags

    private var reader: BaseReader? = null
    private var currentPower: Int = 20

    fun connect(serialPort: String = "/dev/ttyS4", baudRate: Int = 115200) {
        scope.launch {
            try {
                _state.value = RfidState.Connecting
                val baseReader = BaseReader()
                baseReader.setTagCallback(object : TagCallback {
                    override fun tagCallback(tag: ReadTag?) {
                        tag?.let {
                            scope.launch { _tags.emit(it) }
                        }
                    }

                    override fun StopReadCallBack() {}
                })

                val result = baseReader.connect(serialPort, baudRate)
                if (result == 0) {
                    reader = baseReader
                    _state.value = RfidState.Connected
                } else {
                    _state.value = RfidState.Error("No se pudo conectar al lector RFID (código: $result)")
                }
            } catch (e: Exception) {
                _state.value = RfidState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                reader?.let {
                    it.stopInventory()
                    it.disconnect()
                }
                reader = null
                _state.value = RfidState.Disconnected
            } catch (e: Exception) {
                _state.value = RfidState.Disconnected
            }
        }
    }

    fun startInventory() {
        scope.launch {
            try {
                reader?.let {
                    it.startInventory()
                    _state.value = RfidState.Scanning
                } ?: run {
                    _state.value = RfidState.Error("Lector no conectado")
                }
            } catch (e: Exception) {
                _state.value = RfidState.Error("Error al iniciar escaneo: ${e.message}")
            }
        }
    }

    fun stopInventory() {
        scope.launch {
            try {
                reader?.let {
                    it.stopInventory()
                    _state.value = RfidState.Connected
                }
            } catch (e: Exception) {
                _state.value = RfidState.Error("Error al detener escaneo: ${e.message}")
            }
        }
    }

    fun setPower(power: Int) {
        currentPower = power.coerceIn(0, 30)
        scope.launch {
            try {
                reader?.setPower(currentPower)
            } catch (_: Exception) {}
        }
    }

    fun getPower(): Int = currentPower
}
