package com.seretail.inventarios.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioQueryUiState(
    val searchQuery: String = "",
    val results: List<InventarioRegistroEntity> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class InventarioQueryViewModel @Inject constructor(
    private val registroDao: RegistroDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioQueryUiState())
    val uiState: StateFlow<InventarioQueryUiState> = _uiState

    fun onSearchChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length >= 2) {
            search(query)
        } else if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), hasSearched = false)
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val results = registroDao.searchInventarioRegistros(query)
            _uiState.value = _uiState.value.copy(
                results = results, isLoading = false, hasSearched = true,
            )
        }
    }

    fun deleteRegistro(id: Long) {
        viewModelScope.launch {
            registroDao.deleteInventario(id)
            // Re-search
            val q = _uiState.value.searchQuery
            if (q.length >= 2) search(q)
            _uiState.value = _uiState.value.copy(message = "Registro eliminado")
        }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }
}
