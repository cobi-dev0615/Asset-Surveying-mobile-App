package com.seretail.inventarios.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.repository.InventarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioListUiState(
    val sessions: List<InventarioEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class InventarioListViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioListUiState())
    val uiState: StateFlow<InventarioListUiState> = _uiState

    init {
        viewModelScope.launch {
            inventarioRepository.observeSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions, isLoading = false)
            }
        }
    }
}
