package com.seretail.inventarios.ui.dashboard

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.Info
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.StatusTransferred
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

@Composable
fun DashboardScreen(
    onNavigateToInventario: () -> Unit = {},
    onNavigateToActivoFijo: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.syncMessage) {
        state.syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "SER Inventarios",
                isOnline = state.isOnline,
                actions = {
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            color = SERBlue,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = viewModel::syncNow) {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = "Sincronizar",
                                tint = if (state.pendingSyncCount > 0) Warning else TextMuted,
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Main stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = "Inventario",
                    count = state.inventarioCount,
                    icon = Icons.Default.Inventory2,
                    color = SERBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventario,
                )
                StatCard(
                    title = "Activo Fijo",
                    count = state.activoFijoCount,
                    icon = Icons.Default.QrCodeScanner,
                    color = Info,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToActivoFijo,
                )
            }

            // Pending sync
            if (state.pendingSyncCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.SyncProblem, contentDescription = null, tint = Warning, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pendientes de sincronizar", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text("${state.pendingSyncCount} registros", color = Warning, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Status breakdown section
            Text(
                "Desglose Activo Fijo",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MiniStatCard("Encontrados", state.foundCount, StatusFound, Modifier.weight(1f))
                MiniStatCard("No Encontrados", state.notFoundCount, StatusNotFound, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MiniStatCard("Agregados", state.addedCount, StatusAdded, Modifier.weight(1f))
                MiniStatCard("Traspasados", state.transferredCount, StatusTransferred, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                if (onClick != null) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Ir",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}
