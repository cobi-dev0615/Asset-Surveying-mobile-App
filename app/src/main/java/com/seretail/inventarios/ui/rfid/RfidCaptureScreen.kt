package com.seretail.inventarios.ui.rfid

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.seretail.inventarios.data.local.entity.RfidTagEntity
import com.seretail.inventarios.rfid.RfidState
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

@Composable
fun RfidCaptureScreen(
    onBackClick: () -> Unit,
    viewModel: RfidCaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSessionPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "RFID",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = viewModel::toggleFilter) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = if (state.showMatchedOnly) SERBlue else TextMuted,
                        )
                    }
                    IconButton(onClick = viewModel::clearSession) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar", tint = TextMuted)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Connection status card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = when (state.rfidState) {
                                is RfidState.Connected, is RfidState.Scanning -> StatusFound
                                is RfidState.Connecting -> Warning
                                is RfidState.Error -> Error
                                else -> TextMuted
                            },
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (state.rfidState) {
                                    is RfidState.Disconnected -> "Desconectado"
                                    is RfidState.Connecting -> "Conectando..."
                                    is RfidState.Connected -> "Conectado"
                                    is RfidState.Scanning -> "Escaneando..."
                                    is RfidState.Error -> "Error: ${(state.rfidState as RfidState.Error).message}"
                                },
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        when (state.rfidState) {
                            is RfidState.Disconnected, is RfidState.Error -> {
                                Button(
                                    onClick = viewModel::connect,
                                    colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                                    shape = RoundedCornerShape(8.dp),
                                ) { Text("Conectar") }
                            }
                            is RfidState.Connecting -> {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SERBlue, strokeWidth = 2.dp)
                            }
                            is RfidState.Connected, is RfidState.Scanning -> {
                                OutlinedButton(
                                    onClick = viewModel::disconnect,
                                    shape = RoundedCornerShape(8.dp),
                                ) { Text("Desconectar", color = TextSecondary) }
                            }
                        }
                    }

                    // Scanning animation
                    if (state.rfidState is RfidState.Scanning) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            color = SERBlue,
                            trackColor = DarkSurfaceVariant,
                        )
                    }
                }
            }

            // Session selector
            Box {
                OutlinedButton(
                    onClick = { showSessionPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = state.sessions.find { it.id == state.selectedSessionId }?.nombre ?: "Seleccionar sesión",
                        color = if (state.selectedSessionId != null) TextPrimary else TextMuted,
                    )
                }
                DropdownMenu(
                    expanded = showSessionPicker,
                    onDismissRequest = { showSessionPicker = false },
                ) {
                    state.sessions.forEach { session ->
                        DropdownMenuItem(
                            text = { Text(session.nombre, color = TextPrimary) },
                            onClick = {
                                viewModel.selectSession(session.id)
                                showSessionPicker = false
                            },
                        )
                    }
                }
            }

            // Power slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Text("Potencia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.width(8.dp))
                Slider(
                    value = state.power.toFloat(),
                    onValueChange = { viewModel.setPower(it.toInt()) },
                    valueRange = 0f..30f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SERBlue,
                        activeTrackColor = SERBlue,
                        inactiveTrackColor = DarkSurfaceVariant,
                    ),
                )
                Text("${state.power}", color = TextPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(24.dp))
            }

            // Start/Stop scan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.rfidState is RfidState.Scanning) {
                    Button(
                        onClick = viewModel::stopScan,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Detener Escaneo")
                    }
                } else {
                    Button(
                        onClick = viewModel::startScan,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.rfidState is RfidState.Connected,
                        colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar Escaneo")
                    }
                }
            }

            // Counts
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.totalCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.matchedCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = StatusFound)
                    Text("Coincidentes", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.totalCount - state.matchedCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = StatusNotFound)
                    Text("Sin coincidencia", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            // Tag list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
            ) {
                val filteredTags = viewModel.getFilteredTags()
                items(filteredTags, key = { it.id }) { tag ->
                    RfidTagCard(tag)
                }
            }
        }
    }
}

@Composable
private fun RfidTagCard(tag: RfidTagEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Match indicator
            Box(
                modifier = Modifier
                    .size(8.dp, 32.dp)
                    .background(
                        if (tag.matched) StatusFound else DarkSurfaceVariant,
                        RoundedCornerShape(4.dp),
                    ),
            )

            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            ) {
                Text(
                    text = tag.epc,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                )
                Text(
                    text = if (tag.matched) "Coincide" else "Sin coincidencia",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tag.matched) StatusFound else TextMuted,
                )
            }

            // RSSI bar
            Column(horizontalAlignment = Alignment.End) {
                Text("${tag.rssi} dBm", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                // Simple signal strength bar
                val strength = ((tag.rssi + 90).coerceIn(0, 60).toFloat() / 60f)
                LinearProgressIndicator(
                    progress = { strength },
                    modifier = Modifier.width(48.dp).height(4.dp),
                    color = when {
                        strength > 0.6f -> StatusFound
                        strength > 0.3f -> Warning
                        else -> Error
                    },
                    trackColor = DarkSurfaceVariant,
                )
                Text("x${tag.readCount}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}
