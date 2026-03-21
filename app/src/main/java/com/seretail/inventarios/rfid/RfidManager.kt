package com.seretail.inventarios.rfid

import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.rfid.trans.BaseReader
import com.rfid.trans.ReadTag
import com.rfid.trans.TagCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
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
    private var inventoryJob: Job? = null

    companion object {
        private const val TAG = "RfidManager"
        // GPIO path to power on/off the UHF RFID module (Chainway devices)
        private const val GPIO_UHF_POWER = "/proc/gpiocontrol/set_uhf"
        private const val GPIO_ID_POWER = "/proc/gpiocontrol/set_id"
        // Serial port paths by device/SDK version
        private const val PORT_DEFAULT = "/dev/ttyS3"
        private const val PORT_SDK28 = "/dev/ttyS2"
        // Additional ports to try
        private val EXTRA_PORTS = listOf("/dev/ttyS4", "/dev/ttyS1", "/dev/ttyS0")
    }

    /**
     * Power on the RFID module via GPIO before opening serial port.
     * Chainway devices require this step.
     */
    private fun powerOnRfidModule() {
        try {
            val gpioFile = File(GPIO_UHF_POWER)
            if (gpioFile.exists()) {
                FileWriter(gpioFile).use { it.write("1") }
                Log.d(TAG, "RFID GPIO power ON via $GPIO_UHF_POWER")
                // Wait 1.5 seconds for the module to power up
                SystemClock.sleep(1500)
            } else {
                Log.d(TAG, "GPIO path $GPIO_UHF_POWER not found, skipping power-on")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not power on RFID via GPIO: ${e.message}")
        }
        // Also try ID power GPIO
        try {
            val gpioFile = File(GPIO_ID_POWER)
            if (gpioFile.exists()) {
                FileWriter(gpioFile).use { it.write("1") }
                Log.d(TAG, "ID GPIO power ON via $GPIO_ID_POWER")
            }
        } catch (_: Exception) {}
    }

    private fun powerOffRfidModule() {
        try {
            val gpioFile = File(GPIO_UHF_POWER)
            if (gpioFile.exists()) {
                FileWriter(gpioFile).use { it.write("0") }
                Log.d(TAG, "RFID GPIO power OFF")
            }
        } catch (_: Exception) {}
    }

    fun connect(serialPort: String? = null, baudRate: Int = 115200) {
        scope.launch {
            try {
                _state.value = RfidState.Connecting

                // Step 1: Power on the RFID module via GPIO
                powerOnRfidModule()

                // Step 2: Create reader and set callback
                val baseReader = BaseReader()
                baseReader.SetCallBack(object : TagCallback {
                    override fun tagCallback(tag: ReadTag?) {
                        tag?.let {
                            scope.launch { _tags.emit(it) }
                        }
                    }

                    override fun CRCErrorCallBack(i: Int): Int = 0

                    override fun FinishCallBack() {}

                    override fun tagCallbackFailed(i: Int): Int = 0
                })

                // Step 3: Try to connect
                // If specific port given, try it directly
                if (serialPort != null) {
                    val result = baseReader.Connect(serialPort, baudRate, 1)
                    if (result == 0) {
                        SystemClock.sleep(100)
                        reader = baseReader
                        _state.value = RfidState.Connected
                        Log.d(TAG, "Connected on $serialPort @ $baudRate")
                        return@launch
                    }
                }

                // Auto-detect: determine primary port based on SDK version
                val primaryPort = if (Build.VERSION.SDK_INT == 28) PORT_SDK28 else PORT_DEFAULT
                val portsToTry = listOf(primaryPort) + EXTRA_PORTS.filter { it != primaryPort }

                for (port in portsToTry) {
                    try {
                        Log.d(TAG, "Trying $port @ 115200...")
                        val result = baseReader.Connect(port, 115200, 1)
                        if (result == 0) {
                            SystemClock.sleep(100) // Stabilization delay
                            reader = baseReader
                            _state.value = RfidState.Connected
                            Log.d(TAG, "Connected on $port @ 115200")
                            return@launch
                        }
                    } catch (_: Throwable) {
                        // Try next port
                    }
                }

                _state.value = RfidState.Error(
                    "No se pudo conectar al lector RFID. Verifique que el módulo RFID esté activado.",
                )
            } catch (e: Throwable) {
                val msg = when {
                    e is UnsatisfiedLinkError ->
                        "Lector RFID no disponible en este dispositivo"
                    e is ExceptionInInitializerError ||
                        e.message?.contains("SerialPort", ignoreCase = true) == true ->
                        "No se detectó hardware RFID. Conecte el lector e intente nuevamente."
                    else ->
                        "Error de conexión RFID: ${e.javaClass.simpleName}"
                }
                _state.value = RfidState.Error(msg)
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                inventoryJob?.cancel()
                inventoryJob = null
                reader?.DisConnect()
                reader = null
                powerOffRfidModule()
                _state.value = RfidState.Disconnected
            } catch (e: Exception) {
                _state.value = RfidState.Disconnected
            }
        }
    }

    fun startInventory() {
        scope.launch {
            try {
                reader?.let { r ->
                    _state.value = RfidState.Scanning
                    inventoryJob?.cancel()
                    inventoryJob = scope.launch {
                        while (isActive) {
                            val epcData = ByteArray(25600)
                            val epcLen = IntArray(1)
                            val tagCount = IntArray(1)
                            r.Inventory_G2(
                                0xFF.toByte(), // ComAddr (broadcast)
                                4.toByte(),    // Q-value
                                0.toByte(),    // Session
                                0.toByte(),    // TID pointer
                                0.toByte(),    // TID length
                                0.toByte(),    // Session flag
                                0x80.toByte(), // Antenna
                                20,            // Scan time (20ms)
                                epcData, epcLen, tagCount
                            )
                            delay(20) // Interval between scans
                        }
                    }
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
                inventoryJob?.cancel()
                inventoryJob = null
                reader?.let {
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
                reader?.SetRfPower(0xFF.toByte(), currentPower.toByte())
            } catch (_: Exception) {}
        }
    }

    fun getPower(): Int = currentPower
}
