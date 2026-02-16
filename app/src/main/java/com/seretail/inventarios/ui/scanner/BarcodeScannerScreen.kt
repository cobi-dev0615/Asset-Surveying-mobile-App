package com.seretail.inventarios.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextPrimary
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        CameraPreviewContent(onBarcodeScanned = onBarcodeScanned, onBackClick = onBackClick)
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text("Se requiere permiso de cámara para escanear", color = TextPrimary)
        }
    }
}

@Composable
private fun CameraPreviewContent(
    onBarcodeScanned: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var torchOn by remember { mutableStateOf(false) }
    var lastScanned by remember { mutableStateOf("") }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    val options = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_ITF,
            )
            .build()
    }
    val scanner = remember { BarcodeScanning.getClient(options) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                @androidx.camera.core.ExperimentalGetImage
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees,
                    )
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            val barcode = barcodes.firstOrNull()?.rawValue
                            if (barcode != null && barcode != lastScanned) {
                                lastScanned = barcode
                                onBarcodeScanned(barcode)
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            executor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        // Scan overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scanBoxWidth = size.width * 0.75f
            val scanBoxHeight = scanBoxWidth * 0.5f
            val left = (size.width - scanBoxWidth) / 2
            val top = (size.height - scanBoxHeight) / 2

            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(left, top, left + scanBoxWidth, top + scanBoxHeight),
                        CornerRadius(16.dp.toPx()),
                    )
                )
            }
            clipPath(path, ClipOp.Difference) {
                drawRect(Color.Black.copy(alpha = 0.6f))
            }
            drawRoundRect(
                color = Color(0xFF0D6EFD),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(scanBoxWidth, scanBoxHeight),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 3.dp.toPx()),
            )
        }

        // Mode label
        Text(
            text = "Escanea un código de barras",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }

        // Torch toggle
        FloatingActionButton(
            onClick = {
                torchOn = !torchOn
                camera?.cameraControl?.enableTorch(torchOn)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            containerColor = SERBlue,
        ) {
            Icon(
                if (torchOn) Icons.Default.FlashOff else Icons.Default.FlashOn,
                contentDescription = if (torchOn) "Apagar linterna" else "Encender linterna",
                tint = Color.White,
            )
        }
    }
}
