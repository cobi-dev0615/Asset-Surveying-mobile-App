package com.seretail.inventarios.ui.activofijo

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun ActivoFijoListScreen(
    onSessionClick: (Long) -> Unit,
    onCompareClick: (Long, Long) -> Unit = { _, _ -> },
    viewModel: ActivoFijoListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate to newly created session
    LaunchedEffect(state.createdSessionId) {
        state.createdSessionId?.let {
            onSessionClick(it)
            viewModel.clearCreatedSession()
        }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = if (state.compareMode) "Selecciona 2 sesiones" else "Activo Fijo",
                actions = {
                    if (state.sessions.size >= 2) {
                        IconButton(onClick = { viewModel.toggleCompareMode() }) {
                            Icon(
                                Icons.Default.CompareArrows,
                                contentDescription = "Comparar",
                                tint = if (state.compareMode) SERBlue else TextMuted,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!state.compareMode) {
                FloatingActionButton(
                    onClick = { viewModel.showCreateDialog() },
                    containerColor = SERBlue,
                    contentColor = TextPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear sesión")
                }
            }
        },
        containerColor = DarkBackground,
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SERBlue)
            }
        } else if (state.sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text("No hay sesiones de activo fijo", color = TextMuted, modifier = Modifier.padding(top = 8.dp))
                    Text("Toca + para crear una nueva", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                ) {
                    items(state.sessions, key = { it.id }) { session ->
                        val isSelected = session.id in state.selectedForCompare
                        SessionCard(
                            session = session,
                            isSelected = isSelected,
                            compareMode = state.compareMode,
                            onClick = {
                                if (state.compareMode) {
                                    viewModel.toggleCompareSelection(session.id)
                                } else {
                                    onSessionClick(session.id)
                                }
                            },
                        )
                    }
                }

                // Compare button when 2 sessions selected
                if (state.compareMode && state.selectedForCompare.size == 2) {
                    Button(
                        onClick = {
                            val ids = state.selectedForCompare.toList()
                            viewModel.toggleCompareMode()
                            onCompareClick(ids[0], ids[1])
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Comparar Sesiones", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Create session dialog
    if (state.showCreateDialog) {
        var nombre by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateDialog() },
            title = { Text("Nueva Sesión Activo Fijo", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de la sesión") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SERBlue,
                            focusedLabelColor = SERBlue,
                            unfocusedTextColor = TextPrimary,
                            focusedTextColor = TextPrimary,
                        ),
                    )
                    if (state.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (state.isCreating) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator(color = SERBlue, modifier = Modifier.size(24.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.createSession(nombre) },
                    enabled = !state.isCreating,
                ) { Text("Crear", color = SERBlue) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateDialog() }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
        )
    }
}

@Composable
private fun SessionCard(
    session: ActivoFijoSessionEntity,
    isSelected: Boolean = false,
    compareMode: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SERBlue.copy(alpha = 0.15f) else DarkSurface,
        ),
        border = if (isSelected) BorderStroke(2.dp, SERBlue) else null,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )

            Spacer(Modifier.size(8.dp))

            if (session.empresaNombre != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(session.empresaNombre, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            if (session.sucursalNombre != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(session.sucursalNombre, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            if (session.fechaCreacion != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(session.fechaCreacion, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = session.estado.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.estado == "activo") SERBlue else TextMuted,
                )
            }
        }
    }
}
