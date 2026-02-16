package com.seretail.inventarios.ui.crosscount

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.components.statusColor
import com.seretail.inventarios.ui.components.statusLabel
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

data class ComparisonItem(
    val barcode: String,
    val description: String?,
    val session1Status: Int?,
    val session2Status: Int?,
    val discrepancy: DiscrepancyType,
)

enum class DiscrepancyType {
    MATCH,
    MISSING_IN_SESSION2,
    MISSING_IN_SESSION1,
    STATUS_MISMATCH,
}

@Composable
fun CrossCountScreen(
    session1Registros: List<ActivoFijoRegistroEntity>,
    session2Registros: List<ActivoFijoRegistroEntity>,
    session1Name: String,
    session2Name: String,
    onBackClick: () -> Unit,
) {
    var comparisons by remember { mutableStateOf<List<ComparisonItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(session1Registros, session2Registros) {
        val map1 = session1Registros.associateBy { it.codigoBarras }
        val map2 = session2Registros.associateBy { it.codigoBarras }
        val allBarcodes = (map1.keys + map2.keys).distinct().sorted()

        comparisons = allBarcodes.map { barcode ->
            val r1 = map1[barcode]
            val r2 = map2[barcode]
            val discrepancy = when {
                r1 != null && r2 == null -> DiscrepancyType.MISSING_IN_SESSION2
                r1 == null && r2 != null -> DiscrepancyType.MISSING_IN_SESSION1
                r1 != null && r2 != null && r1.statusId != r2.statusId -> DiscrepancyType.STATUS_MISMATCH
                else -> DiscrepancyType.MATCH
            }
            ComparisonItem(
                barcode = barcode,
                description = r1?.descripcion ?: r2?.descripcion,
                session1Status = r1?.statusId,
                session2Status = r2?.statusId,
                discrepancy = discrepancy,
            )
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "Cruce de Conteos",
                onBackClick = onBackClick,
            )
        },
        containerColor = DarkBackground,
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SERBlue)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Summary stats
            val matches = comparisons.count { it.discrepancy == DiscrepancyType.MATCH }
            val missingIn2 = comparisons.count { it.discrepancy == DiscrepancyType.MISSING_IN_SESSION2 }
            val missingIn1 = comparisons.count { it.discrepancy == DiscrepancyType.MISSING_IN_SESSION1 }
            val statusMismatch = comparisons.count { it.discrepancy == DiscrepancyType.STATUS_MISMATCH }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = SERBlue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Resumen de Comparación", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryBadge("Coinciden", matches, StatusFound)
                        SummaryBadge("Faltantes", missingIn2 + missingIn1, StatusNotFound)
                        SummaryBadge("Diferente estado", statusMismatch, Warning)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$session1Name  vs  $session2Name",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
            }

            // Comparison list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
            ) {
                items(comparisons) { item ->
                    ComparisonCard(item, session1Name, session2Name)
                }
            }
        }
    }
}

@Composable
private fun SummaryBadge(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun ComparisonCard(item: ComparisonItem, session1Name: String, session2Name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (item.discrepancy) {
                DiscrepancyType.MATCH -> DarkSurface
                DiscrepancyType.STATUS_MISMATCH -> Warning.copy(alpha = 0.08f)
                else -> StatusNotFound.copy(alpha = 0.08f)
            },
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (item.discrepancy) {
                    DiscrepancyType.MATCH -> Icons.Default.CheckCircle
                    DiscrepancyType.STATUS_MISMATCH -> Icons.Default.Warning
                    else -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (item.discrepancy) {
                    DiscrepancyType.MATCH -> StatusFound
                    DiscrepancyType.STATUS_MISMATCH -> Warning
                    else -> StatusNotFound
                },
                modifier = Modifier.size(20.dp),
            )

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(item.barcode, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                if (!item.description.isNullOrBlank()) {
                    Text(item.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
                }

                when (item.discrepancy) {
                    DiscrepancyType.MISSING_IN_SESSION2 -> {
                        Text("Falta en: $session2Name", style = MaterialTheme.typography.labelSmall, color = StatusNotFound)
                    }
                    DiscrepancyType.MISSING_IN_SESSION1 -> {
                        Text("Falta en: $session1Name", style = MaterialTheme.typography.labelSmall, color = StatusNotFound)
                    }
                    DiscrepancyType.STATUS_MISMATCH -> {
                        Row {
                            Text(
                                "${statusLabel(item.session1Status ?: 0)} → ${statusLabel(item.session2Status ?: 0)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Warning,
                            )
                        }
                    }
                    DiscrepancyType.MATCH -> {}
                }
            }
        }
    }
}
