package com.seretail.inventarios.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.LoteDao
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.data.local.entity.LoteEntity
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.InventarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CaptureOptions(
    val usarLote: Boolean = false,
    val usarCaducidad: Boolean = false,
    val usarFactor: Boolean = false,
    val usarSerie: Boolean = false,
    val conteo1a1: Boolean = false,
    val validarCatalogo: Boolean = true,
    val forzados: Boolean = false,
)

data class InventarioCaptureUiState(
    val session: InventarioEntity? = null,
    val registros: List<InventarioRegistroEntity> = emptyList(),
    val barcode: String = "",
    val description: String = "",
    val quantity: String = "1",
    val location: String = "",
    val lote: String = "",
    val caducidad: String = "",
    val factor: String = "",
    val factorResult: String = "",
    val numeroSerie: String = "",
    val availableLotes: List<LoteEntity> = emptyList(),
    val options: CaptureOptions = CaptureOptions(),
    val showOptionsMenu: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null,
    val barcodeNotInCatalog: Boolean = false,
)

@HiltViewModel
class InventarioCaptureViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val authRepository: AuthRepository,
    private val loteDao: LoteDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioCaptureUiState())
    val uiState: StateFlow<InventarioCaptureUiState> = _uiState

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = inventarioRepository.getSession(sessionId)
            _uiState.value = _uiState.value.copy(session = session, isLoading = false)
        }
        viewModelScope.launch {
            inventarioRepository.observeRegistros(sessionId).collect { registros ->
                _uiState.value = _uiState.value.copy(registros = registros)
            }
        }
    }

    fun onBarcodeChanged(v: String) {
        _uiState.value = _uiState.value.copy(barcode = v, barcodeNotInCatalog = false)
    }
    fun onDescriptionChanged(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onQuantityChanged(v: String) {
        _uiState.value = _uiState.value.copy(quantity = v)
        recalculateFactor()
    }
    fun onLocationChanged(v: String) { _uiState.value = _uiState.value.copy(location = v) }
    fun onCaducidadChanged(v: String) { _uiState.value = _uiState.value.copy(caducidad = v) }
    fun onFactorChanged(v: String) {
        _uiState.value = _uiState.value.copy(factor = v)
        recalculateFactor()
    }
    fun onNumeroSerieChanged(v: String) { _uiState.value = _uiState.value.copy(numeroSerie = v) }

    fun onLoteSelected(lote: String) {
        _uiState.value = _uiState.value.copy(lote = lote)
        val match = _uiState.value.availableLotes.find { it.lote == lote }
        if (match?.caducidad != null) {
            _uiState.value = _uiState.value.copy(caducidad = match.caducidad)
        }
    }

    private fun recalculateFactor() {
        val state = _uiState.value
        val qty = state.quantity.toIntOrNull()
        val fac = state.factor.toIntOrNull()
        val result = if (qty != null && fac != null && fac > 0) "$qty × $fac = ${qty * fac}" else ""
        _uiState.value = state.copy(factorResult = result)
    }

    fun onBarcodeScanned(barcode: String) {
        _uiState.value = _uiState.value.copy(barcode = barcode, barcodeNotInCatalog = false)
        val opts = _uiState.value.options

        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val product = inventarioRepository.findProduct(barcode, session.empresaId)

            if (product != null) {
                _uiState.value = _uiState.value.copy(description = product.descripcion)
            } else if (opts.validarCatalogo && !opts.forzados) {
                _uiState.value = _uiState.value.copy(
                    barcodeNotInCatalog = true,
                    message = "Código no encontrado en catálogo",
                )
                return@launch
            }

            if (opts.usarLote) {
                val lotes = loteDao.getByBarcode(barcode)
                _uiState.value = _uiState.value.copy(availableLotes = lotes)
            }

            if (opts.conteo1a1) {
                _uiState.value = _uiState.value.copy(quantity = "1")
                saveRegistro()
            }
        }
    }

    // Options
    fun toggleOptionsMenu() {
        _uiState.value = _uiState.value.copy(showOptionsMenu = !_uiState.value.showOptionsMenu)
    }
    fun toggleLote(v: Boolean) { _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(usarLote = v)) }
    fun toggleCaducidad(v: Boolean) { _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(usarCaducidad = v)) }
    fun toggleFactor(v: Boolean) { _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(usarFactor = v)) }
    fun toggleSerie(v: Boolean) { _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(usarSerie = v)) }
    fun toggleConteo1a1(v: Boolean) { _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(conteo1a1 = v)) }
    fun toggleValidarCatalogo(v: Boolean) { _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(validarCatalogo = v)) }
    fun toggleForzados(v: Boolean) { _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(forzados = v)) }

    fun deleteLocationRecords() {
        val loc = _uiState.value.location
        if (loc.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Ingresa una ubicación primero")
            return
        }
        viewModelScope.launch {
            val toDelete = _uiState.value.registros.filter { it.ubicacion == loc }
            toDelete.forEach { inventarioRepository.deleteRegistro(it.id) }
            _uiState.value = _uiState.value.copy(message = "${toDelete.size} registros eliminados de $loc")
        }
    }

    fun saveRegistro() {
        val state = _uiState.value
        if (state.barcode.isBlank()) {
            _uiState.value = state.copy(message = "Ingresa un código de barras")
            return
        }
        if (state.barcodeNotInCatalog && state.options.validarCatalogo && !state.options.forzados) {
            _uiState.value = state.copy(message = "Código no válido. Activa 'Forzados'.")
            return
        }
        val session = state.session ?: return

        val baseQty = state.quantity.toIntOrNull() ?: 1
        val factorVal = if (state.options.usarFactor) state.factor.toIntOrNull() else null
        val effectiveQty = if (factorVal != null && factorVal > 0) baseQty * factorVal else baseQty

        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val registro = InventarioRegistroEntity(
                sessionId = session.id,
                codigoBarras = state.barcode,
                descripcion = state.description.ifBlank { null },
                cantidad = effectiveQty,
                ubicacion = state.location.ifBlank { null },
                lote = if (state.options.usarLote) state.lote.ifBlank { null } else null,
                caducidad = if (state.options.usarCaducidad) state.caducidad.ifBlank { null } else null,
                factor = factorVal,
                numeroSerie = if (state.options.usarSerie) state.numeroSerie.ifBlank { null } else null,
                fechaCaptura = inventarioRepository.now(),
                usuarioId = user?.id,
            )
            inventarioRepository.saveRegistro(registro)

            _uiState.value = state.copy(
                barcode = "",
                description = "",
                quantity = "1",
                lote = "",
                caducidad = "",
                factor = state.factor,
                factorResult = "",
                numeroSerie = "",
                availableLotes = emptyList(),
                barcodeNotInCatalog = false,
                message = "Guardado (cant: $effectiveQty)",
            )
        }
    }

    fun deleteRegistro(id: Long) {
        viewModelScope.launch { inventarioRepository.deleteRegistro(id) }
    }
}
