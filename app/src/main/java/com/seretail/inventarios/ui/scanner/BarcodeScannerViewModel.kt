package com.seretail.inventarios.ui.scanner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class ScannerUiState(
    val isTorchOn: Boolean = false,
    val lastScannedCode: String? = null,
    val isProcessing: Boolean = false,
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(isTorchOn = !_uiState.value.isTorchOn)
    }

    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value.isProcessing) return
        _uiState.value = _uiState.value.copy(
            lastScannedCode = barcode,
            isProcessing = true,
        )
    }

    fun resetProcessing() {
        _uiState.value = _uiState.value.copy(isProcessing = false)
    }
}
