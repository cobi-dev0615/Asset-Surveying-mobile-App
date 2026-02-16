package com.seretail.inventarios.di

import com.seretail.inventarios.data.local.AppDatabase
import com.seretail.inventarios.data.local.dao.RfidTagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RfidModule {

    @Provides
    fun provideRfidTagDao(db: AppDatabase): RfidTagDao = db.rfidTagDao()
}
