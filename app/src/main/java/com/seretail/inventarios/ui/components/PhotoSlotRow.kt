package com.seretail.inventarios.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seretail.inventarios.ui.theme.DarkBorder
import com.seretail.inventarios.ui.theme.DarkSurfaceVariant
import com.seretail.inventarios.ui.theme.Error
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted

@Composable
fun PhotoSlotRow(
    photo1: String?,
    photo2: String?,
    photo3: String?,
    onCapture: (slot: Int) -> Unit,
    onRemove: (slot: Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PhotoSlot(photo1, onCapture = { onCapture(1) }, onRemove = { onRemove(1) }, modifier = Modifier.weight(1f))
        PhotoSlot(photo2, onCapture = { onCapture(2) }, onRemove = { onRemove(2) }, modifier = Modifier.weight(1f))
        PhotoSlot(photo3, onCapture = { onCapture(3) }, onRemove = { onRemove(3) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PhotoSlot(
    photoUri: String?,
    onCapture: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable { onCapture() },
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Foto",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar foto",
                    tint = Error,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            Icon(
                Icons.Default.AddAPhoto,
                contentDescription = "Tomar foto",
                tint = TextMuted,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
