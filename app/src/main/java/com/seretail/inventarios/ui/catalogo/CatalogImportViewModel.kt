package com.seretail.inventarios.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.LoteDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.entity.LoteEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.data.remote.ApiService
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogImportUiState(
    val isImporting: Boolean = false,
    val progress: Float = 0f,
    val importedCount: Int = 0,
    val totalCount: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val currentPhase: String = "",
    val importComplete: Boolean = false,
    val lotesImported: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class CatalogImportViewModel @Inject constructor(
    private val apiService: ApiService,
    private val productoDao: ProductoDao,
    private val loteDao: LoteDao,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogImportUiState())
    val uiState: StateFlow<CatalogImportUiState> = _uiState

    private var importJob: Job? = null

    fun startImport() {
        if (_uiState.value.isImporting) return

        importJob = viewModelScope.launch {
            _uiState.value = CatalogImportUiState(
                isImporting = true,
                currentPhase = "Conectando con servidor...",
            )

            try {
                val empresaId = preferencesManager.empresaId.first()
                if (empresaId == null || empresaId <= 0L) {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        error = "No se ha seleccionado una empresa",
                    )
                    return@launch
                }

                // Phase 1: Import products
                _uiState.value = _uiState.value.copy(currentPhase = "Descargando productos...")

                productoDao.deleteByEmpresa(empresaId)
                var page = 1
                var totalInserted = 0

                // First page to get total info
                val firstResponse = apiService.getProductos(empresaId, 1)
                if (!firstResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        error = "Error del servidor: ${firstResponse.code()}",
                    )
                    return@launch
                }

                val firstBody = firstResponse.body()!!
                val totalPages = firstBody.lastPage ?: 1
                val totalProducts = firstBody.total ?: (totalPages * firstBody.data.size)

                _uiState.value = _uiState.value.copy(
                    totalCount = totalProducts,
                    totalPages = totalPages,
                )

                // Insert first page
                val firstBatch = firstBody.data.map { it.toEntity() }
                productoDao.insertAll(firstBatch)
                totalInserted += firstBatch.size
                page = 2

                _uiState.value = _uiState.value.copy(
                    currentPage = 1,
                    importedCount = totalInserted,
                    progress = 1f / totalPages,
                )

                // Remaining pages
                while (page <= totalPages) {
                    val response = apiService.getProductos(empresaId, page)
                    if (!response.isSuccessful) break

                    val body = response.body() ?: break
                    val batch = body.data.map { it.toEntity() }
                    productoDao.insertAll(batch)
                    totalInserted += batch.size

                    _uiState.value = _uiState.value.copy(
                        currentPage = page,
                        importedCount = totalInserted,
                        progress = page.toFloat() / totalPages,
                        currentPhase = "Descargando productos... ($totalInserted/$totalProducts)",
                    )

                    page++
                }

                // Phase 2: Import lotes
                _uiState.value = _uiState.value.copy(
                    currentPhase = "Descargando lotes...",
                )

                var lotesImported = 0
                try {
                    val lotesResponse = apiService.getLotes(empresaId)
                    if (lotesResponse.isSuccessful) {
                        val lotes = lotesResponse.body()!!.map {
                            LoteEntity(
                                id = it.id,
                                empresaId = it.empresaId,
                                productoId = it.productoId,
                                codigoBarras = it.codigoBarras,
                                lote = it.lote,
                                caducidad = it.caducidad,
                                existencia = it.existencia,
                            )
                        }
                        loteDao.deleteByEmpresa(empresaId)
                        loteDao.insertAll(lotes)
                        lotesImported = lotes.size
                    }
                } catch (_: Exception) {
                    // Lotes import failure is non-critical
                }

                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importComplete = true,
                    progress = 1f,
                    importedCount = totalInserted,
                    lotesImported = lotesImported,
                    currentPhase = "Importacion completada",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Error: ${e.message}",
                )
            }
        }
    }

    fun cancelImport() {
        importJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isImporting = false,
            currentPhase = "Importacion cancelada",
        )
    }

    fun reset() {
        _uiState.value = CatalogImportUiState()
    }

    private fun com.seretail.inventarios.data.remote.dto.ProductoDto.toEntity() = ProductoEntity(
        id = id,
        empresaId = empresaId,
        codigoBarras = codigoBarras,
        descripcion = descripcion,
        categoria = categoria,
        marca = marca,
        modelo = modelo,
        color = color,
        serie = serie,
        sucursalId = sucursalId,
        codigo2 = codigo2,
        codigo3 = codigo3,
        precioVenta = precioVenta,
        cantidadTeorica = cantidadTeorica,
        unidadMedida = unidadMedida,
        factor = factor,
    )
}
