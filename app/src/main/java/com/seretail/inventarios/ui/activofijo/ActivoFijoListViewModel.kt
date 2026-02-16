package com.seretail.inventarios.ui.activofijo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.repository.ActivoFijoRepository
import com.seretail.inventarios.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivoFijoListUiState(
    val sessions: List<ActivoFijoSessionEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class ActivoFijoListViewModel @Inject constructor(
    private val activoFijoRepository: ActivoFijoRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivoFijoListUiState())
    val uiState: StateFlow<ActivoFijoListUiState> = _uiState

    init {
        viewModelScope.launch {
            activoFijoRepository.observeSessions().collect { sessions ->
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
            syncRepository.syncActivoFijoSessions()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
