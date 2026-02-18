package com.seretail.inventarios.ui.activofijo

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTextField
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun AssetSearchScreen(
    sessionId: Long,
    onBackClick: () -> Unit,
    onScanBarcode: (() -> Unit)? = null,
    viewModel: AssetSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.setSession(sessionId)
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "Consulta de Activo",
                onBackClick = onBackClick,
            )
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
            // Search bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                SERTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchChanged,
                    label = "Código de barras o TAG",
                    modifier = Modifier.weight(1f),
                )
                if (onScanBarcode != null) {
                    IconButton(onClick = onScanBarcode) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Escanear", tint = SERBlue)
                    }
                }
            }

            Button(
                onClick = viewModel::search,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                enabled = state.searchQuery.isNotBlank() && !state.isSearching,
            ) {
                if (state.isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Buscar", fontWeight = FontWeight.SemiBold)
                }
            }

            // Results
            if (state.searched && !state.isSearching) {
                if (state.product == null && state.registro == null) {
                    // Not found
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            "No se encontró ningún activo con ese código.",
                            color = Error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                // Catalog info
                state.product?.let { product ->
                    SectionCard("Información del Catálogo") {
                        InfoRow("Código", product.codigoBarras)
                        InfoRow("Descripción", product.descripcion)
                        product.categoria?.let { InfoRow("Categoría", it) }
                        product.marca?.let { InfoRow("Marca", it) }
                        product.modelo?.let { InfoRow("Modelo", it) }
                        product.color?.let { InfoRow("Color", it) }
                        product.serie?.let { InfoRow("No. Serie", it) }
                    }
                }

                // Capture info
                state.registro?.let { registro ->
                    SectionCard("Registro Capturado") {
                        InfoRow("Código", registro.codigoBarras)
                        registro.descripcion?.let { InfoRow("Descripción", it) }
                        registro.categoria?.let { InfoRow("Categoría", it) }
                        registro.marca?.let { InfoRow("Marca", it) }
                        registro.modelo?.let { InfoRow("Modelo", it) }
                        registro.color?.let { InfoRow("Color", it) }
                        registro.serie?.let { InfoRow("No. Serie", it) }
                        registro.ubicacion?.let { InfoRow("Ubicación", it) }
                        registro.comentarios?.let { InfoRow("Comentarios", it) }
                        registro.tagNuevo?.let { InfoRow("Tag Nuevo", it) }
                        registro.serieRevisado?.let { InfoRow("Serie Revisado", it) }
                        InfoRow("Estado", when (registro.statusId) {
                            1 -> "Encontrado"
                            2 -> "No Encontrado"
                            3 -> "Agregado"
                            4 -> "Traspasado"
                            else -> "Desconocido"
                        })
                        registro.fechaCaptura?.let { InfoRow("Fecha", it) }
                        InfoRow("Sincronizado", if (registro.sincronizado) "Sí" else "No")
                    }

                    // Photos
                    val photos = listOfNotNull(registro.imagen1, registro.imagen2, registro.imagen3)
                    if (photos.isNotEmpty()) {
                        SectionCard("Fotos") {
                            Text("${photos.size} foto(s) capturada(s)", color = StatusFound, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = SERBlue)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.4f))
        Text(value, color = TextPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.6f))
    }
}
