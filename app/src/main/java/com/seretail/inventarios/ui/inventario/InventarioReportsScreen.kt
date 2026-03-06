package com.seretail.inventarios.ui.inventario

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.ExportDialog
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InventarioReportsScreen(
    onBackClick: () -> Unit,
    viewModel: InventarioReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSessionDropdown by remember { mutableStateOf(false) }
    var showReportTypeDropdown by remember { mutableStateOf(false) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")) }

    if (state.showExportDialog) {
        ExportDialog(
            onDismiss = { viewModel.toggleExportDialog() },
            onExportCsv = {
                scope.launch {
                    exportReportFile(context, state, "csv")
                }
            },
            onExportExcel = {
                scope.launch {
                    exportReportFile(context, state, "xlsx")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "Reportes",
                onBackClick = onBackClick,
            )
        },
        containerColor = DarkBackground,
        floatingActionButton = {
            if (state.groupedData.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.toggleExportDialog() },
                    containerColor = SERBlue,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Exportar")
                }
            }
        },
    ) { padding ->
        if (state.isLoading && state.sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SERBlue)
            }
            return@Scaffold
        }

        if (state.sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No hay sesiones de inventario",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Crea un inventario para ver reportes",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // Session selector
            val selectedSession = state.sessions.find { it.id == state.selectedSessionId }

            Box {
                OutlinedButton(
                    onClick = { showSessionDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = SERBlue,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selectedSession?.nombre ?: "Seleccionar sesión",
                        color = if (selectedSession != null) TextPrimary else TextMuted,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = showSessionDropdown,
                    onDismissRequest = { showSessionDropdown = false },
                ) {
                    state.sessions.forEach { session ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(session.nombre, color = TextPrimary)
                                    if (session.sucursalNombre != null) {
                                        Text(
                                            session.sucursalNombre,
                                            color = TextMuted,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.selectSession(session.id)
                                showSessionDropdown = false
                            },
                            leadingIcon = if (session.id == state.selectedSessionId) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SERBlue,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Report type selector
            Box {
                OutlinedButton(
                    onClick = { showReportTypeDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = SERBlue,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state.reportType.label,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = showReportTypeDropdown,
                    onDismissRequest = { showReportTypeDropdown = false },
                ) {
                    ReportType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label, color = TextPrimary) },
                            onClick = {
                                viewModel.selectReportType(type)
                                showReportTypeDropdown = false
                            },
                            leadingIcon = if (type == state.reportType) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SERBlue,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Summary stats card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    SummaryStatItem(
                        label = "TOTAL CONTEO",
                        value = "${state.totalQuantity}",
                    )
                    SummaryStatItem(
                        label = "REGISTROS",
                        value = "${state.totalRegistros}",
                    )
                    SummaryStatItem(
                        label = "UBICACIONES",
                        value = "${state.totalLocations}",
                    )
                }
            }

            // Difference stats card (only for DIFFERENCES report)
            if (state.reportType == ReportType.DIFFERENCES) {
                Spacer(Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        SummaryStatItem(
                            label = "TEÓRICO",
                            value = "%.0f".format(state.totalTeorico),
                        )
                        SummaryStatItem(
                            label = "REAL",
                            value = "${state.totalQuantity}",
                            valueColor = SERBlue,
                        )
                        SummaryStatItem(
                            label = "DIFERENCIA",
                            value = "%+.0f".format(state.totalDiferencia),
                            valueColor = when {
                                state.totalDiferencia < 0 -> StatusNotFound
                                state.totalDiferencia > 0 -> StatusAdded
                                else -> StatusFound
                            },
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        SummaryStatItem(
                            label = "IMP. TEÓRICO",
                            value = currencyFormat.format(state.totalImporteTeorico),
                        )
                        SummaryStatItem(
                            label = "IMP. REAL",
                            value = currencyFormat.format(state.totalImporteReal),
                            valueColor = SERBlue,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Results header
            Text(
                text = "Resultados (${state.groupedData.size})",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            // Sort controls
            if (state.groupedData.isNotEmpty()) {
                val sortColumns = when (state.reportType) {
                    ReportType.DIFFERENCES -> listOf(
                        SortColumn.CODE, SortColumn.DESCRIPTION,
                        SortColumn.QUANTITY, SortColumn.TEORICO, SortColumn.DIFERENCIA,
                    )
                    ReportType.BY_PRODUCT_LOCATION, ReportType.CROSS_COUNT -> listOf(
                        SortColumn.CODE, SortColumn.DESCRIPTION,
                        SortColumn.LOCATION, SortColumn.QUANTITY, SortColumn.REGISTROS,
                    )
                    ReportType.DETAILED -> listOf(
                        SortColumn.CODE, SortColumn.DESCRIPTION,
                        SortColumn.LOCATION, SortColumn.QUANTITY,
                    )
                    ReportType.BY_PRODUCT -> listOf(
                        SortColumn.CODE, SortColumn.DESCRIPTION,
                        SortColumn.QUANTITY, SortColumn.REGISTROS,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    sortColumns.forEach { column ->
                        val isActive = state.sortColumn == column
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.toggleSort(column) }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = column.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) SERBlue else TextMuted,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                            )
                            Icon(
                                imageVector = when {
                                    !isActive -> Icons.Default.SwapVert
                                    state.sortDirection == SortDirection.ASC -> Icons.Default.ArrowUpward
                                    else -> Icons.Default.ArrowDownward
                                },
                                contentDescription = null,
                                tint = if (isActive) SERBlue else TextMuted,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }
            }

            // Loading indicator for session switch
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SERBlue)
                }
            } else if (state.groupedData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sin registros en esta sesión",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else {
                // Grouped data list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(state.groupedData) { item ->
                        ReportItemCard(item = item, reportType = state.reportType)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    valueColor: Color = SERBlue,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun ReportItemCard(item: GroupedReport, reportType: ReportType) {
    val diffColor = when {
        reportType != ReportType.DIFFERENCES -> SERBlue
        (item.diferencia ?: 0.0) < 0 -> StatusNotFound
        (item.diferencia ?: 0.0) > 0 -> StatusAdded
        else -> StatusFound
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.codigoBarras,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                if (!item.descripcion.isNullOrBlank()) {
                    Text(
                        text = item.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                    )
                }
                if (!item.ubicacion.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.ubicacion,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            if (reportType == ReportType.DIFFERENCES) {
                // Teórico | Real | Diferencia columns
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.0f".format(item.cantidadTeorica ?: 0.0),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Teór.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${item.totalCantidad}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = SERBlue,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Real",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%+.0f".format(item.diferencia ?: 0.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = diffColor,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Dif.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp,
                    )
                }
            } else {
                // Standard: Quantity and count badges
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.totalCantidad}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SERBlue,
                    )
                    if (reportType != ReportType.DETAILED) {
                        Text(
                            text = "${item.registroCount} reg.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
            }
        }
    }
}

private suspend fun exportReportFile(
    context: Context,
    state: InventarioReportsUiState,
    format: String,
) {
    withContext(Dispatchers.IO) {
        try {
            val fileName = "reporte_${state.reportType.name.lowercase()}_${System.currentTimeMillis()}.$format"
            val file = File(context.cacheDir, fileName)

            val header = when (state.reportType) {
                ReportType.DIFFERENCES -> "Código,Descripción,Teórico,Real,Diferencia,Precio,Imp. Real,Imp. Teórico"
                ReportType.BY_PRODUCT_LOCATION, ReportType.CROSS_COUNT -> "Código,Descripción,Ubicación,Cantidad,Registros"
                ReportType.DETAILED -> "Código,Descripción,Ubicación,Cantidad"
                ReportType.BY_PRODUCT -> "Código,Descripción,Cantidad,Registros"
            }

            val rows = state.groupedData.joinToString("\n") { item ->
                val desc = (item.descripcion ?: "").replace(",", ";")
                val ubic = (item.ubicacion ?: "").replace(",", ";")
                when (state.reportType) {
                    ReportType.DIFFERENCES -> "${ item.codigoBarras },$desc,${item.cantidadTeorica ?: 0},${item.totalCantidad},${item.diferencia ?: 0},${item.precioVenta ?: 0},${item.importeReal ?: 0},${item.importeTeorico ?: 0}"
                    ReportType.BY_PRODUCT_LOCATION, ReportType.CROSS_COUNT -> "${item.codigoBarras},$desc,$ubic,${item.totalCantidad},${item.registroCount}"
                    ReportType.DETAILED -> "${item.codigoBarras},$desc,$ubic,${item.totalCantidad}"
                    ReportType.BY_PRODUCT -> "${item.codigoBarras},$desc,${item.totalCantidad},${item.registroCount}"
                }
            }

            file.writeText("$header\n$rows")

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file,
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (format == "csv") "text/csv" else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(intent, "Exportar reporte"))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
