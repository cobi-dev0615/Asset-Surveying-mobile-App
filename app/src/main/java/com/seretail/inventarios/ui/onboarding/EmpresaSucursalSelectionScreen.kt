package com.seretail.inventarios.ui.onboarding

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
fun EmpresaSucursalSelectionScreen(
    onSelectionComplete: () -> Unit,
    viewModel: EmpresaSucursalSelectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.selectionComplete) {
        if (state.selectionComplete) onSelectionComplete()
    }

    val title = if (state.step == 1) "EMPRESAS DISPONIBLES" else "SUCURSALES DISPONIBLES"
    val onBack: (() -> Unit)? = if (state.step == 2) viewModel::goBack else null

    Scaffold(
        topBar = {
            SERTopBar(
                title = title,
                onBackClick = onBack,
                actions = {
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = SERBlue,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = TextMuted)
                        }
                    }
                },
            )
        },
        containerColor = DarkBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChanged,
                placeholder = {
                    Text(
                        if (state.step == 1) "Buscar empresa..." else "Buscar sucursal...",
                        color = TextMuted,
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = SERBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    cursorColor = SERBlue,
                ),
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SERBlue)
                }
            } else if (state.step == 1) {
                // Empresa list
                val items = viewModel.getFilteredEmpresas()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { empresa ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectEmpresa(empresa) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(empresa.codigo, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(empresa.nombre, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f))
                    }
                }
            } else {
                // Sucursal list
                val items = viewModel.getFilteredSucursales()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { sucursal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSucursal(sucursal) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sucursal.codigo ?: "", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(sucursal.nombre, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }

    // Confirmation dialog
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissConfirm,
            containerColor = DarkSurface,
            title = { Text("Confirmación", color = TextPrimary) },
            text = {
                Text(
                    "¿Deseas continuar con la sucursal ${state.selectedSucursal?.nombre}?",
                    color = TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmSelection,
                    colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                ) { Text("Sí, continuar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissConfirm) {
                    Text("No", color = TextMuted)
                }
            },
        )
    }
}
