package com.seretail.inventarios.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.StatusTransferred
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

fun statusColor(statusId: Int): Color = when (statusId) {
    1 -> StatusFound
    2 -> StatusNotFound
    3 -> StatusAdded
    4 -> StatusTransferred
    else -> StatusFound
}

fun statusLabel(statusId: Int): String = when (statusId) {
    1 -> "Encontrado"
    2 -> "No Encontrado"
    3 -> "Agregado"
    4 -> "Traspasado"
    else -> "Encontrado"
}

@Composable
fun ScanResultCard(
    barcode: String,
    description: String?,
    statusId: Int,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onEdit != null) Modifier.clickable { onEdit() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(8.dp, 40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor(statusId)),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = barcode,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = statusLabel(statusId),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor(statusId),
                )
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
