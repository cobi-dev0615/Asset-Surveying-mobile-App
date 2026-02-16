package com.seretail.inventarios.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.EmpresaDao
import com.seretail.inventarios.data.local.dao.SucursalDao
import com.seretail.inventarios.data.local.entity.EmpresaEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.entity.SucursalEntity
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.data.repository.SyncRepository
import com.seretail.inventarios.export.CatalogImporter
import com.seretail.inventarios.printing.BluetoothPrinterManager
import com.seretail.inventarios.printing.PrinterState
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = "",
    val autoSync: Boolean = true,
    val syncWifiOnly: Boolean = false,
    val useCamera: Boolean = true,
    val empresas: List<EmpresaEntity> = emptyList(),
    val sucursales: List<SucursalEntity> = emptyList(),
    val selectedEmpresaId: Long? = null,
    val selectedSucursalId: Long? = null,
    val empresaNombre: String? = null,
    val sucursalNombre: String? = null,
    val printerMac: String? = null,
    val printerName: String? = null,
    val printerType: String? = null,
    val printerState: PrinterState = PrinterState.Disconnected,
    val lastSync: String? = null,
    val isSyncing: Boolean = false,
    val message: String? = null,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val productoDao: ProductoDao,
    private val printerManager: BluetoothPrinterManager,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadPreferences()
        loadEmpresas()
        observePrinter()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                serverUrl = preferencesManager.serverUrl.first(),
                autoSync = preferencesManager.autoSync.first(),
                syncWifiOnly = preferencesManager.syncWifiOnly.first(),
                useCamera = preferencesManager.useCamera.first(),
                selectedEmpresaId = preferencesManager.empresaId.first(),
                selectedSucursalId = preferencesManager.sucursalId.first(),
                empresaNombre = preferencesManager.empresaNombre.first(),
                sucursalNombre = preferencesManager.sucursalNombre.first(),
                printerMac = preferencesManager.printerMac.first(),
                printerName = preferencesManager.printerName.first(),
                printerType = preferencesManager.printerType.first(),
                lastSync = preferencesManager.lastSync.first(),
            )
            // Load sucursales for selected empresa
            _uiState.value.selectedEmpresaId?.let { loadSucursales(it) }
        }
    }

    private fun loadEmpresas() {
        viewModelScope.launch {
            empresaDao.observeAll().collect { empresas ->
                _uiState.value = _uiState.value.copy(empresas = empresas)
            }
        }
    }

    private fun loadSucursales(empresaId: Long) {
        viewModelScope.launch {
            sucursalDao.observeByEmpresa(empresaId).collect { sucursales ->
                _uiState.value = _uiState.value.copy(sucursales = sucursales)
            }
        }
    }

    private fun observePrinter() {
        viewModelScope.launch {
            printerManager.state.collectLatest { state ->
                _uiState.value = _uiState.value.copy(printerState = state)
            }
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

    fun selectEmpresa(empresa: EmpresaEntity) {
        _uiState.value = _uiState.value.copy(
            selectedEmpresaId = empresa.id,
            empresaNombre = empresa.nombre,
            selectedSucursalId = null,
            sucursalNombre = null,
        )
        viewModelScope.launch { preferencesManager.saveEmpresa(empresa.id, empresa.nombre) }
        loadSucursales(empresa.id)
    }

    fun selectSucursal(sucursal: SucursalEntity) {
        _uiState.value = _uiState.value.copy(
            selectedSucursalId = sucursal.id,
            sucursalNombre = sucursal.nombre,
        )
        viewModelScope.launch { preferencesManager.saveSucursal(sucursal.id, sucursal.nombre) }
    }

    fun savePrinter(mac: String, name: String, type: String) {
        _uiState.value = _uiState.value.copy(printerMac = mac, printerName = name, printerType = type)
        viewModelScope.launch { preferencesManager.savePrinter(mac, name, type) }
    }

    fun connectPrinter() {
        val mac = _uiState.value.printerMac ?: return
        printerManager.connect(mac)
    }

    fun disconnectPrinter() = printerManager.disconnect()

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                syncRepository.syncAll()
                _uiState.value = _uiState.value.copy(isSyncing = false, message = "Sincronización completa")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSyncing = false, message = "Error: ${e.message}")
            }
        }
    }

    fun importCatalog(uri: android.net.Uri) {
        val empresaId = _uiState.value.selectedEmpresaId
        if (empresaId == null) {
            _uiState.value = _uiState.value.copy(message = "Selecciona una empresa primero")
            return
        }
        viewModelScope.launch {
            val result = CatalogImporter.importFromUri(appContext, uri, empresaId)
            if (result.products.isNotEmpty()) {
                productoDao.insertAll(result.products)
                _uiState.value = _uiState.value.copy(
                    message = "Importados ${result.products.size} productos" +
                        if (result.errors.isNotEmpty()) " (${result.errors.size} errores)" else "",
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "No se importaron productos. ${result.errors.firstOrNull() ?: ""}",
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }
}
