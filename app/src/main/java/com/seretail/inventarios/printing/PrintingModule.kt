package com.seretail.inventarios.printing

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PrintingModule {
    // BluetoothPrinterManager is @Singleton with @Inject constructor,
    // so Hilt provides it automatically. This module exists for future bindings.
}
