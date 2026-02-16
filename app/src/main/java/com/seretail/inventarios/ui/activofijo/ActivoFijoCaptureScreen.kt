package com.seretail.inventarios.ui.activofijo

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.components.ScanResultCard
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.StatusTransferred
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

private data class StatusOption(val id: Int, val label: String, val color: androidx.compose.ui.graphics.Color)

private val statusOptions = listOf(
    StatusOption(1, "Encontrado", StatusFound),
    StatusOption(2, "No Encontrado", StatusNotFound),
    StatusOption(3, "Agregado", StatusAdded),
    StatusOption(4, "Traspasado", StatusTransferred),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivoFijoCaptureScreen(
    sessionId: Long,
    onBackClick: () -> Unit,
    onScanClick: () -> Unit,
    viewModel: ActivoFijoCaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            // Edit mode banner
            if (uiState.isEditMode) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Warning.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Warning, modifier = Modifier.size(18.dp))
                            Text("Modo edición", style = MaterialTheme.typography.labelLarge, color = Warning)
                        }
                        TextButton(onClick = viewModel::cancelEdit) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    }
                }
            }

            // Area / Zone
            item {
                OutlinedTextField(
                    value = uiState.area,
                    onValueChange = viewModel::onAreaChanged,
                    label = { Text("Área / Zona") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors(),
                )
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
                        colors = fieldColors(),
                    )
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Default.CameraAlt, "Escanear", tint = SERBlue)
                    }
                }
            }

            // Description
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

            // Category + Brand (with autocomplete dropdown)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.category,
                        onValueChange = viewModel::onCategoryChanged,
                        label = { Text("Categoría") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = uiState.brand,
                            onValueChange = viewModel::onBrandChanged,
                            label = { Text("Marca") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = fieldColors(),
                        )
                        DropdownMenu(
                            expanded = uiState.showBrandSuggestions,
                            onDismissRequest = viewModel::dismissBrandSuggestions,
                        ) {
                            uiState.brandSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = { viewModel.selectBrandSuggestion(suggestion) },
                                )
                            }
                        }
                    }
                }
            }

            // Model + Color
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.model,
                        onValueChange = viewModel::onModelChanged,
                        label = { Text("Modelo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                    OutlinedTextField(
                        value = uiState.color,
                        onValueChange = viewModel::onColorChanged,
                        label = { Text("Color") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                }
            }

            // Serie + Location
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.serie,
                        onValueChange = viewModel::onSerieChanged,
                        label = { Text("No. Serie") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                    OutlinedTextField(
                        value = uiState.location,
                        onValueChange = viewModel::onLocationChanged,
                        label = { Text("Ubicación") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                }
            }

            // Status selector
            item {
                Text("Estado", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    statusOptions.forEach { option ->
                        val selected = uiState.selectedStatus == option.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) option.color.copy(alpha = 0.2f)
                                    else DarkSurfaceVariant,
                                )
                                .clickable { viewModel.onStatusChanged(option.id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) option.color else TextSecondary,
                            )
                        }
                    }
                }
            }

            // Photo slots (3 photos)
            item {
                Text("Fotografías", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PhotoSlot(
                        photoUri = uiState.photo1,
                        slot = 1,
                        onCapture = { viewModel.startPhotoCapture(1) },
                        onRemove = { viewModel.removePhoto(1) },
                        modifier = Modifier.weight(1f),
                    )
                    PhotoSlot(
                        photoUri = uiState.photo2,
                        slot = 2,
                        onCapture = { viewModel.startPhotoCapture(2) },
                        onRemove = { viewModel.removePhoto(2) },
                        modifier = Modifier.weight(1f),
                    )
                    PhotoSlot(
                        photoUri = uiState.photo3,
                        slot = 3,
                        onCapture = { viewModel.startPhotoCapture(3) },
                        onRemove = { viewModel.removePhoto(3) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Save / Update button
            item {
                Button(
                    onClick = viewModel::saveRegistro,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isEditMode) Warning else SERBlue,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        if (uiState.isEditMode) "Actualizar Activo" else "Guardar Activo",
                        color = TextPrimary,
                    )
                }
            }

            // Counter + category filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${uiState.capturedCount} activos capturados",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    if (uiState.categories.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val allSelected = uiState.selectedCategoryFilter == null
                            Text(
                                text = "Todos",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (allSelected) SERBlue else TextSecondary,
                                modifier = Modifier
                                    .background(
                                        if (allSelected) SERBlue.copy(alpha = 0.15f) else DarkSurface,
                                        RoundedCornerShape(4.dp),
                                    )
                                    .clickable { viewModel.onCategoryFilterChanged(null) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                            uiState.categories.take(3).forEach { cat ->
                                val catSelected = uiState.selectedCategoryFilter == cat
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (catSelected) SERBlue else TextSecondary,
                                    modifier = Modifier
                                        .background(
                                            if (catSelected) SERBlue.copy(alpha = 0.15f) else DarkSurface,
                                            RoundedCornerShape(4.dp),
                                        )
                                        .clickable { viewModel.onCategoryFilterChanged(cat) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Registros list (filtered)
            val filteredRegistros = viewModel.getFilteredRegistros()
            items(filteredRegistros) { reg ->
                val statusLabel = statusOptions.find { it.id == reg.statusId }
                ScanResultCard(
                    code = reg.codigoBarras,
                    description = reg.descripcion ?: "Sin descripción",
                    subtitle = listOfNotNull(reg.categoria, reg.marca, reg.ubicacion)
                        .joinToString(" — "),
                    statusText = statusLabel?.label,
                    statusColor = statusLabel?.color ?: TextSecondary,
                    onDelete = { viewModel.deleteRegistro(reg.id) },
                    onClick = { viewModel.enterEditMode(reg) },
                )
            }
        }
    }
}

@Composable
private fun PhotoSlot(
    photoUri: String?,
    slot: Int,
    onCapture: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onCapture),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = Uri.parse(photoUri),
                contentDescription = "Foto $slot",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = Error,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = "Tomar foto $slot",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    "Foto $slot",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
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
