package com.seretail.inventarios.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExportCsv: () -> Unit,
    onExportExcel: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Exportar Datos",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = "Selecciona el formato de exportación",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    TextButton(onClick = {
                        onExportCsv()
                        onDismiss()
                    }) {
                        Text("CSV", color = SERBlue)
                    }
                    TextButton(onClick = {
                        onExportExcel()
                        onDismiss()
                    }) {
                        Text("Excel (.xlsx)", color = SERBlue)
                    }
                }
            }
        }
    }
}
