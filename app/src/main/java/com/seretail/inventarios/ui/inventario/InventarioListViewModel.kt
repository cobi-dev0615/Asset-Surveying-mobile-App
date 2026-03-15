package com.seretail.inventarios.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.repository.InventarioRepository
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioListUiState(
    val sessions: List<InventarioEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false,
    val createdSessionId: Long? = null,
    val error: String? = null,
)

@HiltViewModel
class InventarioListViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val syncRepository: SyncRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioListUiState())
    val uiState: StateFlow<InventarioListUiState> = _uiState

    init {
        // Observe local DB filtered by selected empresa/sucursal
        viewModelScope.launch {
            val empresaId = preferencesManager.empresaId.first()
            val sucursalId = preferencesManager.sucursalId.first()
            inventarioRepository.observeSessions().collect { sessions ->
                val filtered = sessions.filter { s ->
                    (empresaId == null || s.empresaId == empresaId) &&
                        (sucursalId == null || s.sucursalId == sucursalId)
                }
                _uiState.value = _uiState.value.copy(sessions = filtered, isLoading = false)
            }
        }
        // Sync sessions from server
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                syncRepository.syncInventarioSessions()
            } catch (_: Exception) {}
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, error = null)
    }

    fun dismissCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false, error = null)
    }

    fun createSession(nombre: String) {
        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa un nombre")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val empresaId = preferencesManager.empresaId.first() ?: 1L
            val sucursalId = preferencesManager.sucursalId.first() ?: 1L

            val result = inventarioRepository.createSession(nombre, empresaId, sucursalId)
            result.fold(
                onSuccess = { entity ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        showCreateDialog = false,
                        createdSessionId = entity.id,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = it.message ?: "Error al crear sesión",
                    )
                },
            )
        }
    }

    fun clearCreatedSession() {
        _uiState.value = _uiState.value.copy(createdSessionId = null)
    }
}
