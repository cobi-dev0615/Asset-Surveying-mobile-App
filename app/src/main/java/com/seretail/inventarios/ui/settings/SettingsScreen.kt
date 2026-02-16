package com.seretail.inventarios.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLogoutDialog,
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(onLogout) }) {
                    Text("Sí, cerrar sesión", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLogoutDialog) {
                    Text("Cancelar")
                }
            },
        )
    }

    Scaffold(
        topBar = { SERTopBar(title = "Ajustes") },
        containerColor = DarkBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // User info
            if (uiState.userName.isNotBlank()) {
                SectionCard(title = "Usuario") {
                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                    )
                }
            }

            // Server
            SectionCard(title = "Servidor") {
                OutlinedTextField(
                    value = uiState.serverUrl,
                    onValueChange = viewModel::onServerUrlChanged,
                    label = { Text("URL del Servidor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SERBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = SERBlue,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = SERBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
            }

            // Sync
            SectionCard(title = "Sincronización") {
                SettingsSwitch(
                    label = "Sincronización automática",
                    checked = uiState.autoSync,
                    onCheckedChange = viewModel::onAutoSyncChanged,
                )
                SettingsSwitch(
                    label = "Solo con WiFi",
                    checked = uiState.syncWifiOnly,
                    onCheckedChange = viewModel::onSyncWifiOnlyChanged,
                )
            }

            // Camera
            SectionCard(title = "Cámara y Escáner") {
                SettingsSwitch(
                    label = "Usar cámara del dispositivo",
                    checked = uiState.useCamera,
                    onCheckedChange = viewModel::onUseCameraChanged,
                )
            }

            // About
            SectionCard(title = "Acerca de") {
                Text(
                    text = "SER Inventarios v1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Text(
                    text = "Servicios Empresariales Retail",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            // Logout
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = viewModel::showLogoutDialog,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Error),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Cerrar Sesión", color = TextPrimary)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = SERBlue,
        )
        content()
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = SERBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DarkBorder,
            ),
        )
    }
}
