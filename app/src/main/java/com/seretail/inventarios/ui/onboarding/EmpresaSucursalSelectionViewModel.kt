package com.seretail.inventarios.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.EmpresaDao
import com.seretail.inventarios.data.local.dao.SucursalDao
import com.seretail.inventarios.data.local.entity.EmpresaEntity
import com.seretail.inventarios.data.local.entity.SucursalEntity
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.util.PreferencesManager
import retrofit2.HttpException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectionUiState(
    val step: Int = 1, // 1=empresa, 2=sucursal, 3=confirm
    val empresas: List<EmpresaEntity> = emptyList(),
    val sucursales: List<SucursalEntity> = emptyList(),
    val selectedEmpresa: EmpresaEntity? = null,
    val selectedSucursal: SucursalEntity? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val selectionComplete: Boolean = false,
    val error: String? = null,
    val authFailed: Boolean = false,
)

@HiltViewModel
class EmpresaSucursalSelectionViewModel @Inject constructor(
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val preferencesManager: PreferencesManager,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectionUiState())
    val uiState: StateFlow<SelectionUiState> = _uiState

    init {
        checkExistingSelection()
    }

    private fun checkExistingSelection() {
        viewModelScope.launch {
            val empresaId = preferencesManager.empresaId.first()
            val sucursalId = preferencesManager.sucursalId.first()
            if (empresaId != null && sucursalId != null) {
                // Already configured — skip straight to dashboard
                _uiState.value = _uiState.value.copy(isLoading = false, selectionComplete = true)
            } else {
                loadEmpresas()
            }
        }
    }

    private fun loadEmpresas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val empresas = empresaDao.getAll()
            if (empresas.isEmpty()) {
                // First login — need to sync catalogs first
                _uiState.value = _uiState.value.copy(isSyncing = true)
                try {
                    syncRepository.syncEmpresas()
                    val synced = empresaDao.getAll()
                    _uiState.value = _uiState.value.copy(
                        empresas = synced, isLoading = false, isSyncing = false,
                    )
                } catch (e: Exception) {
                    val isAuthError = e is HttpException && e.code() == 401
                    if (isAuthError) {
                        authRepository.logout()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, isSyncing = false, authFailed = true,
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, isSyncing = false,
                            error = "Error al cargar empresas: ${e.message}",
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(empresas = empresas, isLoading = false)
            }
        }
    }

    fun onSearchChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredEmpresas(): List<EmpresaEntity> {
        val q = _uiState.value.searchQuery.lowercase()
        if (q.isBlank()) return _uiState.value.empresas
        return _uiState.value.empresas.filter {
            it.nombre.lowercase().contains(q) || it.codigo.lowercase().contains(q)
        }
    }

    fun getFilteredSucursales(): List<SucursalEntity> {
        val q = _uiState.value.searchQuery.lowercase()
        if (q.isBlank()) return _uiState.value.sucursales
        return _uiState.value.sucursales.filter {
            it.nombre.lowercase().contains(q) || (it.codigo?.lowercase()?.contains(q) == true)
        }
    }

    fun selectEmpresa(empresa: EmpresaEntity) {
        _uiState.value = _uiState.value.copy(
            selectedEmpresa = empresa, step = 2, searchQuery = "", isLoading = true,
        )
        viewModelScope.launch {
            // Sync sucursales for this empresa if needed
            val sucursales = sucursalDao.getByEmpresa(empresa.id)
            if (sucursales.isEmpty()) {
                try {
                    syncRepository.syncSucursales(empresa.id)
                    val synced = sucursalDao.getByEmpresa(empresa.id)
                    _uiState.value = _uiState.value.copy(sucursales = synced, isLoading = false)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar sucursales: ${e.message}",
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(sucursales = sucursales, isLoading = false)
            }
        }
    }

    fun selectSucursal(sucursal: SucursalEntity) {
        _uiState.value = _uiState.value.copy(
            selectedSucursal = sucursal, showConfirmDialog = true,
        )
    }

    fun confirmSelection() {
        val emp = _uiState.value.selectedEmpresa ?: return
        val suc = _uiState.value.selectedSucursal ?: return
        viewModelScope.launch {
            preferencesManager.saveEmpresa(emp.id, emp.nombre)
            preferencesManager.saveSucursal(suc.id, suc.nombre)
            _uiState.value = _uiState.value.copy(
                showConfirmDialog = false, selectionComplete = true,
            )
        }
    }

    fun dismissConfirm() {
        _uiState.value = _uiState.value.copy(showConfirmDialog = false, selectedSucursal = null)
    }

    fun goBack() {
        if (_uiState.value.step == 2) {
            _uiState.value = _uiState.value.copy(
                step = 1, searchQuery = "", selectedEmpresa = null, sucursales = emptyList(),
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                syncRepository.syncEmpresas()
                val empresas = empresaDao.getAll()
                _uiState.value = _uiState.value.copy(empresas = empresas, isSyncing = false)
                // Also refresh sucursales if on step 2
                _uiState.value.selectedEmpresa?.let { emp ->
                    syncRepository.syncSucursales(emp.id)
                    val sucursales = sucursalDao.getByEmpresa(emp.id)
                    _uiState.value = _uiState.value.copy(sucursales = sucursales)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false, error = "Error: ${e.message}",
                )
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
