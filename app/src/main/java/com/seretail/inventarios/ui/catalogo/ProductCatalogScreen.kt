package com.seretail.inventarios.ui.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun ProductCatalogScreen(
    onBackClick: () -> Unit,
    onNewProductClick: () -> Unit = {},
    viewModel: ProductCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SERTopBar(
                title = "Catalogo de Productos",
                onBackClick = onBackClick,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewProductClick,
                containerColor = SERBlue,
                contentColor = TextPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo producto")
            }
        },
        containerColor = DarkBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Product count badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = SERBlue,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "%,d productos".format(state.productCount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }

            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::search,
                label = { Text("Buscar por codigo o descripcion", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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

            // Category filter chips
            if (state.categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategory == null && state.searchQuery.isEmpty() && state.products.isEmpty(),
                            onClick = viewModel::clearFilter,
                            label = { Text("Todos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SERBlue,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = DarkBorder,
                                selectedBorderColor = SERBlue,
                                enabled = true,
                                selected = state.selectedCategory == null && state.searchQuery.isEmpty() && state.products.isEmpty(),
                            ),
                        )
                    }
                    items(state.categories) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = {
                                Text(
                                    text = category,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SERBlue,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = DarkBorder,
                                selectedBorderColor = SERBlue,
                                enabled = true,
                                selected = state.selectedCategory == category,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SERBlue)
                }
                return@Scaffold
            }

            // Product list or empty state
            if (state.products.isEmpty() && (state.selectedCategory != null || state.searchQuery.isNotEmpty())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No hay productos en el catalogo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                        )
                    }
                }
            } else if (state.products.isEmpty()) {
                // Initial state - prompt user to search or select category
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Busca o selecciona una categoria",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductCard(product = product)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: com.seretail.inventarios.data.local.entity.ProductoEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Barcode (bold)
            Text(
                text = product.codigoBarras,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(Modifier.height(4.dp))

            // Description
            Text(
                text = product.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(6.dp))

            // Category and brand row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!product.categoria.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SERBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = product.categoria,
                            style = MaterialTheme.typography.labelSmall,
                            color = SERBlue,
                        )
                    }
                }
                if (!product.marca.isNullOrBlank()) {
                    Text(
                        text = product.marca,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}
