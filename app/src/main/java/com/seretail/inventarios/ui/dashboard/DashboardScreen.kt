package com.seretail.inventarios.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.BarChart
import com.seretail.inventarios.ui.components.PieChart
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.Info
import com.seretail.inventarios.ui.theme.RoleAdmin
import com.seretail.inventarios.ui.theme.RoleCapturista
import com.seretail.inventarios.ui.theme.RoleInvitado
import com.seretail.inventarios.ui.theme.RoleSupervisor
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
    onNavigateToRfid: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val avatarInitials = state.userName
        ?.split(" ")
        ?.take(2)
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.joinToString("") ?: "?"

    val avatarColor = when (state.userRolId) {
        1 -> RoleAdmin
        2 -> RoleSupervisor
        3 -> RoleCapturista
        else -> RoleInvitado
    }

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
                    // Avatar button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                            .clickable(onClick = onProfileClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = avatarInitials,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    // Sync button
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
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color(0xFF1A1A1A),
                    actionColor = SERBlue,
                )
            }
        },
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
            // === 3 MODULE CARDS (equal weight) ===
            Text(
                "Módulos",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ModuleCard(
                    title = "Inventario",
                    subtitle = "Conteo rápido",
                    count = state.inventarioCount,
                    countLabel = "sesiones",
                    icon = Icons.Default.Inventory2,
                    color = SERBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventario,
                )
                ModuleCard(
                    title = "Activo Fijo",
                    subtitle = "Registro activos",
                    count = state.activoFijoCount,
                    countLabel = "sesiones",
                    icon = Icons.Default.QrCodeScanner,
                    color = Info,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToActivoFijo,
                )
                ModuleCard(
                    title = "RFID",
                    subtitle = "Lector tags",
                    count = null,
                    countLabel = null,
                    icon = Icons.Default.Nfc,
                    color = Warning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRfid,
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

            // === ACTIVO FIJO STATUS BREAKDOWN ===
            if (state.foundCount + state.notFoundCount + state.addedCount + state.transferredCount > 0) {
                Text(
                    "Desglose Activo Fijo",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
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

            // Charts section
            if (state.progressSlices.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Avance General",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PieChart(
                            slices = state.progressSlices,
                            size = 90.dp,
                            strokeWidth = 14.dp,
                        )
                        Spacer(Modifier.width(20.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ChartLegendItem("Encontrados", state.foundCount, StatusFound)
                            ChartLegendItem("No Encontrados", state.notFoundCount, StatusNotFound)
                            ChartLegendItem("Agregados", state.addedCount, StatusAdded)
                            ChartLegendItem("Traspasados", state.transferredCount, StatusTransferred)
                        }
                    }
                }
            }

            if (state.categoryBars.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Activos por Categoría",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    BarChart(
                        data = state.categoryBars,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    title: String,
    subtitle: String,
    count: Int?,
    countLabel: String?,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            if (count != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                if (countLabel != null) {
                    Text(
                        text = countLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
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
