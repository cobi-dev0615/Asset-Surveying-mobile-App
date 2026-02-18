package com.seretail.inventarios.ui.activofijo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.export.CsvExporter
import com.seretail.inventarios.export.ExcelExporter
import com.seretail.inventarios.printing.BluetoothPrinterManager
import com.seretail.inventarios.printing.PrintTemplates
import com.seretail.inventarios.ui.components.PieChart
import com.seretail.inventarios.ui.components.PieSlice
import com.seretail.inventarios.ui.components.PhotoSlotRow
import com.seretail.inventarios.ui.components.SERTextField
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.components.ScanResultCard
import com.seretail.inventarios.ui.components.StatusChipRow
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.StatusTransferred
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import java.io.File

@Composable
fun ActivoFijoCaptureScreen(
    sessionId: Long,
    onBackClick: () -> Unit,
    onScanBarcode: () -> Unit,
    onCatalogClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    printerManager: BluetoothPrinterManager? = null,
    viewModel: ActivoFijoCaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showForm by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Photo capture
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            viewModel.onPhotoCaptured(photoUri!!)
        }
    }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Launch camera when activePhotoSlot changes
    LaunchedEffect(state.activePhotoSlot) {
        if (state.activePhotoSlot > 0) {
            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile,
            )
            cameraLauncher.launch(photoUri!!)
        }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = state.session?.nombre ?: "Captura",
                onBackClick = onBackClick,
                actions = {
                    if (onSearchClick != null) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Consulta", tint = TextMuted)
                        }
                    }
                    if (onCatalogClick != null) {
                        IconButton(onClick = onCatalogClick) {
                            Icon(Icons.Default.Inventory2, contentDescription = "Catálogo", tint = TextMuted)
                        }
                    }
                    if (state.registros.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Exportar", tint = TextMuted)
                        }
                    }
                    if (printerManager != null) {
                        IconButton(onClick = {
                            val lastRegistro = state.registros.lastOrNull()
                            if (lastRegistro != null) {
                                val data = PrintTemplates.buildAssetLabel(lastRegistro)
                                scope.launch {
                                    val ok = printerManager.print(data)
                                    if (!ok) snackbarHostState.showSnackbar("Impresora no conectada")
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("No hay registros para imprimir") }
                            }
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "Imprimir etiqueta", tint = TextMuted)
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
            // Capture form
            if (showForm) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
                ) {
                    // Edit mode indicator
                    if (state.isEditMode) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SERBlue.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = SERBlue, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Editando registro", color = SERBlue, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = viewModel::cancelEdit, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = "Cancelar", tint = Error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Barcode
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    }

                    // Description
                    item {
                        SERTextField(
                            value = state.description,
                            onValueChange = viewModel::onDescriptionChanged,
                            label = "Descripción",
                        )
                    }

                    // Category + Brand
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SERTextField(
                                value = state.category,
                                onValueChange = viewModel::onCategoryChanged,
                                label = "Categoría",
                                modifier = Modifier.weight(1f),
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                SERTextField(
                                    value = state.brand,
                                    onValueChange = viewModel::onBrandChanged,
                                    label = "Marca",
                                )
                                DropdownMenu(
                                    expanded = state.showBrandSuggestions,
                                    onDismissRequest = viewModel::dismissBrandSuggestions,
                                ) {
                                    state.brandSuggestions.forEach { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion, color = TextPrimary) },
                                            onClick = { viewModel.selectBrandSuggestion(suggestion) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Model + Color
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SERTextField(
                                value = state.model,
                                onValueChange = viewModel::onModelChanged,
                                label = "Modelo",
                                modifier = Modifier.weight(1f),
                            )
                            SERTextField(
                                value = state.color,
                                onValueChange = viewModel::onColorChanged,
                                label = "Color",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Serie + Location
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SERTextField(
                                value = state.serie,
                                onValueChange = viewModel::onSerieChanged,
                                label = "No. Serie",
                                modifier = Modifier.weight(1f),
                            )
                            SERTextField(
                                value = state.location,
                                onValueChange = viewModel::onLocationChanged,
                                label = "Ubicación",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Area with autocomplete
                    item {
                        Box {
                            SERTextField(
                                value = state.area,
                                onValueChange = viewModel::onAreaChanged,
                                label = "Área (se mantiene entre capturas)",
                            )
                            DropdownMenu(
                                expanded = state.showAreaSuggestions,
                                onDismissRequest = viewModel::dismissAreaSuggestions,
                            ) {
                                state.areaSuggestions.forEach { suggestion ->
                                    DropdownMenuItem(
                                        text = { Text(suggestion, color = TextPrimary) },
                                        onClick = { viewModel.selectAreaSuggestion(suggestion) },
                                    )
                                }
                            }
                        }
                    }

                    // Tag Nuevo + Serie Revisado
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SERTextField(
                                value = state.tagNuevo,
                                onValueChange = viewModel::onTagNuevoChanged,
                                label = "Tag Nuevo",
                                modifier = Modifier.weight(1f),
                            )
                            SERTextField(
                                value = state.serieRevisado,
                                onValueChange = viewModel::onSerieRevisadoChanged,
                                label = "Serie Revisado",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Comentarios
                    item {
                        SERTextField(
                            value = state.comentarios,
                            onValueChange = viewModel::onComentariosChanged,
                            label = "Comentarios",
                        )
                    }

                    // Status chips
                    item {
                        Text("Estado", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(4.dp))
                        StatusChipRow(
                            selectedId = state.selectedStatus,
                            onStatusSelected = viewModel::onStatusChanged,
                        )
                    }

                    // Photos
                    item {
                        Text("Fotos", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(4.dp))
                        PhotoSlotRow(
                            photo1 = state.photo1,
                            photo2 = state.photo2,
                            photo3 = state.photo3,
                            onCapture = viewModel::startPhotoCapture,
                            onRemove = viewModel::removePhoto,
                        )
                    }

                    // Save button
                    item {
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = viewModel::saveRegistro,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (state.isEditMode) "Actualizar Activo" else "Guardar Activo",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            } else {
                // Registros list
                Column(modifier = Modifier.weight(1f)) {
                    // Session stats dashboard
                    if (state.registros.isNotEmpty()) {
                        SessionStatsDashboard(
                            total = state.capturedCount,
                            found = state.foundCount,
                            notFound = state.notFoundCount,
                            added = state.addedCount,
                            transferred = state.transferredCount,
                        )
                    }

                    // Category filter
                    if (state.categories.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            item {
                                FilterChip(
                                    selected = state.selectedCategoryFilter == null,
                                    onClick = { viewModel.onCategoryFilterChanged(null) },
                                    label = { Text("Todos") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SERBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = SERBlue,
                                        labelColor = TextMuted,
                                    ),
                                )
                            }
                            items(state.categories) { cat ->
                                FilterChip(
                                    selected = state.selectedCategoryFilter == cat,
                                    onClick = { viewModel.onCategoryFilterChanged(cat) },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SERBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = SERBlue,
                                        labelColor = TextMuted,
                                    ),
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                    ) {
                        val filteredRegistros = viewModel.getFilteredRegistros()
                        items(filteredRegistros, key = { it.id }) { registro ->
                            ScanResultCard(
                                barcode = registro.codigoBarras,
                                description = registro.descripcion,
                                statusId = registro.statusId,
                                onEdit = { viewModel.enterEditMode(registro); showForm = true },
                                onDelete = { viewModel.deleteRegistro(registro.id) },
                            )
                        }
                    }
                }
            }

            // Bottom toggle bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Registros: ${state.registros.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                TextButton(onClick = { showForm = !showForm }) {
                    Text(
                        if (showForm) "Ver registros" else "Capturar",
                        color = SERBlue,
                    )
                }
            }
        }
    }

    // Export dialog
    if (showExportDialog) {
        val sessionName = state.session?.nombre ?: "activo_fijo"
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Registros", color = TextPrimary) },
            text = { Text("${state.registros.size} registros. Selecciona el formato:", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    val uri = ExcelExporter.exportActivoFijo(context, sessionName, state.registros)
                    CsvExporter.shareFile(context, uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                }) { Text("Excel (.xlsx)", color = SERBlue) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    val uri = CsvExporter.exportActivoFijo(context, sessionName, state.registros)
                    CsvExporter.shareFile(context, uri)
                }) { Text("CSV", color = SERBlue) }
            },
        )
    }

    // Transfer confirmation dialog
    if (state.showTransferDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTransfer() },
            title = { Text("Traspaso Detectado", color = TextPrimary) },
            text = {
                Text(
                    "Este activo pertenece a otra sucursal. Se registrará como traspasado a la sucursal actual.",
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmTransfer() }) {
                    Text("Confirmar Traspaso", color = SERBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissTransfer() }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
        )
    }
}

@Composable
private fun SessionStatsDashboard(
    total: Int,
    found: Int,
    notFound: Int,
    added: Int,
    transferred: Int,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pie chart
            PieChart(
                slices = listOf(
                    PieSlice(found.toFloat(), StatusFound),
                    PieSlice(notFound.toFloat(), StatusNotFound),
                    PieSlice(added.toFloat(), StatusAdded),
                    PieSlice(transferred.toFloat(), StatusTransferred),
                ),
                size = 72.dp,
                strokeWidth = 10.dp,
            )
            Spacer(Modifier.width(16.dp))
            // Stats grid
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBadge("Total", "$total", SERBlue, Modifier.weight(1f))
                    StatBadge("Encontrados", "$found", StatusFound, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBadge("No Encontrados", "$notFound", StatusNotFound, Modifier.weight(1f))
                    StatBadge("Agregados", "$added", StatusAdded, Modifier.weight(1f))
                }
                if (transferred > 0) {
                    StatBadge("Traspasados", "$transferred", StatusTransferred, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1)
    }
}
