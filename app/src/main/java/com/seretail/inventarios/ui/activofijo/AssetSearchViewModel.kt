package com.seretail.inventarios.ui.activofijo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetSearchUiState(
    val searchQuery: String = "",
    val product: ProductoEntity? = null,
    val registro: ActivoFijoRegistroEntity? = null,
    val isSearching: Boolean = false,
    val searched: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class AssetSearchViewModel @Inject constructor(
    private val productoDao: ProductoDao,
    private val registroDao: RegistroDao,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetSearchUiState())
    val uiState: StateFlow<AssetSearchUiState> = _uiState

    private var sessionId: Long = 0

    fun setSession(sessionId: Long) {
        this.sessionId = sessionId
    }

    fun onSearchChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun search() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searched = true)

            // Search in product catalog
            val product = productoDao.findByBarcodeGlobal(query)

            // Search in captured registros for this session
            val registros = registroDao.getActivoFijoBySession(sessionId)
            val registro = registros.find { it.codigoBarras == query }

            _uiState.value = _uiState.value.copy(
                product = product,
                registro = registro,
                isSearching = false,
                message = if (product == null && registro == null) "No se encontró el activo" else null,
            )
        }
    }

    fun onBarcodeScanned(barcode: String) {
        _uiState.value = _uiState.value.copy(searchQuery = barcode)
        search()
    }

    fun clearSearch() {
        _uiState.value = AssetSearchUiState()
    }
}
