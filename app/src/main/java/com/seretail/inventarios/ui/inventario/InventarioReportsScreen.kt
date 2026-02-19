package com.seretail.inventarios.ui.inventario

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun InventarioReportsScreen(
    onBackClick: () -> Unit,
    viewModel: InventarioReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showSessionDropdown by remember { mutableStateOf(false) }
    var showReportTypeDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "Reportes",
                onBackClick = onBackClick,
            )
        },
        containerColor = DarkBackground,
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

            Spacer(Modifier.height(8.dp))

            // Results header
            Text(
                text = "Resultados (${state.groupedData.size})",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 4.dp),
            )

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
private fun SummaryStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
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

@Composable
private fun ReportItemCard(item: GroupedReport, reportType: ReportType) {
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

            Spacer(Modifier.width(12.dp))

            // Quantity and count badges
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
