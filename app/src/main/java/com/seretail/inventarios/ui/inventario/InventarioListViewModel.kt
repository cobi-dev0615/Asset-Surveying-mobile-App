package com.seretail.inventarios.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.repository.InventarioRepository
import com.seretail.inventarios.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioListUiState(
    val sessions: List<InventarioEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class InventarioListViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioListUiState())
    val uiState: StateFlow<InventarioListUiState> = _uiState

    init {
        viewModelScope.launch {
            inventarioRepository.observeSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(
                    sessions = sessions,
                    isLoading = false,
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            syncRepository.syncInventarioSessions()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
