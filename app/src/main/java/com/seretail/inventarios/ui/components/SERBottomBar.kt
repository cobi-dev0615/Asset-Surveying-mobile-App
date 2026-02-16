package com.seretail.inventarios.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
)

val bottomNavItems = listOf(
    BottomNavItem("dashboard", "Inicio", Icons.Default.Home),
    BottomNavItem("inventario_list", "Inventario", Icons.Default.Inventory2),
    BottomNavItem("activofijo_list", "Activo Fijo", Icons.Default.QrCodeScanner),
    BottomNavItem("rfid_capture", "RFID", Icons.Default.Nfc),
    BottomNavItem("settings", "Ajustes", Icons.Default.Settings),
)

@Composable
fun SERBottomBar(
    currentRoute: String?,
    pendingSyncCount: Int = 0,
    onItemClick: (String) -> Unit,
) {
    NavigationBar(containerColor = DarkSurface) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
                icon = {
                    if (item.route == "dashboard" && pendingSyncCount > 0) {
                        BadgedBox(badge = {
                            Badge { Text("$pendingSyncCount") }
                        }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SERBlue,
                    selectedTextColor = SERBlue,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = SERBlue.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
