package com.seretail.inventarios.ui.activofijo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    viewModel: ActivoFijoListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { SERTopBar(title = "Activo Fijo") },
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
                    Text("Sincroniza para obtener sesiones", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            ) {
                items(state.sessions, key = { it.id }) { session ->
                    SessionCard(session = session, onClick = { onSessionClick(session.id) })
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: ActivoFijoSessionEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
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
