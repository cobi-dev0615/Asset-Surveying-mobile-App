package com.seretail.inventarios.ui.activofijo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogProduct(
    val producto: ProductoEntity,
    val isCaptured: Boolean,
)

data class AssetCatalogUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val products: List<CatalogProduct> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val totalProducts: Int = 0,
    val capturedProducts: Int = 0,
)

@HiltViewModel
class AssetCatalogViewModel @Inject constructor(
    private val productoDao: ProductoDao,
    private val registroDao: RegistroDao,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetCatalogUiState())
    val uiState: StateFlow<AssetCatalogUiState> = _uiState

    private var sessionId: Long = 0
    private var empresaId: Long = 0
    private var capturedBarcodes: Set<String> = emptySet()

    fun load(sessionId: Long) {
        this.sessionId = sessionId
        viewModelScope.launch {
            empresaId = preferencesManager.empresaId.first() ?: return@launch

            // Get captured barcodes for this session
            val registros = registroDao.getActivoFijoBySession(sessionId)
            capturedBarcodes = registros.map { it.codigoBarras }.toSet()

            // Load categories
            val categories = productoDao.getCategories(empresaId)
            val totalProducts = productoDao.countByEmpresa(empresaId)

            _uiState.value = _uiState.value.copy(
                categories = categories,
                isLoading = false,
                totalProducts = totalProducts,
                capturedProducts = capturedBarcodes.size,
            )
        }
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category, isLoading = true)
        viewModelScope.launch {
            val products = if (category != null) {
                productoDao.getByCategory(empresaId, category)
            } else {
                emptyList()
            }
            _uiState.value = _uiState.value.copy(
                products = products.map {
                    CatalogProduct(it, it.codigoBarras in capturedBarcodes)
                },
                isLoading = false,
            )
        }
    }

    fun onSearchChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length >= 2) {
            viewModelScope.launch {
                val products = productoDao.search(empresaId, query)
                _uiState.value = _uiState.value.copy(
                    products = products.map {
                        CatalogProduct(it, it.codigoBarras in capturedBarcodes)
                    },
                )
            }
        } else if (query.isEmpty()) {
            val cat = _uiState.value.selectedCategory
            if (cat != null) selectCategory(cat)
            else _uiState.value = _uiState.value.copy(products = emptyList())
        }
    }
}
