package com.seretail.inventarios.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seretail.inventarios.ui.activofijo.ActivoFijoCaptureScreen
import com.seretail.inventarios.ui.activofijo.ActivoFijoListScreen
import com.seretail.inventarios.ui.components.SERBottomBar
import com.seretail.inventarios.ui.dashboard.DashboardScreen
import com.seretail.inventarios.ui.inventario.InventarioCaptureScreen
import com.seretail.inventarios.ui.inventario.InventarioListScreen
import com.seretail.inventarios.ui.login.LoginScreen
import com.seretail.inventarios.ui.rfid.RfidCaptureScreen
import com.seretail.inventarios.ui.scanner.BarcodeScannerScreen
import com.seretail.inventarios.ui.settings.SettingsScreen
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.SERBlue
import kotlinx.coroutines.flow.first

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val INVENTARIO_LIST = "inventario_list"
    const val INVENTARIO_CAPTURE = "inventario_capture/{sessionId}"
    const val ACTIVOFIJO_LIST = "activofijo_list"
    const val ACTIVOFIJO_CAPTURE = "activofijo_capture/{sessionId}"
    const val RFID_CAPTURE = "rfid_capture"
    const val SETTINGS = "settings"
    const val SCANNER = "scanner/{returnRoute}"

    fun inventarioCapture(sessionId: Long) = "inventario_capture/$sessionId"
    fun activoFijoCapture(sessionId: Long) = "activofijo_capture/$sessionId"
    fun scanner(returnRoute: String) = "scanner/$returnRoute"
}

private val bottomBarRoutes = setOf(
    Routes.DASHBOARD,
    Routes.INVENTARIO_LIST,
    Routes.ACTIVOFIJO_LIST,
    Routes.RFID_CAPTURE,
    Routes.SETTINGS,
)

@Composable
fun AppNavigation() {
    val navViewModel: NavigationViewModel = hiltViewModel()
    val printerManager = navViewModel.printerManager
    val preferencesManager = navViewModel.preferencesManager

    // 3-state auth: null = checking, true = logged in, false = logged out
    var authState by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val token = preferencesManager.token.first()
        authState = token != null
    }

    // Show loading while checking auth
    if (authState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = SERBlue, modifier = Modifier.size(40.dp))
        }
        return
    }

    val startDestination = if (authState == true) Routes.DASHBOARD else Routes.LOGIN
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                SERBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Routes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        },
        containerColor = DarkBackground,
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            // Login
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        authState = true
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                )
            }

            // Dashboard
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateToInventario = {
                        navController.navigate(Routes.INVENTARIO_LIST) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToActivoFijo = {
                        navController.navigate(Routes.ACTIVOFIJO_LIST) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            // Inventario List
            composable(Routes.INVENTARIO_LIST) {
                InventarioListScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Routes.inventarioCapture(sessionId))
                    },
                )
            }

            // Inventario Capture
            composable(
                route = Routes.INVENTARIO_CAPTURE,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                val scannedBarcode = backStackEntry.savedStateHandle.get<String>("scanned_barcode")
                val viewModel = hiltViewModel<com.seretail.inventarios.ui.inventario.InventarioCaptureViewModel>()

                LaunchedEffect(scannedBarcode) {
                    scannedBarcode?.let {
                        viewModel.onBarcodeScanned(it)
                        backStackEntry.savedStateHandle.remove<String>("scanned_barcode")
                    }
                }

                InventarioCaptureScreen(
                    sessionId = sessionId,
                    onBackClick = { navController.popBackStack() },
                    onScanBarcode = {
                        navController.navigate(Routes.scanner("inventario_capture/$sessionId"))
                    },
                    viewModel = viewModel,
                )
            }

            // Activo Fijo List
            composable(Routes.ACTIVOFIJO_LIST) {
                ActivoFijoListScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Routes.activoFijoCapture(sessionId))
                    },
                )
            }

            // Activo Fijo Capture
            composable(
                route = Routes.ACTIVOFIJO_CAPTURE,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                val scannedBarcode = backStackEntry.savedStateHandle.get<String>("scanned_barcode")
                val viewModel = hiltViewModel<com.seretail.inventarios.ui.activofijo.ActivoFijoCaptureViewModel>()

                LaunchedEffect(scannedBarcode) {
                    scannedBarcode?.let {
                        viewModel.onBarcodeScanned(it)
                        backStackEntry.savedStateHandle.remove<String>("scanned_barcode")
                    }
                }

                ActivoFijoCaptureScreen(
                    sessionId = sessionId,
                    onBackClick = { navController.popBackStack() },
                    onScanBarcode = {
                        navController.navigate(Routes.scanner("activofijo_capture/$sessionId"))
                    },
                    printerManager = printerManager,
                    viewModel = viewModel,
                )
            }

            // RFID Capture
            composable(Routes.RFID_CAPTURE) {
                RfidCaptureScreen(
                    onBackClick = { navController.popBackStack() },
                )
            }

            // Settings
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLoggedOut = {
                        authState = false
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    printerManager = printerManager,
                )
            }

            // Barcode Scanner (fullscreen overlay)
            composable(
                route = Routes.SCANNER,
                arguments = listOf(navArgument("returnRoute") { type = NavType.StringType }),
            ) {
                BarcodeScannerScreen(
                    onBarcodeScanned = { barcode ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("scanned_barcode", barcode)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    }
}
