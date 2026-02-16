package com.seretail.inventarios.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUsuarioChanged(v: String) { _uiState.value = _uiState.value.copy(usuario = v, error = null) }
    fun onPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }

    fun login() {
        val state = _uiState.value
        if (state.usuario.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa usuario y contraseña")
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
