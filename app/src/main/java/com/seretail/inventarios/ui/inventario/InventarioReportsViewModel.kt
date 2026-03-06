package com.seretail.inventarios.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReportType(val label: String) {
    BY_PRODUCT("Conteo agrupado por producto"),
    BY_PRODUCT_LOCATION("Conteo por producto y ubicación"),
    DIFFERENCES("Diferencias (teórico vs real)"),
    DETAILED("Conteo a detalle"),
    CROSS_COUNT("Conteo cruzado"),
}

enum class SortColumn(val label: String) {
    CODE("Código"),
    DESCRIPTION("Descripción"),
    QUANTITY("Cantidad"),
    REGISTROS("Registros"),
    LOCATION("Ubicación"),
    TEORICO("Teórico"),
    DIFERENCIA("Diferencia"),
}

enum class SortDirection { ASC, DESC }

data class GroupedReport(
    val codigoBarras: String,
    val descripcion: String?,
    val ubicacion: String?,
    val totalCantidad: Int,
    val registroCount: Int,
    val cantidadTeorica: Double? = null,
    val diferencia: Double? = null,
    val precioVenta: Double? = null,
    val importeReal: Double? = null,
    val importeTeorico: Double? = null,
    val importeDiferencia: Double? = null,
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
    val totalTeorico: Double = 0.0,
    val totalDiferencia: Double = 0.0,
    val totalImporteReal: Double = 0.0,
    val totalImporteTeorico: Double = 0.0,
    val showExportDialog: Boolean = false,
    val sortColumn: SortColumn = SortColumn.CODE,
    val sortDirection: SortDirection = SortDirection.ASC,
)

@HiltViewModel
class InventarioReportsViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val registroDao: RegistroDao,
    private val productoDao: ProductoDao,
    private val preferencesManager: PreferencesManager,
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
        _uiState.value = _uiState.value.copy(reportType = type, isLoading = true)
        viewModelScope.launch {
            generateReport()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun toggleExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = !_uiState.value.showExportDialog)
    }

    fun toggleSort(column: SortColumn) {
        val current = _uiState.value
        val newDirection = if (current.sortColumn == column) {
            if (current.sortDirection == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
        } else {
            SortDirection.ASC
        }
        _uiState.value = current.copy(
            sortColumn = column,
            sortDirection = newDirection,
            groupedData = applySorting(current.groupedData, column, newDirection),
        )
    }

    private fun applySorting(
        data: List<GroupedReport>,
        column: SortColumn,
        direction: SortDirection,
    ): List<GroupedReport> {
        val comparator: Comparator<GroupedReport> = when (column) {
            SortColumn.CODE -> compareBy { it.codigoBarras }
            SortColumn.DESCRIPTION -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.descripcion ?: "" }
            SortColumn.QUANTITY -> compareBy { it.totalCantidad }
            SortColumn.REGISTROS -> compareBy { it.registroCount }
            SortColumn.LOCATION -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.ubicacion ?: "" }
            SortColumn.TEORICO -> compareBy { it.cantidadTeorica ?: 0.0 }
            SortColumn.DIFERENCIA -> compareBy { it.diferencia ?: 0.0 }
        }
        return if (direction == SortDirection.ASC) data.sortedWith(comparator)
        else data.sortedWith(comparator.reversed())
    }

    private suspend fun generateReport() {
        val registros = _uiState.value.registros
        val totalQty = registros.sumOf { it.cantidad }
        val totalLocations = registros.mapNotNull { it.ubicacion }.distinct().size

        val grouped: List<GroupedReport>
        var totalTeorico = 0.0
        var totalDiferencia = 0.0
        var totalImporteReal = 0.0
        var totalImporteTeorico = 0.0

        when (_uiState.value.reportType) {
            ReportType.BY_PRODUCT -> {
                grouped = registros.groupBy { it.codigoBarras }.map { (code, items) ->
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
                grouped = registros.groupBy { "${it.codigoBarras}|${it.ubicacion ?: ""}" }.map { (_, items) ->
                    GroupedReport(
                        codigoBarras = items.first().codigoBarras,
                        descripcion = items.first().descripcion,
                        ubicacion = items.first().ubicacion,
                        totalCantidad = items.sumOf { it.cantidad },
                        registroCount = items.size,
                    )
                }
            }
            ReportType.DIFFERENCES -> {
                val empresaId = preferencesManager.empresaId.first() ?: 0L
                val productos = if (empresaId > 0L) {
                    productoDao.getAllByEmpresa(empresaId)
                } else {
                    emptyList()
                }

                val registrosByCode = registros.groupBy { it.codigoBarras }

                grouped = productos.mapNotNull { producto ->
                    val items = registrosByCode[producto.codigoBarras]
                    val cantidadReal = items?.sumOf { it.cantidad } ?: 0
                    val cantidadTeorica = producto.cantidadTeorica ?: 0.0
                    val precio = producto.precioVenta ?: 0.0
                    val diff = cantidadReal - cantidadTeorica
                    val importeReal = cantidadReal * precio
                    val importeTeorico = cantidadTeorica * precio

                    if (cantidadReal > 0 || cantidadTeorica > 0) {
                        totalTeorico += cantidadTeorica
                        totalDiferencia += diff
                        totalImporteReal += importeReal
                        totalImporteTeorico += importeTeorico

                        GroupedReport(
                            codigoBarras = producto.codigoBarras,
                            descripcion = producto.descripcion,
                            ubicacion = null,
                            totalCantidad = cantidadReal,
                            registroCount = items?.size ?: 0,
                            cantidadTeorica = cantidadTeorica,
                            diferencia = diff,
                            precioVenta = precio,
                            importeReal = importeReal,
                            importeTeorico = importeTeorico,
                            importeDiferencia = importeReal - importeTeorico,
                        )
                    } else {
                        null
                    }
                }.sortedBy { it.diferencia }
            }
            ReportType.DETAILED -> {
                grouped = registros.map {
                    GroupedReport(
                        codigoBarras = it.codigoBarras,
                        descripcion = it.descripcion,
                        ubicacion = it.ubicacion,
                        totalCantidad = it.cantidad,
                        registroCount = 1,
                    )
                }
            }
            ReportType.CROSS_COUNT -> {
                grouped = registros.groupBy { "${it.codigoBarras}|${it.ubicacion ?: ""}" }.map { (_, items) ->
                    GroupedReport(
                        codigoBarras = items.first().codigoBarras,
                        descripcion = items.first().descripcion,
                        ubicacion = items.first().ubicacion,
                        totalCantidad = items.sumOf { it.cantidad },
                        registroCount = items.size,
                    )
                }
            }
        }

        val state = _uiState.value
        _uiState.value = state.copy(
            groupedData = applySorting(grouped, state.sortColumn, state.sortDirection),
            totalQuantity = totalQty,
            totalRegistros = registros.size,
            totalLocations = totalLocations,
            totalTeorico = totalTeorico,
            totalDiferencia = totalDiferencia,
            totalImporteReal = totalImporteReal,
            totalImporteTeorico = totalImporteTeorico,
        )
    }
}
