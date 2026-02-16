package com.seretail.inventarios.ui.activofijo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.local.entity.TraspasoEntity
import com.seretail.inventarios.data.repository.ActivoFijoRepository
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.util.FeedbackManager
import com.seretail.inventarios.util.HardwareScannerBus
import com.seretail.inventarios.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivoFijoCaptureUiState(
    val session: ActivoFijoSessionEntity? = null,
    val registros: List<ActivoFijoRegistroEntity> = emptyList(),
    val barcode: String = "",
    val description: String = "",
    val category: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val serie: String = "",
    val location: String = "",
    val area: String = "",
    val selectedStatus: Int = 1,
    val isLoading: Boolean = true,
    val message: String? = null,
    val photo1: String? = null,
    val photo2: String? = null,
    val photo3: String? = null,
    val activePhotoSlot: Int = 0,
    val editingRegistroId: Long? = null,
    val isEditMode: Boolean = false,
    val brandSuggestions: List<String> = emptyList(),
    val showBrandSuggestions: Boolean = false,
    val categories: List<String> = emptyList(),
    val selectedCategoryFilter: String? = null,
    val capturedCount: Int = 0,
    val showTransferDialog: Boolean = false,
    val transferOriginSucursalId: Long? = null,
    val transferOriginSucursalName: String? = null,
)

@HiltViewModel
class ActivoFijoCaptureViewModel @Inject constructor(
    private val activoFijoRepository: ActivoFijoRepository,
    private val authRepository: AuthRepository,
    private val registroDao: RegistroDao,
    private val feedbackManager: FeedbackManager,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivoFijoCaptureUiState())
    val uiState: StateFlow<ActivoFijoCaptureUiState> = _uiState

    private var allBrands: List<String> = emptyList()

    init {
        viewModelScope.launch {
            HardwareScannerBus.barcodes.collect { barcode -> onBarcodeScanned(barcode) }
        }
    }

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = activoFijoRepository.getSession(sessionId)
            _uiState.value = _uiState.value.copy(session = session, isLoading = false)
        }
        viewModelScope.launch {
            activoFijoRepository.observeRegistros(sessionId).collect { registros ->
                val categories = registros.mapNotNull { it.categoria }.distinct().sorted()
                val brands = registros.mapNotNull { it.marca }.distinct().sorted()
                allBrands = brands
                _uiState.value = _uiState.value.copy(
                    registros = registros,
                    categories = categories,
                    capturedCount = registros.size,
                )
            }
        }
    }

    fun onBarcodeChanged(v: String) { _uiState.value = _uiState.value.copy(barcode = v) }
    fun onDescriptionChanged(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onCategoryChanged(v: String) { _uiState.value = _uiState.value.copy(category = v) }
    fun onModelChanged(v: String) { _uiState.value = _uiState.value.copy(model = v) }
    fun onColorChanged(v: String) { _uiState.value = _uiState.value.copy(color = v) }
    fun onSerieChanged(v: String) { _uiState.value = _uiState.value.copy(serie = v) }
    fun onLocationChanged(v: String) { _uiState.value = _uiState.value.copy(location = v) }
    fun onAreaChanged(v: String) { _uiState.value = _uiState.value.copy(area = v) }
    fun onStatusChanged(id: Int) { _uiState.value = _uiState.value.copy(selectedStatus = id) }

    fun onBrandChanged(v: String) {
        val suggestions = if (v.length >= 2) {
            allBrands.filter { it.contains(v, ignoreCase = true) }.take(5)
        } else emptyList()
        _uiState.value = _uiState.value.copy(
            brand = v,
            brandSuggestions = suggestions,
            showBrandSuggestions = suggestions.isNotEmpty(),
        )
    }

    fun selectBrandSuggestion(brand: String) {
        _uiState.value = _uiState.value.copy(brand = brand, showBrandSuggestions = false)
    }

    fun dismissBrandSuggestions() {
        _uiState.value = _uiState.value.copy(showBrandSuggestions = false)
    }

    fun onBarcodeScanned(barcode: String) {
        feedbackManager.playDecode()
        _uiState.value = _uiState.value.copy(barcode = barcode)
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch

            val (product, isTransfer) = activoFijoRepository.findProductWithTransferCheck(barcode, session.sucursalId)
            if (product != null) {
                _uiState.value = _uiState.value.copy(
                    description = product.descripcion,
                    category = product.categoria ?: "",
                    brand = product.marca ?: "",
                    model = product.modelo ?: "",
                    color = product.color ?: "",
                    serie = product.serie ?: "",
                )

                if (isTransfer) {
                    _uiState.value = _uiState.value.copy(
                        showTransferDialog = true,
                        transferOriginSucursalId = product.sucursalId,
                        transferOriginSucursalName = null,
                    )
                    return@launch
                }
            }
            val existing = _uiState.value.registros.find { it.codigoBarras == barcode }
            if (existing != null) enterEditMode(existing)
        }
    }

    fun enterEditMode(registro: ActivoFijoRegistroEntity) {
        _uiState.value = _uiState.value.copy(
            editingRegistroId = registro.id,
            isEditMode = true,
            barcode = registro.codigoBarras,
            description = registro.descripcion ?: "",
            category = registro.categoria ?: "",
            brand = registro.marca ?: "",
            model = registro.modelo ?: "",
            color = registro.color ?: "",
            serie = registro.serie ?: "",
            location = registro.ubicacion ?: "",
            selectedStatus = registro.statusId,
            photo1 = registro.imagen1,
            photo2 = registro.imagen2,
            photo3 = registro.imagen3,
        )
    }

    fun cancelEdit() = clearForm()

    fun startPhotoCapture(slot: Int) {
        _uiState.value = _uiState.value.copy(activePhotoSlot = slot)
    }

    fun onPhotoCaptured(uri: Uri) {
        val path = uri.toString()
        val state = _uiState.value
        _uiState.value = when (state.activePhotoSlot) {
            1 -> state.copy(photo1 = path, activePhotoSlot = 0)
            2 -> state.copy(photo2 = path, activePhotoSlot = 0)
            3 -> state.copy(photo3 = path, activePhotoSlot = 0)
            else -> state.copy(activePhotoSlot = 0)
        }
    }

    fun removePhoto(slot: Int) {
        val state = _uiState.value
        _uiState.value = when (slot) {
            1 -> state.copy(photo1 = null)
            2 -> state.copy(photo2 = null)
            3 -> state.copy(photo3 = null)
            else -> state
        }
    }

    fun onCategoryFilterChanged(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
    }

    fun getFilteredRegistros(): List<ActivoFijoRegistroEntity> {
        val state = _uiState.value
        val filter = state.selectedCategoryFilter
        return if (filter != null) state.registros.filter { it.categoria == filter }
        else state.registros
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
            val coords = LocationHelper.getCurrentLocation(appContext)

            if (state.isEditMode && state.editingRegistroId != null) {
                val updated = ActivoFijoRegistroEntity(
                    id = state.editingRegistroId,
                    sessionId = session.id,
                    codigoBarras = state.barcode,
                    descripcion = state.description.ifBlank { null },
                    categoria = state.category.ifBlank { null },
                    marca = state.brand.ifBlank { null },
                    modelo = state.model.ifBlank { null },
                    color = state.color.ifBlank { null },
                    serie = state.serie.ifBlank { null },
                    ubicacion = state.location.ifBlank { null },
                    statusId = state.selectedStatus,
                    imagen1 = state.photo1,
                    imagen2 = state.photo2,
                    imagen3 = state.photo3,
                    latitud = coords?.first,
                    longitud = coords?.second,
                    fechaCaptura = activoFijoRepository.now(),
                    usuarioId = user?.id,
                    sincronizado = false,
                )
                registroDao.updateActivoFijo(updated)
                feedbackManager.playSuccess()
                clearForm()
                _uiState.value = _uiState.value.copy(message = "Activo actualizado")
            } else {
                val registro = ActivoFijoRegistroEntity(
                    sessionId = session.id,
                    codigoBarras = state.barcode,
                    descripcion = state.description.ifBlank { null },
                    categoria = state.category.ifBlank { null },
                    marca = state.brand.ifBlank { null },
                    modelo = state.model.ifBlank { null },
                    color = state.color.ifBlank { null },
                    serie = state.serie.ifBlank { null },
                    ubicacion = state.location.ifBlank { null },
                    statusId = state.selectedStatus,
                    imagen1 = state.photo1,
                    imagen2 = state.photo2,
                    imagen3 = state.photo3,
                    latitud = coords?.first,
                    longitud = coords?.second,
                    fechaCaptura = activoFijoRepository.now(),
                    usuarioId = user?.id,
                )
                activoFijoRepository.saveRegistro(registro)
                feedbackManager.playSuccess()
                clearForm()
                _uiState.value = _uiState.value.copy(message = "Activo guardado")
            }
        }
    }

    private fun clearForm() {
        val state = _uiState.value
        _uiState.value = state.copy(
            barcode = "",
            description = "",
            category = "",
            brand = "",
            model = "",
            color = "",
            serie = "",
            location = state.area.ifBlank { state.location },
            selectedStatus = 1,
            photo1 = null,
            photo2 = null,
            photo3 = null,
            editingRegistroId = null,
            isEditMode = false,
            showBrandSuggestions = false,
            message = null,
        )
    }

    fun deleteRegistro(id: Long) {
        viewModelScope.launch { activoFijoRepository.deleteRegistro(id) }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }

    fun confirmTransfer() {
        val state = _uiState.value
        val session = state.session ?: return

        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val coords = LocationHelper.getCurrentLocation(appContext)

            // Save registro with status = 4 (Transferred)
            val registro = ActivoFijoRegistroEntity(
                sessionId = session.id,
                codigoBarras = state.barcode,
                descripcion = state.description.ifBlank { null },
                categoria = state.category.ifBlank { null },
                marca = state.brand.ifBlank { null },
                modelo = state.model.ifBlank { null },
                color = state.color.ifBlank { null },
                serie = state.serie.ifBlank { null },
                ubicacion = state.location.ifBlank { null },
                statusId = 4, // Transferred
                imagen1 = state.photo1,
                imagen2 = state.photo2,
                imagen3 = state.photo3,
                latitud = coords?.first,
                longitud = coords?.second,
                fechaCaptura = activoFijoRepository.now(),
                usuarioId = user?.id,
            )
            val registroId = activoFijoRepository.saveRegistro(registro)

            // Create traspaso record
            val traspaso = TraspasoEntity(
                registroId = registroId,
                sucursalOrigenId = state.transferOriginSucursalId ?: 0L,
                sucursalDestinoId = session.sucursalId,
                fechaCaptura = activoFijoRepository.now(),
            )
            activoFijoRepository.saveTraspaso(traspaso)

            feedbackManager.playSuccess()
            _uiState.value = _uiState.value.copy(
                showTransferDialog = false,
                transferOriginSucursalId = null,
                transferOriginSucursalName = null,
                message = "Traspaso registrado",
            )
            clearForm()
        }
    }

    fun dismissTransfer() {
        _uiState.value = _uiState.value.copy(
            showTransferDialog = false,
            transferOriginSucursalId = null,
            transferOriginSucursalName = null,
        )
    }
}
