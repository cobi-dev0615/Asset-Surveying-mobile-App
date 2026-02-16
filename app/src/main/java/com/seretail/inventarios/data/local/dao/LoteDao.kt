package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seretail.inventarios.data.local.entity.LoteEntity

@Dao
interface LoteDao {
    @Query("SELECT * FROM lotes WHERE codigo_barras = :codigoBarras ORDER BY lote")
    suspend fun getByBarcode(codigoBarras: String): List<LoteEntity>

    @Query("SELECT * FROM lotes WHERE empresa_id = :empresaId ORDER BY lote")
    suspend fun getByEmpresa(empresaId: Long): List<LoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lotes: List<LoteEntity>)

    @Query("DELETE FROM lotes WHERE empresa_id = :empresaId")
    suspend fun deleteByEmpresa(empresaId: Long)

    @Query("DELETE FROM lotes")
    suspend fun deleteAll()
}
