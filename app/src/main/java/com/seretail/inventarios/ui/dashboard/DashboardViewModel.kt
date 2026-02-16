package com.seretail.inventarios.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.util.NetworkMonitor
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val empresaNombre: String? = null,
    val sucursalNombre: String? = null,
    val totalSessions: Int = 0,
    val totalRegistros: Int = 0,
    val pendingSync: Int = 0,
    val lastSync: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    // Activo Fijo status breakdown
    val afFound: Int = 0,
    val afNotFound: Int = 0,
    val afAdded: Int = 0,
    val afTransferred: Int = 0,
    // Inventario totals
    val inventarioRegistros: Int = 0,
    val activoFijoRegistros: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val registroDao: RegistroDao,
    private val preferencesManager: PreferencesManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    val isOnline = networkMonitor.isOnline.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true,
    )

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val totalAF = registroDao.countAllActivoFijo()
            val totalInv = registroDao.countAllInventario()
            val pending = registroDao.countAllUnsynced()
            val afFound = registroDao.countActivoFijoFound()
            val afNotFound = registroDao.countActivoFijoNotFound()
            val afAdded = registroDao.countActivoFijoAdded()
            val afTransferred = registroDao.countActivoFijoTransferred()

            val empresaNombre = preferencesManager.empresaNombre.first()
            val sucursalNombre = preferencesManager.sucursalNombre.first()

            preferencesManager.lastSync.collect { lastSync ->
                _uiState.value = _uiState.value.copy(
                    userName = user?.nombres ?: "Usuario",
                    empresaNombre = empresaNombre,
                    sucursalNombre = sucursalNombre,
                    totalRegistros = totalAF + totalInv,
                    pendingSync = pending,
                    lastSync = lastSync,
                    afFound = afFound,
                    afNotFound = afNotFound,
                    afAdded = afAdded,
                    afTransferred = afTransferred,
                    inventarioRegistros = totalInv,
                    activoFijoRegistros = totalAF,
                )
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncMessage = null)
            val result = syncRepository.syncAll()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        syncMessage = "Sincronización completada",
                    )
                    loadDashboardData()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        syncMessage = "Error: ${it.message}",
                    )
                },
            )
        }
    }
}
