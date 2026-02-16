package com.seretail.inventarios.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.StatusFound
import com.seretail.inventarios.ui.theme.StatusNotFound
import com.seretail.inventarios.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SERTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    isOnline: Boolean = true,
    isSyncing: Boolean = false,
    onSyncClick: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = TextPrimary,
                    )
                }
            }
        },
        actions = {
            if (onSyncClick != null) {
                IconButton(onClick = onSyncClick) {
                    Icon(
                        imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.Sync,
                        contentDescription = "Sincronizar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Icon(
                imageVector = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                contentDescription = if (isOnline) "Conectado" else "Sin conexión",
                tint = if (isOnline) StatusFound else StatusNotFound,
                modifier = Modifier.size(18.dp),
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = DarkSurface,
        ),
    )
}
