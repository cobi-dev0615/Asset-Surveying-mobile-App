package com.seretail.inventarios.rfid

import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.lckj.lcrrgxmodule.factory.ILcUhfProduct
import com.lckj.lcrrgxmodule.factory.LcModule
import com.rfid.PowerUtil
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RfidManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<RfidState>(RfidState.Disconnected)
    val state: StateFlow<RfidState> = _state

    private val _tags = MutableSharedFlow<ReadTag>(extraBufferCapacity = 256)
    val tags: SharedFlow<ReadTag> = _tags

    private var uhfProduct: ILcUhfProduct? = null
    private var currentPower: Int = 20
    private var inventoryJob: Job? = null

    companion object {
        private const val TAG = "RfidManager"
        // Serial port paths matching RT501 SDK (Connect232.java)
        private const val PORT_DEFAULT = "/dev/ttyS3"
        private const val PORT_SDK28 = "/dev/ttyS2"
    }

    fun connect() {
        scope.launch {
            try {
                _state.value = RfidState.Connecting

                // Step 1: Power on the RFID module via PowerUtil (from SDK)
                try {
                    PowerUtil.power("1")
                    Log.d(TAG, "PowerUtil.power(1) — module powered ON")
                    SystemClock.sleep(1500)
                } catch (e: Throwable) {
                    Log.w(TAG, "PowerUtil.power failed: ${e.message}")
                }

                // Step 2: Create RFID product via LcModule factory (matches SDK demo)
                val product: ILcUhfProduct = try {
                    // Try with Context first (auto-detects device type)
                    LcModule().createProduct()
                } catch (_: Throwable) {
                    try {
                        // Fallback: RR product (0x10)
                        LcModule().createProduct(0x10)
                    } catch (_: Throwable) {
                        // Fallback: GX product (0x20)
                        LcModule().createProduct(0x20)
                    }
                }

                // Step 3: Set tag callback
                product.SetCallBack(object : TagCallback {
                    override fun tagCallback(tag: ReadTag?) {
                        tag?.let {
                            scope.launch { _tags.emit(it) }
                        }
                    }

                    override fun CRCErrorCallBack(i: Int): Int = 0
                    override fun FinishCallBack() {}
                    override fun tagCallbackFailed(i: Int): Int = 0
                })

                // Step 4: Connect to serial port (matching SDK's Connect232.java logic)
                val port = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) PORT_SDK28 else PORT_DEFAULT
                Log.d(TAG, "Connecting on $port @ 115200...")
                val result = product.Connect(port, 115200)

                if (result == 0) {
                    uhfProduct = product
                    SystemClock.sleep(100)
                    _state.value = RfidState.Connected
                    Log.d(TAG, "Connected successfully on $port")

                    // Set initial power
                    try {
                        product.SetRfPower(currentPower)
                    } catch (_: Throwable) {}
                } else {
                    // Try the other port
                    val altPort = if (port == PORT_DEFAULT) PORT_SDK28 else PORT_DEFAULT
                    Log.d(TAG, "Failed on $port (result=$result), trying $altPort...")
                    val altResult = product.Connect(altPort, 115200)

                    if (altResult == 0) {
                        uhfProduct = product
                        SystemClock.sleep(100)
                        _state.value = RfidState.Connected
                        Log.d(TAG, "Connected successfully on $altPort")
                        try {
                            product.SetRfPower(currentPower)
                        } catch (_: Throwable) {}
                    } else {
                        _state.value = RfidState.Error(
                            "No se pudo conectar al lector RFID (código: $result). Verifique que el módulo RFID esté activado.",
                        )
                    }
                }
            } catch (e: Throwable) {
                val msg = when {
                    e is UnsatisfiedLinkError ->
                        "Lector RFID no disponible en este dispositivo"
                    e is ExceptionInInitializerError ||
                        e.message?.contains("SerialPort", ignoreCase = true) == true ->
                        "No se detectó hardware RFID. Conecte el lector e intente nuevamente."
                    else ->
                        "Error de conexión RFID: ${e.javaClass.simpleName} - ${e.message}"
                }
                Log.e(TAG, "Connection failed", e)
                _state.value = RfidState.Error(msg)
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                inventoryJob?.cancel()
                inventoryJob = null
                uhfProduct?.DisConnect()
                uhfProduct = null
                // Power off module
                try {
                    PowerUtil.power("0")
                    Log.d(TAG, "PowerUtil.power(0) — module powered OFF")
                } catch (_: Throwable) {}
                _state.value = RfidState.Disconnected
            } catch (_: Exception) {
                _state.value = RfidState.Disconnected
            }
        }
    }

    fun startInventory() {
        scope.launch {
            try {
                uhfProduct?.let { product ->
                    _state.value = RfidState.Scanning
                    inventoryJob?.cancel()

                    // Use SDK's StartRead() which handles inventory loop internally
                    // Tags arrive via TagCallback set during connect()
                    val result = product.StartRead()
                    if (result != 0) {
                        Log.w(TAG, "StartRead returned $result")
                    }

                    // Also poll for tags via getInventoryTagMapList() as backup
                    inventoryJob = scope.launch {
                        while (isActive) {
                            try {
                                val tagList = product.getInventoryTagMapList()
                                if (tagList != null && tagList.isNotEmpty()) {
                                    for (tagMap in tagList) {
                                        val tag = ReadTag()
                                        tag.epcId = tagMap.strEPC ?: ""
                                        tag.rssi = tagMap.strRSSI?.toIntOrNull() ?: 0
                                        tag.antId = tagMap.antenna
                                        scope.launch { _tags.emit(tag) }
                                    }
                                }
                            } catch (_: Throwable) {}
                            delay(100)
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
                uhfProduct?.StopRead()
                if (uhfProduct != null) {
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
                uhfProduct?.SetRfPower(currentPower)
            } catch (_: Exception) {}
        }
    }

    fun getPower(): Int = currentPower
}
