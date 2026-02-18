package com.seretail.inventarios.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val serverUrl: String = "",
    val showServerDialog: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(serverUrl = preferencesManager.serverUrl.first())
        }
    }

    fun onUsuarioChanged(v: String) { _uiState.value = _uiState.value.copy(usuario = v, error = null) }
    fun onPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }
    fun onServerUrlChanged(v: String) { _uiState.value = _uiState.value.copy(serverUrl = v) }
    fun toggleServerDialog() { _uiState.value = _uiState.value.copy(showServerDialog = !_uiState.value.showServerDialog) }

    fun saveServerUrl() {
        viewModelScope.launch {
            preferencesManager.saveServerUrl(_uiState.value.serverUrl)
            _uiState.value = _uiState.value.copy(showServerDialog = false)
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.usuario.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa usuario y contraseña")
            return
        }

        // TODO: Remove fake login before production
        if (state.usuario == "demo" && state.password == "demo") {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                authRepository.fakeLogin()
                _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(state.usuario, state.password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Error de conexión",
                    )
                },
            )
        }
    }
}
