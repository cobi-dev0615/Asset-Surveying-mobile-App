package com.seretail.inventarios.ui.inventario

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.LoteDao
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.LoteEntity
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.InventarioRepository
import com.seretail.inventarios.util.FeedbackManager
import com.seretail.inventarios.util.HardwareScannerBus
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioCaptureUiState(
    val session: InventarioEntity? = null,
    val registros: List<InventarioRegistroEntity> = emptyList(),
    val barcode: String = "",
    val description: String = "",
    val quantity: String = "1",
    val location: String = "",
    val lot: String = "",
    val expiry: String = "",
    val factor: String = "",
    val serialNumber: String = "",
    val isLoading: Boolean = true,
    val message: String? = null,
    val capturedCount: Int = 0,
    // Lote autocomplete
    val loteSuggestions: List<LoteEntity> = emptyList(),
    val showLoteSuggestions: Boolean = false,
    // Forced code
    val isForcedCode: Boolean = false,
    // Capture option flags (loaded from preferences)
    val showFactor: Boolean = false,
    val showSerial: Boolean = false,
    val showLotes: Boolean = true,
    val allowForced: Boolean = false,
    val validateCatalog: Boolean = true,
    val conteoUnidad: Boolean = true,
    // Stats
    val totalQuantity: Int = 0,
    val totalFactor: Int = 0,
    val registroCount: Int = 0,
)

@HiltViewModel
class InventarioCaptureViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val authRepository: AuthRepository,
    private val feedbackManager: FeedbackManager,
    private val loteDao: LoteDao,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioCaptureUiState())
    val uiState: StateFlow<InventarioCaptureUiState> = _uiState

    init {
        viewModelScope.launch {
            HardwareScannerBus.barcodes.collect { barcode -> onBarcodeScanned(barcode) }
        }
        loadCaptureOptions()
    }

    private fun loadCaptureOptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showFactor = preferencesManager.captureFactor.first(),
                showSerial = preferencesManager.captureSerial.first(),
                showLotes = preferencesManager.captureLotes.first(),
                allowForced = preferencesManager.allowForcedCodes.first(),
                validateCatalog = preferencesManager.validateCatalog.first(),
                conteoUnidad = preferencesManager.conteoUnidad.first(),
            )
        }
    }

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = inventarioRepository.getSession(sessionId)
            _uiState.value = _uiState.value.copy(session = session, isLoading = false)
        }
        viewModelScope.launch {
            inventarioRepository.observeRegistros(sessionId).collect { registros ->
                val totalQty = registros.sumOf { it.cantidad }
                val totalFac = registros.sumOf { it.factor ?: 0 }
                _uiState.value = _uiState.value.copy(
                    registros = registros,
                    capturedCount = registros.size,
                    registroCount = registros.size,
                    totalQuantity = totalQty,
                    totalFactor = totalFac,
                )
            }
        }
    }

    fun onBarcodeChanged(v: String) { _uiState.value = _uiState.value.copy(barcode = v) }
    fun onDescriptionChanged(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onQuantityChanged(v: String) { _uiState.value = _uiState.value.copy(quantity = v) }
    fun onLocationChanged(v: String) { _uiState.value = _uiState.value.copy(location = v) }
    fun onLotChanged(v: String) { _uiState.value = _uiState.value.copy(lot = v) }
    fun onExpiryChanged(v: String) { _uiState.value = _uiState.value.copy(expiry = v) }
    fun onFactorChanged(v: String) { _uiState.value = _uiState.value.copy(factor = v) }
    fun onSerialNumberChanged(v: String) { _uiState.value = _uiState.value.copy(serialNumber = v) }

    fun onLotSelected(lote: LoteEntity) {
        _uiState.value = _uiState.value.copy(
            lot = lote.lote,
            expiry = lote.caducidad ?: _uiState.value.expiry,
            showLoteSuggestions = false,
        )
    }

    fun dismissLoteSuggestions() {
        _uiState.value = _uiState.value.copy(showLoteSuggestions = false)
    }

    fun toggleConteoUnidad() {
        _uiState.value = _uiState.value.copy(conteoUnidad = !_uiState.value.conteoUnidad)
    }

    fun onBarcodeScanned(barcode: String) {
        feedbackManager.playDecode()
        _uiState.value = _uiState.value.copy(barcode = barcode, isForcedCode = false)
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val product = inventarioRepository.findProduct(barcode, session.empresaId)
            if (product != null) {
                _uiState.value = _uiState.value.copy(
                    description = product.descripcion,
                    isForcedCode = false,
                )
            } else if (_uiState.value.allowForced) {
                // Product not in catalog but forced codes allowed
                _uiState.value = _uiState.value.copy(
                    description = "",
                    isForcedCode = true,
                )
            } else if (_uiState.value.validateCatalog) {
                _uiState.value = _uiState.value.copy(
                    description = "",
                    message = "Producto no encontrado en catálogo",
                )
                feedbackManager.playError()
                return@launch
            }

            // Load lote suggestions for this barcode
            if (_uiState.value.showLotes) {
                val lotes = loteDao.getByBarcode(barcode)
                _uiState.value = _uiState.value.copy(
                    loteSuggestions = lotes,
                    showLoteSuggestions = lotes.isNotEmpty(),
                )
            }
        }
    }

    fun saveRegistro() {
        val state = _uiState.value
        if (state.barcode.isBlank()) {
            _uiState.value = state.copy(message = "Ingresa un código de barras")
            feedbackManager.playError()
            return
        }
        val session = state.session ?: return

        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val qty = state.quantity.toIntOrNull() ?: 1
            val fac = state.factor.toIntOrNull()

            val registro = InventarioRegistroEntity(
                sessionId = session.id,
                codigoBarras = state.barcode,
                descripcion = state.description.ifBlank { null },
                cantidad = qty,
                ubicacion = state.location.ifBlank { null },
                lote = state.lot.ifBlank { null },
                caducidad = state.expiry.ifBlank { null },
                factor = fac,
                numeroSerie = state.serialNumber.ifBlank { null },
                fechaCaptura = inventarioRepository.now(),
                usuarioId = user?.id,
            )
            inventarioRepository.saveRegistro(registro)
            feedbackManager.playSuccess()
            clearForm()
            _uiState.value = _uiState.value.copy(message = "Registro guardado")
        }
    }

    fun deleteRegistro(id: Long) {
        viewModelScope.launch {
            inventarioRepository.deleteRegistro(id)
        }
    }

    private fun clearForm() {
        val state = _uiState.value
        _uiState.value = state.copy(
            barcode = "",
            description = "",
            quantity = "1",
            lot = "",
            expiry = "",
            factor = "",
            serialNumber = "",
            isForcedCode = false,
            loteSuggestions = emptyList(),
            showLoteSuggestions = false,
            message = null,
        )
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }
}
