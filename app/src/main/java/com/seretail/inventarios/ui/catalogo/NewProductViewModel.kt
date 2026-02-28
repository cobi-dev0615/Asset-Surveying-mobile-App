package com.seretail.inventarios.ui.catalogo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScanTarget { CODIGO1, CODIGO2, CODIGO3 }

data class NewProductUiState(
    val codigoBarras: String = "",
    val codigo2: String = "",
    val codigo3: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val marca: String = "",
    val modelo: String = "",
    val color: String = "",
    val serie: String = "",
    val unidadMedida: String = "",
    val precioVenta: String = "",
    val cantidadTeorica: String = "",
    val factor: String = "",
    val isEditMode: Boolean = false,
    val editProductId: Long = 0L,
    val scanTarget: ScanTarget = ScanTarget.CODIGO1,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class NewProductViewModel @Inject constructor(
    private val productoDao: ProductoDao,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewProductUiState())
    val uiState: StateFlow<NewProductUiState> = _uiState

    init {
        val productId = savedStateHandle.get<Long>("productId") ?: 0L
        if (productId > 0L) {
            loadProduct(productId)
        }
    }

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            val product = productoDao.getById(id) ?: return@launch
            _uiState.value = _uiState.value.copy(
                isEditMode = true,
                editProductId = id,
                codigoBarras = product.codigoBarras,
                codigo2 = product.codigo2 ?: "",
                codigo3 = product.codigo3 ?: "",
                descripcion = product.descripcion,
                categoria = product.categoria ?: "",
                marca = product.marca ?: "",
                modelo = product.modelo ?: "",
                color = product.color ?: "",
                serie = product.serie ?: "",
                unidadMedida = product.unidadMedida ?: "",
                precioVenta = product.precioVenta?.toString() ?: "",
                cantidadTeorica = product.cantidadTeorica?.toString() ?: "",
                factor = product.factor?.toString() ?: "",
            )
        }
    }

    fun onCodigoBarrasChanged(value: String) {
        _uiState.value = _uiState.value.copy(codigoBarras = value, error = null)
    }

    fun onCodigo2Changed(value: String) {
        _uiState.value = _uiState.value.copy(codigo2 = value, error = null)
    }

    fun onCodigo3Changed(value: String) {
        _uiState.value = _uiState.value.copy(codigo3 = value, error = null)
    }

    fun onDescripcionChanged(value: String) {
        _uiState.value = _uiState.value.copy(descripcion = value, error = null)
    }

    fun onCategoriaChanged(value: String) {
        _uiState.value = _uiState.value.copy(categoria = value)
    }

    fun onMarcaChanged(value: String) {
        _uiState.value = _uiState.value.copy(marca = value)
    }

    fun onModeloChanged(value: String) {
        _uiState.value = _uiState.value.copy(modelo = value)
    }

    fun onColorChanged(value: String) {
        _uiState.value = _uiState.value.copy(color = value)
    }

    fun onSerieChanged(value: String) {
        _uiState.value = _uiState.value.copy(serie = value)
    }

    fun onUnidadMedidaChanged(value: String) {
        _uiState.value = _uiState.value.copy(unidadMedida = value)
    }

    fun onPrecioVentaChanged(value: String) {
        _uiState.value = _uiState.value.copy(precioVenta = value)
    }

    fun onCantidadTeoricaChanged(value: String) {
        _uiState.value = _uiState.value.copy(cantidadTeorica = value)
    }

    fun onFactorChanged(value: String) {
        _uiState.value = _uiState.value.copy(factor = value)
    }

    fun setScanTarget(target: ScanTarget) {
        _uiState.value = _uiState.value.copy(scanTarget = target)
    }

    fun onBarcodeScanned(barcode: String) {
        when (_uiState.value.scanTarget) {
            ScanTarget.CODIGO1 -> onCodigoBarrasChanged(barcode)
            ScanTarget.CODIGO2 -> onCodigo2Changed(barcode)
            ScanTarget.CODIGO3 -> onCodigo3Changed(barcode)
        }
    }

    fun save() {
        val current = _uiState.value

        if (current.codigoBarras.isBlank()) {
            _uiState.value = current.copy(error = "El codigo de barras es obligatorio")
            return
        }
        if (current.descripcion.isBlank()) {
            _uiState.value = current.copy(error = "La descripcion es obligatoria")
            return
        }

        _uiState.value = current.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val empresaId = preferencesManager.empresaId.first()
                if (empresaId == null) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "No se ha seleccionado una empresa",
                    )
                    return@launch
                }

                val excludeId = if (current.isEditMode) current.editProductId else 0L

                // Check duplicate codes
                val codesToCheck = listOfNotNull(
                    current.codigoBarras.trim().ifBlank { null },
                    current.codigo2.trim().ifBlank { null },
                    current.codigo3.trim().ifBlank { null },
                )
                for (code in codesToCheck) {
                    val count = productoDao.countDuplicateCode(empresaId, code, excludeId)
                    if (count > 0) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = "El codigo '$code' ya existe en otro producto",
                        )
                        return@launch
                    }
                }

                val precioVenta = current.precioVenta.toDoubleOrNull()
                val cantidadTeorica = current.cantidadTeorica.toDoubleOrNull()
                val factor = current.factor.toDoubleOrNull()

                if (current.isEditMode) {
                    productoDao.update(
                        id = current.editProductId,
                        codigoBarras = current.codigoBarras.trim(),
                        codigo2 = current.codigo2.trim().ifBlank { null },
                        codigo3 = current.codigo3.trim().ifBlank { null },
                        descripcion = current.descripcion.trim(),
                        categoria = current.categoria.trim().ifBlank { null },
                        marca = current.marca.trim().ifBlank { null },
                        modelo = current.modelo.trim().ifBlank { null },
                        color = current.color.trim().ifBlank { null },
                        serie = current.serie.trim().ifBlank { null },
                        unidadMedida = current.unidadMedida.trim().ifBlank { null },
                        precioVenta = precioVenta,
                        cantidadTeorica = cantidadTeorica,
                        factor = factor,
                    )
                } else {
                    val product = ProductoEntity(
                        empresaId = empresaId,
                        codigoBarras = current.codigoBarras.trim(),
                        codigo2 = current.codigo2.trim().ifBlank { null },
                        codigo3 = current.codigo3.trim().ifBlank { null },
                        descripcion = current.descripcion.trim(),
                        categoria = current.categoria.trim().ifBlank { null },
                        marca = current.marca.trim().ifBlank { null },
                        modelo = current.modelo.trim().ifBlank { null },
                        color = current.color.trim().ifBlank { null },
                        serie = current.serie.trim().ifBlank { null },
                        unidadMedida = current.unidadMedida.trim().ifBlank { null },
                        precioVenta = precioVenta,
                        cantidadTeorica = cantidadTeorica,
                        factor = factor,
                    )
                    productoDao.insert(product)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error al guardar: ${e.message}",
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
