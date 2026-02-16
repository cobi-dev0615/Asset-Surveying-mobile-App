package com.seretail.inventarios.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seretail.inventarios.ui.components.SERBottomBar
import com.seretail.inventarios.ui.activofijo.ActivoFijoCaptureScreen
import com.seretail.inventarios.ui.activofijo.ActivoFijoListScreen
import com.seretail.inventarios.ui.dashboard.DashboardScreen
import com.seretail.inventarios.ui.inventario.InventarioCaptureScreen
import com.seretail.inventarios.ui.inventario.InventarioListScreen
import com.seretail.inventarios.ui.login.LoginScreen
import com.seretail.inventarios.ui.scanner.BarcodeScannerScreen
import com.seretail.inventarios.ui.selection.EmpresaSucursalScreen
import com.seretail.inventarios.ui.settings.SettingsScreen

object Routes {
    const val LOGIN = "login"
    const val SELECT_EMPRESA = "select_empresa"
    const val DASHBOARD = "dashboard"
    const val INVENTARIO_LIST = "inventario/list"
    const val INVENTARIO_CAPTURE = "inventario/capture/{sessionId}"
    const val ACTIVOFIJO_LIST = "activofijo/list"
    const val ACTIVOFIJO_CAPTURE = "activofijo/capture/{sessionId}"
    const val SCANNER = "scanner/{mode}"
    const val SETTINGS = "settings"

    fun inventarioCapture(sessionId: Long) = "inventario/capture/$sessionId"
    fun activoFijoCapture(sessionId: Long) = "activofijo/capture/$sessionId"
    fun scanner(mode: String) = "scanner/$mode"
}

private val screensWithBottomBar = setOf(
    Routes.DASHBOARD,
    Routes.INVENTARIO_LIST,
    Routes.ACTIVOFIJO_LIST,
    Routes.SETTINGS,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in screensWithBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                SERBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.SELECT_EMPRESA) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.SELECT_EMPRESA) {
                EmpresaSucursalScreen(
                    onSelectionComplete = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SELECT_EMPRESA) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateToInventario = {
                        navController.navigate(Routes.INVENTARIO_LIST)
                    },
                    onNavigateToActivoFijo = {
                        navController.navigate(Routes.ACTIVOFIJO_LIST)
                    },
                )
            }

            composable(Routes.INVENTARIO_LIST) {
                InventarioListScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Routes.inventarioCapture(sessionId))
                    },
                )
            }

            composable(
                route = Routes.INVENTARIO_CAPTURE,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                InventarioCaptureScreen(
                    sessionId = sessionId,
                    onBackClick = { navController.popBackStack() },
                    onScanClick = {
                        navController.navigate(Routes.scanner("inventario"))
                    },
                )
            }

            composable(Routes.ACTIVOFIJO_LIST) {
                ActivoFijoListScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Routes.activoFijoCapture(sessionId))
                    },
                )
            }

            composable(
                route = Routes.ACTIVOFIJO_CAPTURE,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                ActivoFijoCaptureScreen(
                    sessionId = sessionId,
                    onBackClick = { navController.popBackStack() },
                    onScanClick = {
                        navController.navigate(Routes.scanner("activofijo"))
                    },
                )
            }

            composable(
                route = Routes.SCANNER,
                arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "inventario"
                BarcodeScannerScreen(
                    mode = mode,
                    onBarcodeScanned = { barcode ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("scanned_barcode", barcode)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
