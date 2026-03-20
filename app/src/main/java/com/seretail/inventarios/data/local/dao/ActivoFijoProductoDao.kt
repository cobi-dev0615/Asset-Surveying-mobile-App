package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seretail.inventarios.data.local.entity.ActivoFijoProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivoFijoProductoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ActivoFijoProductoEntity>)

    @Query("SELECT * FROM activo_fijo_productos WHERE inventarioId = :sessionId")
    fun observeBySession(sessionId: Long): Flow<List<ActivoFijoProductoEntity>>

    @Query("SELECT * FROM activo_fijo_productos WHERE inventarioId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<ActivoFijoProductoEntity>

    @Query("DELETE FROM activo_fijo_productos WHERE inventarioId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM activo_fijo_productos WHERE inventarioId = :sessionId")
    suspend fun countBySession(sessionId: Long): Int
}
