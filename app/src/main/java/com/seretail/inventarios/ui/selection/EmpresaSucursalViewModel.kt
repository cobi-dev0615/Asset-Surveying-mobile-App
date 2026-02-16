package com.seretail.inventarios.ui.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.EmpresaDao
import com.seretail.inventarios.data.local.dao.SucursalDao
import com.seretail.inventarios.data.local.entity.EmpresaEntity
import com.seretail.inventarios.data.local.entity.SucursalEntity
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectionUiState(
    val empresas: List<EmpresaEntity> = emptyList(),
    val sucursales: List<SucursalEntity> = emptyList(),
    val selectedEmpresa: EmpresaEntity? = null,
    val selectedSucursal: SucursalEntity? = null,
    val step: SelectionStep = SelectionStep.EMPRESA,
    val isLoading: Boolean = true,
    val isComplete: Boolean = false,
)

enum class SelectionStep { EMPRESA, SUCURSAL }

@HiltViewModel
class EmpresaSucursalViewModel @Inject constructor(
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectionUiState())
    val uiState: StateFlow<SelectionUiState> = _uiState

    init {
        loadEmpresas()
    }

    private fun loadEmpresas() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val allEmpresas = empresaDao.getAll()

            // Filter by user's assigned empresas (Super Admin sees all)
            val empresaIds = user?.empresaIds?.let { json ->
                json.removeSurrounding("[", "]")
                    .split(",")
                    .mapNotNull { it.trim().toLongOrNull() }
            } ?: emptyList()

            val filtered = if (user?.rolId == 1 || empresaIds.isEmpty()) {
                allEmpresas // Super Admin or no restriction
            } else {
                allEmpresas.filter { it.id in empresaIds }
            }

            // Auto-select if only one empresa
            if (filtered.size == 1) {
                _uiState.value = _uiState.value.copy(
                    empresas = filtered,
                    selectedEmpresa = filtered.first(),
                    isLoading = false,
                )
                loadSucursales(filtered.first())
            } else {
                _uiState.value = _uiState.value.copy(
                    empresas = filtered,
                    isLoading = false,
                )
            }
        }
    }

    fun selectEmpresa(empresa: EmpresaEntity) {
        _uiState.value = _uiState.value.copy(selectedEmpresa = empresa)
        loadSucursales(empresa)
    }

    private fun loadSucursales(empresa: EmpresaEntity) {
        viewModelScope.launch {
            val sucursales = sucursalDao.getByEmpresa(empresa.id)
            _uiState.value = _uiState.value.copy(
                sucursales = sucursales,
                step = SelectionStep.SUCURSAL,
            )

            // Auto-select if only one sucursal
            if (sucursales.size == 1) {
                confirmSelection(sucursales.first())
            }
        }
    }

    fun selectSucursal(sucursal: SucursalEntity) {
        confirmSelection(sucursal)
    }

    fun goBackToEmpresa() {
        _uiState.value = _uiState.value.copy(
            step = SelectionStep.EMPRESA,
            selectedSucursal = null,
            sucursales = emptyList(),
        )
    }

    private fun confirmSelection(sucursal: SucursalEntity) {
        val empresa = _uiState.value.selectedEmpresa ?: return
        viewModelScope.launch {
            preferencesManager.saveEmpresa(empresa.id, empresa.nombre)
            preferencesManager.saveSucursal(sucursal.id, sucursal.nombre)
            _uiState.value = _uiState.value.copy(
                selectedSucursal = sucursal,
                isComplete = true,
            )
        }
    }
}
