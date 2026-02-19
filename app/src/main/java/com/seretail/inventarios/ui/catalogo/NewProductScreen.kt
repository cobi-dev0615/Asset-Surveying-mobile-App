package com.seretail.inventarios.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary

@Composable
fun NewProductScreen(
    onBackClick: () -> Unit,
    viewModel: NewProductViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back on save success
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar("Producto guardado exitosamente")
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = "Nuevo Producto",
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // Error message
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            // Codigo de Barras (required)
            DarkOutlinedTextField(
                value = state.codigoBarras,
                onValueChange = viewModel::onCodigoBarrasChanged,
                label = "Codigo de Barras *",
            )

            // Descripcion (required)
            DarkOutlinedTextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChanged,
                label = "Descripcion *",
            )

            // Categoria
            DarkOutlinedTextField(
                value = state.categoria,
                onValueChange = viewModel::onCategoriaChanged,
                label = "Categoria",
            )

            // Marca
            DarkOutlinedTextField(
                value = state.marca,
                onValueChange = viewModel::onMarcaChanged,
                label = "Marca",
            )

            // Modelo
            DarkOutlinedTextField(
                value = state.modelo,
                onValueChange = viewModel::onModeloChanged,
                label = "Modelo",
            )

            // Color
            DarkOutlinedTextField(
                value = state.color,
                onValueChange = viewModel::onColorChanged,
                label = "Color",
            )

            // Serie
            DarkOutlinedTextField(
                value = state.serie,
                onValueChange = viewModel::onSerieChanged,
                label = "Serie",
            )

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = SERBlue),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = "Guardar Producto",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DarkOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted) },
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
}
