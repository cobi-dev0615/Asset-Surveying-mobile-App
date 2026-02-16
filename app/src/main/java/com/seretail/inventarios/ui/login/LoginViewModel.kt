package com.seretail.inventarios.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.util.NetworkMonitor
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val usuario: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val preferencesManager: PreferencesManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    val isOnline = networkMonitor.isOnline.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true,
    )

    init {
        viewModelScope.launch {
            preferencesManager.serverUrl.collect { url ->
                _uiState.value = _uiState.value.copy(serverUrl = url)
            }
        }
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                _uiState.value = _uiState.value.copy(isLoggedIn = true)
            }
        }
    }

    fun onServerUrlChanged(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url)
    }

    fun onUsuarioChanged(usuario: String) {
        _uiState.value = _uiState.value.copy(usuario = usuario, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.usuario.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa usuario y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (state.serverUrl.isNotBlank()) {
                preferencesManager.saveServerUrl(state.serverUrl)
            }

            val result = authRepository.login(state.usuario, state.password)
            result.fold(
                onSuccess = {
                    // Download initial data after login
                    try {
                        syncRepository.syncAll()
                    } catch (_: Exception) {
                        // Non-blocking: sync can fail, user still logs in
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message,
                    )
                },
            )
        }
    }
}
