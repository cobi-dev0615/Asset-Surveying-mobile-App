package com.seretail.inventarios.ui.catalogo

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

data class NewProductUiState(
    val codigoBarras: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val marca: String = "",
    val modelo: String = "",
    val color: String = "",
    val serie: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class NewProductViewModel @Inject constructor(
    private val productoDao: ProductoDao,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewProductUiState())
    val uiState: StateFlow<NewProductUiState> = _uiState

    fun onCodigoBarrasChanged(value: String) {
        _uiState.value = _uiState.value.copy(codigoBarras = value, error = null)
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

    fun save() {
        val current = _uiState.value

        // Validate required fields
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

                val product = ProductoEntity(
                    empresaId = empresaId,
                    codigoBarras = current.codigoBarras.trim(),
                    descripcion = current.descripcion.trim(),
                    categoria = current.categoria.trim().ifBlank { null },
                    marca = current.marca.trim().ifBlank { null },
                    modelo = current.modelo.trim().ifBlank { null },
                    color = current.color.trim().ifBlank { null },
                    serie = current.serie.trim().ifBlank { null },
                )

                productoDao.insertAll(listOf(product))

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
