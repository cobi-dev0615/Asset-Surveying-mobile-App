package com.seretail.inventarios.ui.inventario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.components.ScanResultCard
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

@Composable
fun InventarioCaptureScreen(
    sessionId: Long,
    onBackClick: () -> Unit,
    onScanClick: () -> Unit,
    viewModel: InventarioCaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }
    LaunchedEffect(uiState.message) { uiState.message?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        topBar = {
            SERTopBar(
                title = uiState.session?.nombre ?: "Captura",
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Menu row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Conteo 1a1 toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = uiState.options.conteo1a1,
                            onCheckedChange = viewModel::toggleConteo1a1,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = SERBlue,
                                checkedThumbColor = TextPrimary,
                                uncheckedTrackColor = DarkBorder,
                            ),
                        )
                        Text(
                            text = "1 a 1",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.options.conteo1a1) SERBlue else TextSecondary,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    // Options menu
                    Row {
                        IconButton(onClick = { viewModel.toggleOptionsMenu() }) {
                            Text(
                                "Opciones",
                                style = MaterialTheme.typography.labelMedium,
                                color = SERBlue,
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menú", tint = TextSecondary)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Eliminar ubicación") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteLocationRecords()
                                },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = Error) },
                            )
                        }
                    }
                }
            }

            // Options panel (expandable)
            item {
                AnimatedVisibility(visible = uiState.showOptionsMenu) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Opciones de Captura", style = MaterialTheme.typography.labelLarge, color = SERBlue)
                        OptionSwitch("Lotes", uiState.options.usarLote, viewModel::toggleLote)
                        OptionSwitch("Caducidad", uiState.options.usarCaducidad, viewModel::toggleCaducidad)
                        OptionSwitch("Factor (contenido caja)", uiState.options.usarFactor, viewModel::toggleFactor)
                        OptionSwitch("Número de Serie", uiState.options.usarSerie, viewModel::toggleSerie)
                        OptionSwitch("Validar Catálogo", uiState.options.validarCatalogo, viewModel::toggleValidarCatalogo)
                        OptionSwitch("Forzados (sin catálogo)", uiState.options.forzados, viewModel::toggleForzados)
                    }
                }
            }

            // Barcode input + scan button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.barcode,
                        onValueChange = viewModel::onBarcodeChanged,
                        label = { Text("Código de Barras") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = uiState.barcodeNotInCatalog,
                        colors = fieldColors(),
                    )
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Default.CameraAlt, "Escanear", tint = SERBlue)
                    }
                }
            }

            // Description (read-only after scan, shows product info)
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors(),
                )
            }

            // Ubicación + Cantidad
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.location,
                        onValueChange = viewModel::onLocationChanged,
                        label = { Text("Ubicación / Marbete") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = viewModel::onQuantityChanged,
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                }
            }

            // Lote dropdown (conditional)
            if (uiState.options.usarLote) {
                item {
                    if (uiState.availableLotes.isNotEmpty()) {
                        Column {
                            Text("Lote", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                uiState.availableLotes.forEach { loteItem ->
                                    val selected = uiState.lote == loteItem.lote
                                    Text(
                                        text = loteItem.lote,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) SERBlue else TextSecondary,
                                        modifier = Modifier
                                            .background(
                                                if (selected) SERBlue.copy(alpha = 0.15f) else DarkSurface,
                                                RoundedCornerShape(6.dp),
                                            )
                                            .clickable { viewModel.onLoteSelected(loteItem.lote) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = uiState.lote,
                            onValueChange = { viewModel.onLoteSelected(it) },
                            label = { Text("Lote") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = fieldColors(),
                        )
                    }
                }
            }

            // Caducidad (conditional)
            if (uiState.options.usarCaducidad) {
                item {
                    OutlinedTextField(
                        value = uiState.caducidad,
                        onValueChange = viewModel::onCaducidadChanged,
                        label = { Text("Caducidad (AAAA-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                }
            }

            // Factor (conditional)
            if (uiState.options.usarFactor) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = uiState.factor,
                            onValueChange = viewModel::onFactorChanged,
                            label = { Text("Factor (pzas/caja)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = fieldColors(),
                        )
                        if (uiState.factorResult.isNotEmpty()) {
                            Text(
                                text = uiState.factorResult,
                                style = MaterialTheme.typography.labelLarge,
                                color = StatusFound,
                            )
                        }
                    }
                }
            }

            // Numero de serie (conditional)
            if (uiState.options.usarSerie) {
                item {
                    OutlinedTextField(
                        value = uiState.numeroSerie,
                        onValueChange = viewModel::onNumeroSerieChanged,
                        label = { Text("Número de Serie") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                }
            }

            // Save button
            item {
                Button(
                    onClick = viewModel::saveRegistro,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !uiState.options.conteo1a1, // disabled in 1a1 mode (auto-save)
                ) {
                    Text(
                        if (uiState.options.conteo1a1) "Modo 1 a 1 (auto)" else "Guardar",
                        color = TextPrimary,
                    )
                }
            }

            // Counter
            item {
                Text(
                    text = "${uiState.registros.size} artículos capturados",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            // Registros list
            items(uiState.registros) { reg ->
                val subtitle = buildString {
                    append("Cant: ${reg.cantidad}")
                    reg.ubicacion?.let { append(" — $it") }
                    reg.lote?.let { append(" | L:$it") }
                    reg.caducidad?.let { append(" | C:$it") }
                }
                ScanResultCard(
                    code = reg.codigoBarras,
                    description = reg.descripcion ?: "Sin descripción",
                    subtitle = subtitle,
                    statusText = if (reg.sincronizado) "Sync" else "Local",
                    statusColor = if (reg.sincronizado) SERBlue else TextSecondary,
                    onDelete = { viewModel.deleteRegistro(reg.id) },
                )
            }
        }
    }
}

@Composable
private fun OptionSwitch(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(
                checkedTrackColor = SERBlue,
                checkedThumbColor = TextPrimary,
                uncheckedTrackColor = DarkBorder,
            ),
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SERBlue,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = SERBlue,
    unfocusedLabelColor = TextSecondary,
    cursorColor = SERBlue,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
)
