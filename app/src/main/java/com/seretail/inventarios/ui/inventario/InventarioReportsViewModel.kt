package com.seretail.inventarios.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReportType(val label: String) {
    BY_PRODUCT("Conteo agrupado por producto"),
    BY_PRODUCT_LOCATION("Conteo por producto y ubicación"),
    DETAILED("Conteo a detalle"),
    SUMMARY("Resumen general"),
}

data class GroupedReport(
    val codigoBarras: String,
    val descripcion: String?,
    val ubicacion: String?,
    val totalCantidad: Int,
    val registroCount: Int,
)

data class InventarioReportsUiState(
    val sessions: List<InventarioEntity> = emptyList(),
    val selectedSessionId: Long? = null,
    val reportType: ReportType = ReportType.BY_PRODUCT,
    val registros: List<InventarioRegistroEntity> = emptyList(),
    val groupedData: List<GroupedReport> = emptyList(),
    val isLoading: Boolean = true,
    val totalQuantity: Int = 0,
    val totalRegistros: Int = 0,
    val totalLocations: Int = 0,
)

@HiltViewModel
class InventarioReportsViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val registroDao: RegistroDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioReportsUiState())
    val uiState: StateFlow<InventarioReportsUiState> = _uiState

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            val sessions = inventarioDao.getAll()
            _uiState.value = _uiState.value.copy(sessions = sessions, isLoading = false)
            if (sessions.isNotEmpty()) {
                selectSession(sessions.first().id)
            }
        }
    }

    fun selectSession(sessionId: Long) {
        _uiState.value = _uiState.value.copy(selectedSessionId = sessionId, isLoading = true)
        viewModelScope.launch {
            val registros = registroDao.getInventarioBySession(sessionId)
            _uiState.value = _uiState.value.copy(registros = registros, isLoading = false)
            generateReport()
        }
    }

    fun selectReportType(type: ReportType) {
        _uiState.value = _uiState.value.copy(reportType = type)
        generateReport()
    }

    private fun generateReport() {
        val registros = _uiState.value.registros
        val totalQty = registros.sumOf { it.cantidad }
        val totalLocations = registros.mapNotNull { it.ubicacion }.distinct().size

        val grouped = when (_uiState.value.reportType) {
            ReportType.BY_PRODUCT -> {
                registros.groupBy { it.codigoBarras }.map { (code, items) ->
                    GroupedReport(
                        codigoBarras = code,
                        descripcion = items.first().descripcion,
                        ubicacion = null,
                        totalCantidad = items.sumOf { it.cantidad },
                        registroCount = items.size,
                    )
                }
            }
            ReportType.BY_PRODUCT_LOCATION -> {
                registros.groupBy { "${it.codigoBarras}|${it.ubicacion ?: ""}" }.map { (_, items) ->
                    GroupedReport(
                        codigoBarras = items.first().codigoBarras,
                        descripcion = items.first().descripcion,
                        ubicacion = items.first().ubicacion,
                        totalCantidad = items.sumOf { it.cantidad },
                        registroCount = items.size,
                    )
                }
            }
            ReportType.DETAILED -> {
                registros.map {
                    GroupedReport(
                        codigoBarras = it.codigoBarras,
                        descripcion = it.descripcion,
                        ubicacion = it.ubicacion,
                        totalCantidad = it.cantidad,
                        registroCount = 1,
                    )
                }
            }
            ReportType.SUMMARY -> {
                registros.groupBy { it.codigoBarras }.map { (code, items) ->
                    GroupedReport(
                        codigoBarras = code,
                        descripcion = items.first().descripcion,
                        ubicacion = null,
                        totalCantidad = items.sumOf { it.cantidad },
                        registroCount = items.size,
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            groupedData = grouped,
            totalQuantity = totalQty,
            totalRegistros = registros.size,
            totalLocations = totalLocations,
        )
    }
}
