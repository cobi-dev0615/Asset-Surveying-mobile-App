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

data class ProductCatalogUiState(
    val searchQuery: String = "",
    val products: List<ProductoEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = true,
    val productCount: Int = 0,
)

@HiltViewModel
class ProductCatalogViewModel @Inject constructor(
    private val productoDao: ProductoDao,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductCatalogUiState())
    val uiState: StateFlow<ProductCatalogUiState> = _uiState

    private var empresaId: Long = 0

    init {
        viewModelScope.launch {
            empresaId = preferencesManager.empresaId.first() ?: return@launch

            val count = productoDao.countByEmpresa(empresaId)
            val categories = productoDao.getCategories(empresaId)

            _uiState.value = _uiState.value.copy(
                productCount = count,
                categories = categories,
                isLoading = false,
            )
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, selectedCategory = null)
        if (query.length >= 2) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val products = productoDao.search(empresaId, query)
                _uiState.value = _uiState.value.copy(
                    products = products,
                    isLoading = false,
                )
            }
        } else if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(products = emptyList())
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            searchQuery = "",
            isLoading = true,
        )
        viewModelScope.launch {
            val products = productoDao.getByCategory(empresaId, category)
            _uiState.value = _uiState.value.copy(
                products = products,
                isLoading = false,
            )
        }
    }

    fun loadAll() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            searchQuery = "",
            isLoading = true,
        )
        viewModelScope.launch {
            val products = productoDao.getByEmpresaLimited(empresaId, 200)
            _uiState.value = _uiState.value.copy(
                products = products,
                isLoading = false,
            )
        }
    }

    fun clearFilter() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            searchQuery = "",
            products = emptyList(),
        )
    }
}
