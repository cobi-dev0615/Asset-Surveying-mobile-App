package com.seretail.inventarios.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.ActivoFijoDao
import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.remote.ApiService
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.ui.components.PieSlice
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.StatusTransferred
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
    val userName: String? = null,
    val userRolId: Int? = null,
    val progressSlices: List<PieSlice> = emptyList(),
    val categoryBars: List<Pair<String, Int>> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val registroDao: RegistroDao,
    private val inventarioDao: InventarioDao,
    private val activoFijoDao: ActivoFijoDao,
    private val apiService: ApiService,
    private val syncRepository: SyncRepository,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        initialSync()
        observeNetwork()
        loadUser()
    }

    private fun initialSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                syncRepository.syncInventarioSessions()
                syncRepository.syncActivoFijoSessions()
            } catch (_: Exception) {}
            _uiState.value = _uiState.value.copy(isSyncing = false)
            loadStats()
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            // Always get pending sync count from local DB
            val pending = registroDao.countAllUnsynced()
            _uiState.value = _uiState.value.copy(pendingSyncCount = pending)

            // Try server stats first, fall back to local
            try {
                val response = apiService.getDashboardStats()
                if (response.isSuccessful) {
                    val stats = response.body()!!
                    val slices = listOf(
                        PieSlice(stats.found.toFloat(), StatusFound),
                        PieSlice(stats.notFound.toFloat(), StatusNotFound),
                        PieSlice(stats.added.toFloat(), StatusAdded),
                        PieSlice(stats.transferred.toFloat(), StatusTransferred),
                    ).filter { it.value > 0f }

                    val bars = stats.categories.map { it.name to it.count }

                    _uiState.value = _uiState.value.copy(
                        inventarioCount = stats.inventarioCount,
                        activoFijoCount = stats.activoFijoCount,
                        foundCount = stats.found,
                        notFoundCount = stats.notFound,
                        addedCount = stats.added,
                        transferredCount = stats.transferred,
                        progressSlices = slices,
                        categoryBars = bars,
                    )
                    return@launch
                }
            } catch (_: Exception) {}

            // Fallback: local Room data
            loadLocalStats()
        }
    }

    private suspend fun loadLocalStats() {
        val invCount = inventarioDao.count()
        val afCount = activoFijoDao.count()
        val found = registroDao.countActivoFijoFound()
        val notFound = registroDao.countActivoFijoNotFound()
        val added = registroDao.countActivoFijoAdded()
        val transferred = registroDao.countActivoFijoTransferred()

        val slices = listOf(
            PieSlice(found.toFloat(), StatusFound),
            PieSlice(notFound.toFloat(), StatusNotFound),
            PieSlice(added.toFloat(), StatusAdded),
            PieSlice(transferred.toFloat(), StatusTransferred),
        ).filter { it.value > 0f }

        val categoryCounts = registroDao.getActivoFijoCategoryCounts()
        val bars = categoryCounts.map { it.categoria to it.cnt }

        _uiState.value = _uiState.value.copy(
            inventarioCount = invCount,
            activoFijoCount = afCount,
            foundCount = found,
            notFoundCount = notFound,
            addedCount = added,
            transferredCount = transferred,
            progressSlices = slices,
            categoryBars = bars,
        )
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

    private fun loadUser() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.value = _uiState.value.copy(
                    userName = user?.nombres,
                    userRolId = user?.rolId,
                )
            }
        }
    }

    fun refresh() = loadStats()

    fun clearMessage() { _uiState.value = _uiState.value.copy(syncMessage = null) }
}
