package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seretail.inventarios.data.local.entity.SucursalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SucursalDao {
    @Query("SELECT * FROM sucursales WHERE empresa_id = :empresaId ORDER BY nombre")
    fun observeByEmpresa(empresaId: Long): Flow<List<SucursalEntity>>

    @Query("SELECT * FROM sucursales WHERE empresa_id = :empresaId ORDER BY nombre")
    suspend fun getByEmpresa(empresaId: Long): List<SucursalEntity>

    @Query("SELECT * FROM sucursales WHERE id = :id")
    suspend fun getById(id: Long): SucursalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sucursales: List<SucursalEntity>)

    @Query("DELETE FROM sucursales WHERE empresa_id = :empresaId")
    suspend fun deleteByEmpresa(empresaId: Long)

    @Query("DELETE FROM sucursales")
    suspend fun deleteAll()
}
