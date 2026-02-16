package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivoFijoDao {
    @Query("SELECT * FROM activo_fijo_sessions ORDER BY id DESC")
    fun observeAll(): Flow<List<ActivoFijoSessionEntity>>

    @Query("SELECT * FROM activo_fijo_sessions WHERE empresa_id = :empresaId ORDER BY id DESC")
    fun observeByEmpresa(empresaId: Long): Flow<List<ActivoFijoSessionEntity>>

    @Query("SELECT * FROM activo_fijo_sessions WHERE id = :id")
    suspend fun getById(id: Long): ActivoFijoSessionEntity?

    @Query("SELECT COUNT(*) FROM activo_fijo_sessions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<ActivoFijoSessionEntity>)

    @Query("DELETE FROM activo_fijo_sessions")
    suspend fun deleteAll()
}
