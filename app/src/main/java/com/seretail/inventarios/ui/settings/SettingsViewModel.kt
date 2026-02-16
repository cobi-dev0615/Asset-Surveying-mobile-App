package com.seretail.inventarios.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = "",
    val autoSync: Boolean = true,
    val syncWifiOnly: Boolean = false,
    val useCamera: Boolean = true,
    val userName: String = "",
    val showLogoutDialog: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            preferencesManager.serverUrl.collect { url ->
                _uiState.value = _uiState.value.copy(serverUrl = url)
            }
        }
        viewModelScope.launch {
            preferencesManager.autoSync.collect { auto ->
                _uiState.value = _uiState.value.copy(autoSync = auto)
            }
        }
        viewModelScope.launch {
            preferencesManager.syncWifiOnly.collect { wifi ->
                _uiState.value = _uiState.value.copy(syncWifiOnly = wifi)
            }
        }
        viewModelScope.launch {
            preferencesManager.useCamera.collect { cam ->
                _uiState.value = _uiState.value.copy(useCamera = cam)
            }
        }
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(userName = user?.nombres ?: "")
        }
    }

    fun onServerUrlChanged(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url)
        viewModelScope.launch { preferencesManager.saveServerUrl(url) }
    }

    fun onAutoSyncChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoSync = enabled)
        viewModelScope.launch { preferencesManager.saveAutoSync(enabled) }
    }

    fun onSyncWifiOnlyChanged(wifiOnly: Boolean) {
        _uiState.value = _uiState.value.copy(syncWifiOnly = wifiOnly)
        viewModelScope.launch { preferencesManager.saveSyncWifiOnly(wifiOnly) }
    }

    fun onUseCameraChanged(useCamera: Boolean) {
        _uiState.value = _uiState.value.copy(useCamera = useCamera)
        viewModelScope.launch { preferencesManager.saveUseCamera(useCamera) }
    }

    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun dismissLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
