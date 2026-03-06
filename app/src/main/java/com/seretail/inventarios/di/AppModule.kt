package com.seretail.inventarios.di

import android.content.Context
import androidx.room.Room
import com.seretail.inventarios.data.local.AppDatabase
import com.seretail.inventarios.data.local.dao.ActivoFijoDao
import com.seretail.inventarios.data.local.dao.EmpresaDao
import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.LoteDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.dao.SucursalDao
import com.seretail.inventarios.data.local.dao.SyncQueueDao
import com.seretail.inventarios.data.local.dao.UserDao
import com.seretail.inventarios.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideEmpresaDao(db: AppDatabase): EmpresaDao = db.empresaDao()

    @Provides
    fun provideSucursalDao(db: AppDatabase): SucursalDao = db.sucursalDao()

    @Provides
    fun provideProductoDao(db: AppDatabase): ProductoDao = db.productoDao()

    @Provides
    fun provideLoteDao(db: AppDatabase): LoteDao = db.loteDao()

    @Provides
    fun provideInventarioDao(db: AppDatabase): InventarioDao = db.inventarioDao()

    @Provides
    fun provideActivoFijoDao(db: AppDatabase): ActivoFijoDao = db.activoFijoDao()

    @Provides
    fun provideRegistroDao(db: AppDatabase): RegistroDao = db.registroDao()

    @Provides
    fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
}
