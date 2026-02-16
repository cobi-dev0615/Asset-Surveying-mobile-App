package com.seretail.inventarios.navigation

import androidx.lifecycle.ViewModel
import com.seretail.inventarios.printing.BluetoothPrinterManager
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val printerManager: BluetoothPrinterManager,
    val preferencesManager: PreferencesManager,
) : ViewModel()
