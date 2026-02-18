package com.seretail.inventarios.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seretail.inventarios.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Logo / branding
            Image(
                painter = painterResource(id = R.drawable.logo_ser),
                contentDescription = "SER Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = "SER Inventarios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = Error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Error.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                )
            }

            // Usuario field
            OutlinedTextField(
                value = state.usuario,
                onValueChange = viewModel::onUsuarioChanged,
                label = { Text("Usuario", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = SERBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    cursorColor = SERBlue,
                    focusedLabelColor = SERBlue,
                    unfocusedLabelColor = TextMuted,
                ),
            )

            // Password field
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Contraseña", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.login()
                }),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                            tint = TextMuted,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = SERBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    cursorColor = SERBlue,
                    focusedLabelColor = SERBlue,
                    unfocusedLabelColor = TextMuted,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Login button
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v1.0.0 — SER Inventarios",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )

            TextButton(onClick = viewModel::toggleServerDialog) {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Conexión", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }

        // Server URL dialog
        if (state.showServerDialog) {
            AlertDialog(
                onDismissRequest = viewModel::toggleServerDialog,
                containerColor = DarkSurface,
                title = { Text("Conexión de servidor", color = TextPrimary) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = state.serverUrl,
                            onValueChange = viewModel::onServerUrlChanged,
                            label = { Text("URL del servidor", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = SERBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant,
                                cursorColor = SERBlue,
                                focusedLabelColor = SERBlue,
                                unfocusedLabelColor = TextMuted,
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cambiar la información de conexión puede ocasionar el mal funcionamiento de la aplicación.",
                            color = Error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Error.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::saveServerUrl,
                        colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                    ) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::toggleServerDialog) {
                        Text("Cancelar", color = TextMuted)
                    }
                },
            )
        }
    }
}
