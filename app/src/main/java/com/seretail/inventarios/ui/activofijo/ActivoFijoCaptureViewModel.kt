package com.seretail.inventarios.ui.activofijo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.repository.ActivoFijoRepository
import com.seretail.inventarios.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val selectedStatus: Int = 1, // 1=Found
    val isLoading: Boolean = true,
    val message: String? = null,
    // Photo paths (local file URIs)
    val photo1: String? = null,
    val photo2: String? = null,
    val photo3: String? = null,
    val activePhotoSlot: Int = 0,
    // Edit mode
    val editingRegistroId: Long? = null,
    val isEditMode: Boolean = false,
    // Brand autocomplete
    val brandSuggestions: List<String> = emptyList(),
    val showBrandSuggestions: Boolean = false,
    // Category filter
    val categories: List<String> = emptyList(),
    val selectedCategoryFilter: String? = null,
    // Stats
    val capturedCount: Int = 0,
)

@HiltViewModel
class ActivoFijoCaptureViewModel @Inject constructor(
    private val activoFijoRepository: ActivoFijoRepository,
    private val authRepository: AuthRepository,
    private val registroDao: RegistroDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivoFijoCaptureUiState())
    val uiState: StateFlow<ActivoFijoCaptureUiState> = _uiState

    private var allBrands: List<String> = emptyList()

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
        } else {
            emptyList()
        }
        _uiState.value = _uiState.value.copy(
            brand = v,
            brandSuggestions = suggestions,
            showBrandSuggestions = suggestions.isNotEmpty(),
        )
    }

    fun selectBrandSuggestion(brand: String) {
        _uiState.value = _uiState.value.copy(
            brand = brand,
            showBrandSuggestions = false,
        )
    }

    fun dismissBrandSuggestions() {
        _uiState.value = _uiState.value.copy(showBrandSuggestions = false)
    }

    fun onBarcodeScanned(barcode: String) {
        _uiState.value = _uiState.value.copy(barcode = barcode)
        viewModelScope.launch {
            val product = activoFijoRepository.findProduct(barcode)
            if (product != null) {
                _uiState.value = _uiState.value.copy(
                    description = product.descripcion,
                    category = product.categoria ?: "",
                    brand = product.marca ?: "",
                    model = product.modelo ?: "",
                    color = product.color ?: "",
                    serie = product.serie ?: "",
                )
            }

            // Check if asset was already captured (enter edit mode)
            val existing = _uiState.value.registros.find { it.codigoBarras == barcode }
            if (existing != null) {
                enterEditMode(existing)
            }
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

    fun cancelEdit() {
        clearForm()
    }

    // Photo management
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

    // Category filter for registro list
    fun onCategoryFilterChanged(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
    }

    fun getFilteredRegistros(): List<ActivoFijoRegistroEntity> {
        val state = _uiState.value
        val filter = state.selectedCategoryFilter
        return if (filter != null) {
            state.registros.filter { it.categoria == filter }
        } else {
            state.registros
        }
    }

    fun saveRegistro() {
        val state = _uiState.value
        if (state.barcode.isBlank()) {
            _uiState.value = state.copy(message = "Ingresa un código de barras")
            return
        }
        val session = state.session ?: return

        viewModelScope.launch {
            val user = authRepository.getCurrentUser()

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
                    fechaCaptura = activoFijoRepository.now(),
                    usuarioId = user?.id,
                    sincronizado = false,
                )
                registroDao.updateActivoFijo(updated)
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
                    fechaCaptura = activoFijoRepository.now(),
                    usuarioId = user?.id,
                )
                activoFijoRepository.saveRegistro(registro)
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
        viewModelScope.launch {
            activoFijoRepository.deleteRegistro(id)
        }
    }
}
