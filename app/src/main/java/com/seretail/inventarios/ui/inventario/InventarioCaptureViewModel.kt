package com.seretail.inventarios.ui.inventario

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.InventarioRepository
import com.seretail.inventarios.util.FeedbackManager
import com.seretail.inventarios.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val isLoading: Boolean = true,
    val message: String? = null,
    val capturedCount: Int = 0,
)

@HiltViewModel
class InventarioCaptureViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val authRepository: AuthRepository,
    private val feedbackManager: FeedbackManager,
    @ApplicationContext private val appContext: Context,
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
                _uiState.value = _uiState.value.copy(
                    registros = registros,
                    capturedCount = registros.size,
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

    fun onBarcodeScanned(barcode: String) {
        feedbackManager.playDecode()
        _uiState.value = _uiState.value.copy(barcode = barcode)
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val product = inventarioRepository.findProduct(barcode, session.empresaId)
            if (product != null) {
                _uiState.value = _uiState.value.copy(
                    description = product.descripcion,
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
            val registro = InventarioRegistroEntity(
                sessionId = session.id,
                codigoBarras = state.barcode,
                descripcion = state.description.ifBlank { null },
                cantidad = state.quantity.toIntOrNull() ?: 1,
                ubicacion = state.location.ifBlank { null },
                lote = state.lot.ifBlank { null },
                caducidad = state.expiry.ifBlank { null },
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
            message = null,
        )
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }
}
