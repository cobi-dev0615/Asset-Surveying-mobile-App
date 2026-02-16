package com.seretail.inventarios.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.components.StatCard
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.StatusTransferred
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

@Composable
fun DashboardScreen(
    onNavigateToInventario: () -> Unit,
    onNavigateToActivoFijo: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "SER Inventarios",
                isOnline = isOnline,
                isSyncing = uiState.isSyncing,
                onSyncClick = viewModel::sync,
            )
        },
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
            // Welcome + empresa/sucursal
            Text(
                text = "Hola, ${uiState.userName}",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            if (uiState.empresaNombre != null || uiState.sucursalNombre != null) {
                Text(
                    text = listOfNotNull(uiState.empresaNombre, uiState.sucursalNombre)
                        .joinToString(" — "),
                    style = MaterialTheme.typography.bodySmall,
                    color = SERBlue,
                )
            }
            if (uiState.lastSync != null) {
                Text(
                    text = "Última sincronización: ${uiState.lastSync}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Overview stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = "Inventario",
                    value = "${uiState.inventarioRegistros}",
                    icon = Icons.Default.Inventory2,
                    iconColor = SERBlue,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "Activo Fijo",
                    value = "${uiState.activoFijoRegistros}",
                    icon = Icons.Default.Assignment,
                    iconColor = SERBlue,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "Pendientes",
                    value = "${uiState.pendingSync}",
                    icon = Icons.Default.CloudSync,
                    iconColor = if (uiState.pendingSync > 0) Warning else StatusFound,
                    modifier = Modifier.weight(1f),
                )
            }

            // Activo Fijo status breakdown
            if (uiState.activoFijoRegistros > 0) {
                Text(
                    text = "Desglose de Activos",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatCard(
                        title = "Encontrado",
                        value = "${uiState.afFound}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = StatusFound,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = "No Encontr.",
                        value = "${uiState.afNotFound}",
                        icon = Icons.Default.ErrorOutline,
                        iconColor = StatusNotFound,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatCard(
                        title = "Agregado",
                        value = "${uiState.afAdded}",
                        icon = Icons.Default.AddCircleOutline,
                        iconColor = StatusAdded,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = "Traspasado",
                        value = "${uiState.afTransferred}",
                        icon = Icons.Default.SwapHoriz,
                        iconColor = StatusTransferred,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )

            QuickActionCard(
                title = "Inventario",
                subtitle = "Captura de productos por código de barras",
                icon = Icons.Default.Inventory2,
                onClick = onNavigateToInventario,
            )

            QuickActionCard(
                title = "Activo Fijo",
                subtitle = "Auditoría de activos con fotos y ubicación",
                icon = Icons.Default.QrCodeScanner,
                onClick = onNavigateToActivoFijo,
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = title,
            tint = SERBlue,
            modifier = Modifier.padding(top = 2.dp),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
