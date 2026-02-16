package com.seretail.inventarios.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary
import com.seretail.inventarios.util.NetworkMonitor
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SERTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    isOnline: Boolean = true,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = TextPrimary)
                if (!isOnline) {
                    Text(
                        text = " Sin Conexión",
                        color = Error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Error.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = TextPrimary,
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
    )
}
