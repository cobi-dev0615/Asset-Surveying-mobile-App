package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seretail.inventarios.data.local.entity.EmpresaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresas WHERE eliminado = 0 ORDER BY nombre")
    fun observeAll(): Flow<List<EmpresaEntity>>

    @Query("SELECT * FROM empresas WHERE eliminado = 0 ORDER BY nombre")
    suspend fun getAll(): List<EmpresaEntity>

    @Query("SELECT * FROM empresas WHERE id = :id")
    suspend fun getById(id: Long): EmpresaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(empresas: List<EmpresaEntity>)

    @Query("DELETE FROM empresas")
    suspend fun deleteAll()
}
