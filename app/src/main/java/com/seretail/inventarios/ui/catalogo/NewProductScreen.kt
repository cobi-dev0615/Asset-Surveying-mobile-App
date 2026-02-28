package com.seretail.inventarios.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    onScanBarcode: () -> Unit = {},
    viewModel: NewProductViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val successMessage = if (state.isEditMode) "Producto actualizado exitosamente" else "Producto guardado exitosamente"

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar(successMessage)
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            SERTopBar(
                title = if (state.isEditMode) "Editar Producto" else "Nuevo Producto",
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

            // Section: Codigos
            SectionLabel("Codigos de Barras")

            // Codigo 1 (required) with scanner
            BarcodeField(
                value = state.codigoBarras,
                onValueChange = viewModel::onCodigoBarrasChanged,
                label = "Codigo 1 (Principal) *",
                onScanClick = {
                    viewModel.setScanTarget(ScanTarget.CODIGO1)
                    onScanBarcode()
                },
            )

            // Codigo 2 (SKU) with scanner
            BarcodeField(
                value = state.codigo2,
                onValueChange = viewModel::onCodigo2Changed,
                label = "Codigo 2 (SKU)",
                onScanClick = {
                    viewModel.setScanTarget(ScanTarget.CODIGO2)
                    onScanBarcode()
                },
            )

            // Codigo 3 with scanner
            BarcodeField(
                value = state.codigo3,
                onValueChange = viewModel::onCodigo3Changed,
                label = "Codigo 3",
                onScanClick = {
                    viewModel.setScanTarget(ScanTarget.CODIGO3)
                    onScanBarcode()
                },
            )

            Spacer(Modifier.height(4.dp))
            SectionLabel("Informacion del Producto")

            // Descripcion (required)
            ProductTextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChanged,
                label = "Descripcion *",
            )

            // Categoria
            ProductTextField(
                value = state.categoria,
                onValueChange = viewModel::onCategoriaChanged,
                label = "Categoria",
            )

            // Marca
            ProductTextField(
                value = state.marca,
                onValueChange = viewModel::onMarcaChanged,
                label = "Marca",
            )

            // Modelo
            ProductTextField(
                value = state.modelo,
                onValueChange = viewModel::onModeloChanged,
                label = "Modelo",
            )

            // Color
            ProductTextField(
                value = state.color,
                onValueChange = viewModel::onColorChanged,
                label = "Color",
            )

            // Serie
            ProductTextField(
                value = state.serie,
                onValueChange = viewModel::onSerieChanged,
                label = "No. Serie",
            )

            Spacer(Modifier.height(4.dp))
            SectionLabel("Datos Numericos")

            // Unidad de Medida
            ProductTextField(
                value = state.unidadMedida,
                onValueChange = viewModel::onUnidadMedidaChanged,
                label = "Unidad de Medida",
            )

            // Precio Venta
            ProductTextField(
                value = state.precioVenta,
                onValueChange = viewModel::onPrecioVentaChanged,
                label = "Precio Venta",
                keyboardType = KeyboardType.Decimal,
            )

            // Cantidad Teorica
            ProductTextField(
                value = state.cantidadTeorica,
                onValueChange = viewModel::onCantidadTeoricaChanged,
                label = "Cantidad Teorica",
                keyboardType = KeyboardType.Decimal,
            )

            // Factor
            ProductTextField(
                value = state.factor,
                onValueChange = viewModel::onFactorChanged,
                label = "Factor",
                keyboardType = KeyboardType.Decimal,
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
                    text = if (state.isEditMode) "Actualizar Producto" else "Guardar Producto",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = SERBlue,
        modifier = Modifier.padding(bottom = 2.dp),
    )
}

@Composable
private fun BarcodeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onScanClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = TextMuted) },
            modifier = Modifier.weight(1f),
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
        IconButton(onClick = onScanClick) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Escanear",
                tint = SERBlue,
            )
        }
    }
}

@Composable
private fun ProductTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
