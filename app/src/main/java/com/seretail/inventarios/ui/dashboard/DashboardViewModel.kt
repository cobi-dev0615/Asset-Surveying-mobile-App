package com.seretail.inventarios.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val inventarioCount: Int = 0,
    val activoFijoCount: Int = 0,
    val foundCount: Int = 0,
    val notFoundCount: Int = 0,
    val addedCount: Int = 0,
    val transferredCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val registroDao: RegistroDao,
    private val syncRepository: SyncRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadStats()
        observeNetwork()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val invCount = registroDao.countAllInventario()
            val afCount = registroDao.countAllActivoFijo()
            val found = registroDao.countActivoFijoFound()
            val notFound = registroDao.countActivoFijoNotFound()
            val added = registroDao.countActivoFijoAdded()
            val transferred = registroDao.countActivoFijoTransferred()
            val pending = registroDao.countAllUnsynced()

            _uiState.value = _uiState.value.copy(
                inventarioCount = invCount,
                activoFijoCount = afCount,
                foundCount = found,
                notFoundCount = notFound,
                addedCount = added,
                transferredCount = transferred,
                pendingSyncCount = pending,
            )
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                _uiState.value = _uiState.value.copy(isOnline = online)
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                syncRepository.syncAll()
                loadStats()
                _uiState.value = _uiState.value.copy(isSyncing = false, syncMessage = "Sincronización completa")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSyncing = false, syncMessage = "Error: ${e.message}")
            }
        }
    }

    fun refresh() = loadStats()

    fun clearMessage() { _uiState.value = _uiState.value.copy(syncMessage = null) }
}
