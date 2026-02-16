package com.seretail.inventarios.ui.activofijo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.repository.ActivoFijoRepository
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivoFijoListUiState(
    val sessions: List<ActivoFijoSessionEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false,
    val createdSessionId: Long? = null,
    val error: String? = null,
    val compareMode: Boolean = false,
    val selectedForCompare: Set<Long> = emptySet(),
)

@HiltViewModel
class ActivoFijoListViewModel @Inject constructor(
    private val activoFijoRepository: ActivoFijoRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivoFijoListUiState())
    val uiState: StateFlow<ActivoFijoListUiState> = _uiState

    init {
        viewModelScope.launch {
            activoFijoRepository.observeSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions, isLoading = false)
            }
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

            val result = activoFijoRepository.createSession(nombre, empresaId, sucursalId)
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

    fun toggleCompareMode() {
        val state = _uiState.value
        _uiState.value = state.copy(
            compareMode = !state.compareMode,
            selectedForCompare = emptySet(),
        )
    }

    fun toggleCompareSelection(sessionId: Long) {
        val state = _uiState.value
        val current = state.selectedForCompare
        val updated = if (sessionId in current) {
            current - sessionId
        } else if (current.size < 2) {
            current + sessionId
        } else {
            current
        }
        _uiState.value = state.copy(selectedForCompare = updated)
    }
}
