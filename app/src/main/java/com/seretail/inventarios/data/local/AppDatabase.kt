package com.seretail.inventarios.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seretail.inventarios.data.local.dao.ActivoFijoDao
import com.seretail.inventarios.data.local.dao.EmpresaDao
import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.LoteDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.dao.RfidTagDao
import com.seretail.inventarios.data.local.dao.SucursalDao
import com.seretail.inventarios.data.local.dao.SyncQueueDao
import com.seretail.inventarios.data.local.dao.UserDao
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.local.entity.EmpresaEntity
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.data.local.entity.LoteEntity
import com.seretail.inventarios.data.local.entity.NoEncontradoEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.data.local.entity.StatusEntity
import com.seretail.inventarios.data.local.entity.SucursalEntity
import com.seretail.inventarios.data.local.entity.SyncQueueEntity
import com.seretail.inventarios.data.local.entity.TraspasoEntity
import com.seretail.inventarios.data.local.entity.RfidTagEntity
import com.seretail.inventarios.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        EmpresaEntity::class,
        SucursalEntity::class,
        ProductoEntity::class,
        LoteEntity::class,
        InventarioEntity::class,
        ActivoFijoSessionEntity::class,
        ActivoFijoRegistroEntity::class,
        InventarioRegistroEntity::class,
        NoEncontradoEntity::class,
        TraspasoEntity::class,
        StatusEntity::class,
        SyncQueueEntity::class,
        RfidTagEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun sucursalDao(): SucursalDao
    abstract fun productoDao(): ProductoDao
    abstract fun loteDao(): LoteDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun activoFijoDao(): ActivoFijoDao
    abstract fun registroDao(): RegistroDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun rfidTagDao(): RfidTagDao
}
