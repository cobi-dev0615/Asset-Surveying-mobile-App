package com.seretail.inventarios.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.ui.components.LoadingOverlay
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioListScreen(
    onSessionClick: (Long) -> Unit,
    viewModel: InventarioListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { SERTopBar(title = "Inventario") },
        containerColor = DarkBackground,
    ) { padding ->
        if (uiState.isLoading) {
            LoadingOverlay()
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (uiState.sessions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            Text(
                                text = "No hay sesiones de inventario",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                            )
                            Text(
                                text = "Crea una sesión desde la plataforma web",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.sessions) { session ->
                            SessionCard(session = session, onClick = { onSessionClick(session.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: InventarioEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Inventory2,
            contentDescription = null,
            tint = SERBlue,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.nombre,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                text = "${session.empresaNombre ?: ""} — ${session.sucursalNombre ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            if (session.fechaCreacion != null) {
                Text(
                    text = session.fechaCreacion,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }
        val statusColor = when (session.estado) {
            "activo" -> StatusFound
            "finalizado" -> TextMuted
            else -> Warning
        }
        Text(
            text = session.estado.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
        )
    }
}
