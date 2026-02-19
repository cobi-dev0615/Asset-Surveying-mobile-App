package com.seretail.inventarios.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.printing.BluetoothPrinterManager
import com.seretail.inventarios.printing.PrinterState
import com.seretail.inventarios.ui.components.SERTextField
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.printing.PrinterSelectionDialog
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    printerManager: BluetoothPrinterManager,
    onProductCatalogClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEmpresaDropdown by remember { mutableStateOf(false) }
    var showSucursalDropdown by remember { mutableStateOf(false) }
    var showPrinterDialog by remember { mutableStateOf(false) }

    val catalogLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCatalog(it) }
    }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (showPrinterDialog) {
        PrinterSelectionDialog(
            printerManager = printerManager,
            currentMac = state.printerMac,
            onPrinterSelected = { mac, name ->
                viewModel.savePrinter(mac, name, "escpos")
                viewModel.connectPrinter()
            },
            onDismiss = { showPrinterDialog = false },
        )
    }

    Scaffold(
        topBar = { SERTopBar(title = "Ajustes") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Server URL
            SectionHeader("Conexión")
            SERTextField(
                value = state.serverUrl,
                onValueChange = viewModel::onServerUrlChanged,
                label = "URL del Servidor",
            )

            // Empresa / Sucursal
            SectionHeader("Empresa y Sucursal")

            // Empresa selector
            androidx.compose.foundation.layout.Box {
                OutlinedButton(
                    onClick = { showEmpresaDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = SERBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state.empresaNombre ?: "Seleccionar empresa",
                        color = if (state.empresaNombre != null) TextPrimary else TextMuted,
                    )
                }
                DropdownMenu(
                    expanded = showEmpresaDropdown,
                    onDismissRequest = { showEmpresaDropdown = false },
                ) {
                    state.empresas.forEach { empresa ->
                        DropdownMenuItem(
                            text = { Text(empresa.nombre, color = TextPrimary) },
                            onClick = {
                                viewModel.selectEmpresa(empresa)
                                showEmpresaDropdown = false
                            },
                            leadingIcon = if (empresa.id == state.selectedEmpresaId) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = SERBlue, modifier = Modifier.size(16.dp)) }
                            } else null,
                        )
                    }
                }
            }

            // Sucursal selector
            androidx.compose.foundation.layout.Box {
                OutlinedButton(
                    onClick = { showSucursalDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = state.selectedEmpresaId != null,
                ) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = SERBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state.sucursalNombre ?: "Seleccionar sucursal",
                        color = if (state.sucursalNombre != null) TextPrimary else TextMuted,
                    )
                }
                DropdownMenu(
                    expanded = showSucursalDropdown,
                    onDismissRequest = { showSucursalDropdown = false },
                ) {
                    state.sucursales.forEach { sucursal ->
                        DropdownMenuItem(
                            text = { Text(sucursal.nombre, color = TextPrimary) },
                            onClick = {
                                viewModel.selectSucursal(sucursal)
                                showSucursalDropdown = false
                            },
                            leadingIcon = if (sucursal.id == state.selectedSucursalId) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = SERBlue, modifier = Modifier.size(16.dp)) }
                            } else null,
                        )
                    }
                }
            }

            // Sync settings
            SectionHeader("Sincronización")

            SettingsSwitch(
                label = "Sincronización automática",
                checked = state.autoSync,
                onCheckedChange = viewModel::onAutoSyncChanged,
            )
            SettingsSwitch(
                label = "Solo por WiFi",
                checked = state.syncWifiOnly,
                onCheckedChange = viewModel::onSyncWifiOnlyChanged,
            )
            SettingsSwitch(
                label = "Usar cámara para escanear",
                checked = state.useCamera,
                onCheckedChange = viewModel::onUseCameraChanged,
            )

            if (state.lastSync != null) {
                Text(
                    text = "Última sincronización: ${state.lastSync}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }

            Button(
                onClick = viewModel::syncNow,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (state.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sincronizar Ahora")
            }

            // Printer
            SectionHeader("Impresora Bluetooth")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = SERBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.printerName ?: "Ninguna",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )
                        Text(
                            text = when (state.printerState) {
                                is PrinterState.Connected -> "Conectada"
                                is PrinterState.Connecting -> "Conectando..."
                                is PrinterState.Printing -> "Imprimiendo..."
                                is PrinterState.Error -> "Error"
                                else -> state.printerMac ?: "No configurada"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (state.printerState) {
                                is PrinterState.Connected -> StatusFound
                                is PrinterState.Error -> Error
                                else -> TextMuted
                            },
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showPrinterDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Configurar Impresora", color = SERBlue)
            }

            // Import catalog
            SectionHeader("Catálogo de Productos")

            OutlinedButton(
                onClick = onProductCatalogClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = SERBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ver Catálogo de Productos", color = SERBlue)
            }

            OutlinedButton(
                onClick = { catalogLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = SERBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Importar Catálogo (CSV/Excel)", color = SERBlue)
            }

            // Capture Options
            SectionHeader("Opciones de Captura")

            SettingsSwitch(
                label = "Validar catálogo",
                checked = state.validateCatalog,
                onCheckedChange = viewModel::onValidateCatalogChanged,
            )
            SettingsSwitch(
                label = "Permitir códigos forzados",
                checked = state.allowForcedCodes,
                onCheckedChange = viewModel::onAllowForcedCodesChanged,
            )
            SettingsSwitch(
                label = "Capturar factor",
                checked = state.captureFactor,
                onCheckedChange = viewModel::onCaptureFactorChanged,
            )
            SettingsSwitch(
                label = "Capturar lotes",
                checked = state.captureLotes,
                onCheckedChange = viewModel::onCaptureLotesChanged,
            )
            SettingsSwitch(
                label = "Capturar número de serie",
                checked = state.captureSerial,
                onCheckedChange = viewModel::onCaptureSerialChanged,
            )
            SettingsSwitch(
                label = "Permitir negativos",
                checked = state.captureNegatives,
                onCheckedChange = viewModel::onCaptureNegativesChanged,
            )
            SettingsSwitch(
                label = "Capturar ceros",
                checked = state.captureZeros,
                onCheckedChange = viewModel::onCaptureZerosChanged,
            )
            SettingsSwitch(
                label = "Capturar GPS",
                checked = state.captureGps,
                onCheckedChange = viewModel::onCaptureGpsChanged,
            )
            SettingsSwitch(
                label = "Conteo por unidad",
                checked = state.conteoUnidad,
                onCheckedChange = viewModel::onConteoUnidadChanged,
            )

            // Logout
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Error),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
            }

            // About
            OutlinedButton(
                onClick = onAboutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Acerca de", color = TextMuted)
            }

            // App version
            Text(
                text = "v1.0.0 — SER Inventarios",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SERBlue,
                checkedTrackColor = SERBlue.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceVariant,
            ),
        )
    }
}
