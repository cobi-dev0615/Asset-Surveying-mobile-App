package com.seretail.inventarios.navigation

import androidx.lifecycle.ViewModel
import com.seretail.inventarios.data.local.dao.ActivoFijoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.repository.AuthRepository
import com.seretail.inventarios.printing.BluetoothPrinterManager
import com.seretail.inventarios.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val printerManager: BluetoothPrinterManager,
    val preferencesManager: PreferencesManager,
    val authRepository: AuthRepository,
    val registroDao: RegistroDao,
    val activoFijoDao: ActivoFijoDao,
) : ViewModel()
