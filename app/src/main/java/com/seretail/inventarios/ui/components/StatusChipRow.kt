package com.seretail.inventarios.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.StatusAdded
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.StatusTransferred
import com.seretail.inventarios.ui.theme.TextPrimary

data class StatusOption(val id: Int, val label: String, val color: Color)

val statusOptions = listOf(
    StatusOption(1, "Encontrado", StatusFound),
    StatusOption(2, "No Encontrado", StatusNotFound),
    StatusOption(3, "Agregado", StatusAdded),
    StatusOption(4, "Traspasado", StatusTransferred),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusChipRow(
    selectedId: Int,
    onStatusSelected: (Int) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        statusOptions.forEach { option ->
            val isSelected = option.id == selectedId
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) TextPrimary else option.color,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isSelected) Modifier.background(option.color.copy(alpha = 0.8f))
                        else Modifier.border(1.dp, option.color, RoundedCornerShape(16.dp))
                    )
                    .clickable { onStatusSelected(option.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}
