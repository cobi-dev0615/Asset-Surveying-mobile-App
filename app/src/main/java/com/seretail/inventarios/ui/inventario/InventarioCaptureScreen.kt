package com.seretail.inventarios.ui.inventario

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.export.CsvExporter
import com.seretail.inventarios.export.ExcelExporter
import com.seretail.inventarios.ui.components.SERTextField
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.components.ScanResultCard
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun InventarioCaptureScreen(
    sessionId: Long,
    onBackClick: () -> Unit,
    onScanBarcode: () -> Unit,
    viewModel: InventarioCaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showForm by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = state.session?.nombre ?: "Captura",
                onBackClick = onBackClick,
                actions = {
                    if (state.registros.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Exportar", tint = TextMuted)
                        }
                    }
                    Text(
                        text = "${state.capturedCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SERBlue,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        bottomBar = {
            // Bottom stats bar
            if (state.registros.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatItem(label = "CONTEO", value = "${state.totalQuantity}")
                    StatItem(label = "REGISTROS", value = "${state.registroCount}")
                    if (state.showFactor) {
                        StatItem(label = "FACTOR", value = "${state.totalFactor}")
                    }
                }
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SERBlue)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Form section
            if (showForm) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Forced code warning
                    if (state.isForcedCode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Error.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Código forzado (no en catálogo)", color = Error, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Barcode row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        SERTextField(
                            value = state.barcode,
                            onValueChange = viewModel::onBarcodeChanged,
                            label = "Código de barras",
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onScanBarcode) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Escanear", tint = SERBlue)
                        }
                    }

                    SERTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        label = "Descripción",
                        readOnly = true,
                    )

                    // Quantity row with conteo toggle
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SERTextField(
                            value = state.quantity,
                            onValueChange = viewModel::onQuantityChanged,
                            label = if (state.conteoUnidad) "Cantidad (unidad)" else "Cantidad (caja)",
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = state.conteoUnidad,
                            onClick = viewModel::toggleConteoUnidad,
                            label = { Text(if (state.conteoUnidad) "Unidad" else "Caja", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SERBlue.copy(alpha = 0.2f),
                                selectedLabelColor = SERBlue,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextMuted,
                            ),
                        )
                    }

                    // Factor + Serial row (conditional)
                    if (state.showFactor || state.showSerial) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.showFactor) {
                                SERTextField(
                                    value = state.factor,
                                    onValueChange = viewModel::onFactorChanged,
                                    label = "Factor",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (state.showSerial) {
                                SERTextField(
                                    value = state.serialNumber,
                                    onValueChange = viewModel::onSerialNumberChanged,
                                    label = "No. Serie",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SERTextField(
                            value = state.location,
                            onValueChange = viewModel::onLocationChanged,
                            label = "Ubicación",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Lote row with autocomplete dropdown
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            SERTextField(
                                value = state.lot,
                                onValueChange = viewModel::onLotChanged,
                                label = "Lote",
                            )
                            DropdownMenu(
                                expanded = state.showLoteSuggestions,
                                onDismissRequest = viewModel::dismissLoteSuggestions,
                            ) {
                                state.loteSuggestions.forEach { lote ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(lote.lote, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                                if (lote.caducidad != null) {
                                                    Text("Cad: ${lote.caducidad}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        },
                                        onClick = { viewModel.onLotSelected(lote) },
                                    )
                                }
                            }
                        }
                        SERTextField(
                            value = state.expiry,
                            onValueChange = viewModel::onExpiryChanged,
                            label = "Caducidad",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Button(
                        onClick = viewModel::saveRegistro,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar Registro", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Toggle form / list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Registros (${state.registros.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                )
                TextButton(onClick = { showForm = !showForm }) {
                    Text(
                        if (showForm) "Ocultar formulario" else "Mostrar formulario",
                        color = SERBlue,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Registros list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(state.registros, key = { it.id }) { registro ->
                    ScanResultCard(
                        barcode = registro.codigoBarras,
                        description = registro.descripcion,
                        statusId = 1,
                        onDelete = { viewModel.deleteRegistro(registro.id) },
                    )
                }
            }
        }
    }

    // Export dialog
    if (showExportDialog) {
        val sessionName = state.session?.nombre ?: "inventario"
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Registros", color = TextPrimary) },
            text = { Text("${state.registros.size} registros. Selecciona el formato:", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    val uri = ExcelExporter.exportInventario(context, sessionName, state.registros)
                    CsvExporter.shareFile(context, uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                }) { Text("Excel (.xlsx)", color = SERBlue) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    val uri = CsvExporter.exportInventario(context, sessionName, state.registros)
                    CsvExporter.shareFile(context, uri)
                }) { Text("CSV", color = SERBlue) }
            },
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SERBlue,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp,
        )
    }
}
