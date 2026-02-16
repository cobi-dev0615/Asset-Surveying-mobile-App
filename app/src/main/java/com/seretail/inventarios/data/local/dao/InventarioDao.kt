package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seretail.inventarios.data.local.entity.InventarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {
    @Query("SELECT * FROM inventarios ORDER BY id DESC")
    fun observeAll(): Flow<List<InventarioEntity>>

    @Query("SELECT * FROM inventarios WHERE empresa_id = :empresaId ORDER BY id DESC")
    fun observeByEmpresa(empresaId: Long): Flow<List<InventarioEntity>>

    @Query("SELECT * FROM inventarios WHERE id = :id")
    suspend fun getById(id: Long): InventarioEntity?

    @Query("SELECT COUNT(*) FROM inventarios")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventario: InventarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inventarios: List<InventarioEntity>)

    @Query("DELETE FROM inventarios")
    suspend fun deleteAll()
}
