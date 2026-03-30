package com.seretail.inventarios.ui.activofijo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.ActivoFijoProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.ActivoFijoProductoEntity
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.local.entity.TraspasoEntity
import com.seretail.inventarios.data.repository.ActivoFijoRepository
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.util.FeedbackManager
import com.seretail.inventarios.util.HardwareScannerBus
import com.seretail.inventarios.util.LocationHelper
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
    val comentarios: String = "",
    val tagNuevo: String = "",
    val serieRevisado: String = "",
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
    // Area autocomplete
    val areaSuggestions: List<String> = emptyList(),
    val showAreaSuggestions: Boolean = false,
    // Session stats
    val catalogCount: Int = 0,
    val foundCount: Int = 0,
    val notFoundCount: Int = 0,
    val addedCount: Int = 0,
    val transferredCount: Int = 0,
    val showTransferDialog: Boolean = false,
    val transferOriginSucursalId: Long? = null,
    val transferOriginSucursalName: String? = null,
    // Pending catalog
    val catalogProducts: List<ActivoFijoProductoEntity> = emptyList(),
    val pendingCount: Int = 0,
    val isSyncingCatalog: Boolean = false,
    // Area/Category navigation for pending assets
    val catalogAreas: List<String> = emptyList(),
    val selectedCatalogArea: String? = null,
    val catalogCategoriesForArea: List<String> = emptyList(),
    val selectedCatalogCategory: String? = null,
)

@HiltViewModel
class ActivoFijoCaptureViewModel @Inject constructor(
    private val activoFijoRepository: ActivoFijoRepository,
    private val authRepository: AuthRepository,
    private val registroDao: RegistroDao,
    private val activoFijoProductoDao: ActivoFijoProductoDao,
    private val syncRepository: SyncRepository,
    private val feedbackManager: FeedbackManager,
    private val preferencesManager: PreferencesManager,
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
            try {
                val session = activoFijoRepository.getSession(sessionId)
                _uiState.update {
                    it.copy(
                        session = session,
                        isLoading = false,
                        message = if (session == null) "Sesión no encontrada" else null,
                    )
                }
                if (session != null) {
                    preferencesManager.saveActiveActivoFijoSession(sessionId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Error al cargar sesión: ${e.message}",
                    )
                }
            }
        }
        viewModelScope.launch {
            try {
                activoFijoRepository.observeRegistros(sessionId).collect { registros ->
                    val categories = registros.mapNotNull { it.categoria }.distinct().sorted()
                    val brands = registros.mapNotNull { it.marca }.distinct().sorted()
                    val areas = registros.mapNotNull { it.ubicacion }.distinct().sorted()
                    allBrands = brands
                    allAreas = areas
                    _uiState.update {
                        it.copy(
                            registros = registros,
                            categories = categories,
                            capturedCount = registros.size,
                            foundCount = registros.count { r -> r.statusId == 1 },
                            notFoundCount = registros.count { r -> r.statusId == 2 },
                            addedCount = registros.count { r -> r.statusId == 3 },
                            transferredCount = registros.count { r -> r.statusId == 4 },
                        )
                    }
                }
            } catch (_: Exception) {}
        }
        // Sync existing registros from server (so pending count is accurate)
        viewModelScope.launch {
            try {
                syncRepository.syncActivoFijoRegistros(sessionId)
            } catch (_: Exception) {}
        }
        // Sync and observe catalog (pending assets)
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSyncingCatalog = true) }
                syncRepository.syncActivoFijoProductos(sessionId)
                _uiState.update { it.copy(isSyncingCatalog = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSyncingCatalog = false) }
            }
        }
        viewModelScope.launch {
            try {
                activoFijoProductoDao.observeBySession(sessionId).collect { productos ->
                    val capturedBarcodes = _uiState.value.registros.map { it.codigoBarras }.toSet()
                    val pending = productos.filter { p ->
                        val codes = listOfNotNull(p.codigo1, p.codigo2, p.codigo3)
                        codes.none { it in capturedBarcodes }
                    }
                    val areas = pending.mapNotNull { it.categoria1 }.distinct().sorted()
                    _uiState.update {
                        it.copy(
                            catalogProducts = productos,
                            catalogCount = productos.size,
                            pendingCount = pending.size,
                            catalogAreas = areas,
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun getPendingProducts(): List<ActivoFijoProductoEntity> {
        val state = _uiState.value
        val capturedBarcodes = state.registros.map { it.codigoBarras }.toSet()
        return state.catalogProducts.filter { p ->
            val codes = listOfNotNull(p.codigo1, p.codigo2, p.codigo3)
            val isPending = codes.none { it in capturedBarcodes }
            val matchesArea = state.selectedCatalogArea == null || p.categoria1 == state.selectedCatalogArea
            val matchesCat = state.selectedCatalogCategory == null || p.categoria2 == state.selectedCatalogCategory
            isPending && matchesArea && matchesCat
        }
    }

    fun selectCatalogArea(area: String?) {
        val products = _uiState.value.catalogProducts
        val categories = if (area != null) {
            products.filter { it.categoria1 == area }
                .mapNotNull { it.categoria2 }
                .distinct().sorted()
        } else emptyList()
        _uiState.update {
            it.copy(
                selectedCatalogArea = area,
                selectedCatalogCategory = null,
                catalogCategoriesForArea = categories,
            )
        }
    }

    fun selectCatalogCategory(category: String?) {
        _uiState.update { it.copy(selectedCatalogCategory = category) }
    }

    fun clearCatalogFilters() {
        _uiState.update {
            it.copy(
                selectedCatalogArea = null,
                selectedCatalogCategory = null,
                catalogCategoriesForArea = emptyList(),
            )
        }
    }

    fun selectPendingAsset(producto: ActivoFijoProductoEntity) {
        _uiState.update {
            it.copy(
                barcode = producto.codigo1 ?: "",
                description = producto.descripcion ?: "",
                category = producto.categoria2 ?: producto.categoria1 ?: "",
                brand = producto.marca ?: "",
                model = producto.modelo ?: "",
                serie = producto.nSerie ?: "",
                location = producto.categoria1 ?: "",
                tagNuevo = producto.tagRfid ?: "",
            )
        }
    }

    fun onBarcodeChanged(v: String) { _uiState.value = _uiState.value.copy(barcode = v) }
    fun onDescriptionChanged(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onCategoryChanged(v: String) { _uiState.value = _uiState.value.copy(category = v) }
    fun onModelChanged(v: String) { _uiState.value = _uiState.value.copy(model = v) }
    fun onColorChanged(v: String) { _uiState.value = _uiState.value.copy(color = v) }
    fun onSerieChanged(v: String) { _uiState.value = _uiState.value.copy(serie = v) }
    fun onLocationChanged(v: String) { _uiState.value = _uiState.value.copy(location = v) }
    fun onComentariosChanged(v: String) { _uiState.value = _uiState.value.copy(comentarios = v) }
    fun onTagNuevoChanged(v: String) { _uiState.value = _uiState.value.copy(tagNuevo = v) }
    fun onSerieRevisadoChanged(v: String) { _uiState.value = _uiState.value.copy(serieRevisado = v) }
    fun onStatusChanged(id: Int) { _uiState.value = _uiState.value.copy(selectedStatus = id) }

    private var allAreas: List<String> = emptyList()

    fun onAreaChanged(v: String) {
        val suggestions = if (v.length >= 2) {
            allAreas.filter { it.contains(v, ignoreCase = true) }.take(5)
        } else emptyList()
        _uiState.value = _uiState.value.copy(
            area = v,
            areaSuggestions = suggestions,
            showAreaSuggestions = suggestions.isNotEmpty(),
        )
    }

    fun selectAreaSuggestion(area: String) {
        _uiState.value = _uiState.value.copy(area = area, showAreaSuggestions = false)
    }

    fun dismissAreaSuggestions() {
        _uiState.value = _uiState.value.copy(showAreaSuggestions = false)
    }

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
            comentarios = registro.comentarios ?: "",
            tagNuevo = registro.tagNuevo ?: "",
            serieRevisado = registro.serieRevisado ?: "",
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
                    comentarios = state.comentarios.ifBlank { null },
                    tagNuevo = state.tagNuevo.ifBlank { null },
                    serieRevisado = state.serieRevisado.ifBlank { null },
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
                com.seretail.inventarios.sync.SyncScheduler.syncNow(appContext)
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
                    comentarios = state.comentarios.ifBlank { null },
                    tagNuevo = state.tagNuevo.ifBlank { null },
                    serieRevisado = state.serieRevisado.ifBlank { null },
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
                // Trigger background sync to upload to server
                com.seretail.inventarios.sync.SyncScheduler.syncNow(appContext)
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
            comentarios = "",
            tagNuevo = "",
            serieRevisado = "",
            selectedStatus = 1,
            photo1 = null,
            photo2 = null,
            photo3 = null,
            editingRegistroId = null,
            isEditMode = false,
            showBrandSuggestions = false,
            showAreaSuggestions = false,
            message = null,
        )
    }

    fun deleteRegistro(id: Long) {
        viewModelScope.launch { activoFijoRepository.deleteRegistro(id) }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }

    fun clearPhotoSlot() { _uiState.value = _uiState.value.copy(activePhotoSlot = 0) }

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
                comentarios = state.comentarios.ifBlank { null },
                tagNuevo = state.tagNuevo.ifBlank { null },
                serieRevisado = state.serieRevisado.ifBlank { null },
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
