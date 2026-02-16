package com.seretail.inventarios.ui.printing

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.seretail.inventarios.printing.BluetoothPrinterManager
import com.seretail.inventarios.printing.PrinterState
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.ui.theme.Warning

@Composable
fun PrinterSelectionDialog(
    printerManager: BluetoothPrinterManager,
    currentMac: String?,
    onPrinterSelected: (mac: String, name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val printerState by printerManager.state.collectAsState()
    val devices = remember { printerManager.getPairedDevices() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Seleccionar Impresora",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = "Dispositivos Bluetooth vinculados",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )

                // Connection status
                when (printerState) {
                    is PrinterState.Connecting -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SERBlue, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Conectando...", color = SERBlue, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is PrinterState.Connected -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = StatusFound, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Conectada", color = StatusFound, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is PrinterState.Error -> {
                        Text(
                            text = "Error: ${(printerState as PrinterState.Error).message}",
                            color = Warning,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    else -> {}
                }

                if (devices.isEmpty()) {
                    Text(
                        text = "No hay dispositivos vinculados.\nVincula una impresora en Ajustes de Bluetooth.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(devices) { device ->
                            PrinterDeviceItem(
                                device = device,
                                isSelected = device.address == currentMac,
                                onClick = {
                                    onPrinterSelected(device.address, device.name ?: "Impresora")
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", color = TextSecondary)
                    }
                    if (printerState is PrinterState.Connected) {
                        TextButton(onClick = {
                            printerManager.print("Test SER Inventarios\n\n\n".toByteArray())
                        }) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = SERBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Prueba", color = SERBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrinterDeviceItem(
    device: BluetoothDevice,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SERBlue.copy(alpha = 0.12f) else DarkSurfaceVariant,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                tint = if (isSelected) SERBlue else TextMuted,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Dispositivo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SERBlue, modifier = Modifier.size(18.dp))
            }
        }
    }
}
