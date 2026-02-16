package com.seretail.inventarios.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun EmpresaSucursalScreen(
    onSelectionComplete: () -> Unit,
    viewModel: EmpresaSucursalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onSelectionComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = SERBlue,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                // Header
                Text(
                    text = "SER",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SERBlue,
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (uiState.step) {
                    SelectionStep.EMPRESA -> {
                        Text(
                            text = "Selecciona una Empresa",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Text(
                            text = "${uiState.empresas.size} empresas disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(uiState.empresas) { empresa ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurface, RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectEmpresa(empresa) }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Default.Business,
                                            contentDescription = null,
                                            tint = SERBlue,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Column {
                                            Text(
                                                text = empresa.nombre,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary,
                                            )
                                            if (empresa.codigo.isNotBlank()) {
                                                Text(
                                                    text = empresa.codigo,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary,
                                                )
                                            }
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                    )
                                }
                            }
                        }
                    }

                    SelectionStep.SUCURSAL -> {
                        Text(
                            text = "Selecciona una Sucursal",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = uiState.selectedEmpresa?.nombre ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = SERBlue,
                            )
                            if (uiState.empresas.size > 1) {
                                TextButton(onClick = viewModel::goBackToEmpresa) {
                                    Text("Cambiar", color = TextSecondary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(uiState.sucursales) { sucursal ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurface, RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectSucursal(sucursal) }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Default.Store,
                                            contentDescription = null,
                                            tint = SERBlue,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Column {
                                            Text(
                                                text = sucursal.nombre,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary,
                                            )
                                            if (!sucursal.codigo.isNullOrBlank()) {
                                                Text(
                                                    text = sucursal.codigo,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary,
                                                )
                                            }
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
