package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.data.local.entity.NoEncontradoEntity
import com.seretail.inventarios.data.local.entity.TraspasoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    // Activo Fijo Registros
    @Query("SELECT * FROM activo_fijo_registros WHERE session_id = :sessionId ORDER BY id DESC")
    fun observeActivoFijoBySession(sessionId: Long): Flow<List<ActivoFijoRegistroEntity>>

    @Query("SELECT * FROM activo_fijo_registros WHERE session_id = :sessionId ORDER BY id DESC")
    suspend fun getActivoFijoBySession(sessionId: Long): List<ActivoFijoRegistroEntity>

    @Query("SELECT COUNT(*) FROM activo_fijo_registros WHERE session_id = :sessionId")
    suspend fun countActivoFijoBySession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM activo_fijo_registros WHERE sincronizado = 0")
    suspend fun countUnsyncedActivoFijo(): Int

    @Query("SELECT * FROM activo_fijo_registros WHERE sincronizado = 0")
    suspend fun getUnsyncedActivoFijo(): List<ActivoFijoRegistroEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivoFijo(registro: ActivoFijoRegistroEntity): Long

    @Update
    suspend fun updateActivoFijo(registro: ActivoFijoRegistroEntity)

    @Query("DELETE FROM activo_fijo_registros WHERE id = :id")
    suspend fun deleteActivoFijo(id: Long)

    // Inventario Registros
    @Query("SELECT * FROM inventario_registros WHERE session_id = :sessionId ORDER BY id DESC")
    fun observeInventarioBySession(sessionId: Long): Flow<List<InventarioRegistroEntity>>

    @Query("SELECT * FROM inventario_registros WHERE session_id = :sessionId ORDER BY id DESC")
    suspend fun getInventarioBySession(sessionId: Long): List<InventarioRegistroEntity>

    @Query("SELECT COUNT(*) FROM inventario_registros WHERE session_id = :sessionId")
    suspend fun countInventarioBySession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM inventario_registros WHERE sincronizado = 0")
    suspend fun countUnsyncedInventario(): Int

    @Query("SELECT * FROM inventario_registros WHERE sincronizado = 0")
    suspend fun getUnsyncedInventario(): List<InventarioRegistroEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventario(registro: InventarioRegistroEntity): Long

    @Update
    suspend fun updateInventario(registro: InventarioRegistroEntity)

    @Query("DELETE FROM inventario_registros WHERE id = :id")
    suspend fun deleteInventario(id: Long)

    @Query("SELECT * FROM inventario_registros WHERE codigo_barras LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%' ORDER BY id DESC")
    suspend fun searchInventarioRegistros(query: String): List<InventarioRegistroEntity>

    // No Encontrados
    @Query("SELECT * FROM no_encontrados WHERE session_id = :sessionId ORDER BY id DESC")
    fun observeNoEncontradosBySession(sessionId: Long): Flow<List<NoEncontradoEntity>>

    @Query("SELECT * FROM no_encontrados WHERE sincronizado = 0")
    suspend fun getUnsyncedNoEncontrados(): List<NoEncontradoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoEncontrado(noEncontrado: NoEncontradoEntity): Long

    // Traspasos
    @Query("SELECT * FROM traspasos WHERE sincronizado = 0")
    suspend fun getUnsyncedTraspasos(): List<TraspasoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraspaso(traspaso: TraspasoEntity): Long

    @Update
    suspend fun updateTraspaso(traspaso: TraspasoEntity)

    // Counts for dashboard
    @Query("SELECT COUNT(*) FROM activo_fijo_registros")
    suspend fun countAllActivoFijo(): Int

    @Query("SELECT COUNT(*) FROM inventario_registros")
    suspend fun countAllInventario(): Int

    @Query("SELECT (SELECT COUNT(*) FROM activo_fijo_registros WHERE sincronizado = 0) + (SELECT COUNT(*) FROM inventario_registros WHERE sincronizado = 0) + (SELECT COUNT(*) FROM no_encontrados WHERE sincronizado = 0) + (SELECT COUNT(*) FROM traspasos WHERE sincronizado = 0)")
    suspend fun countAllUnsynced(): Int

    // Activo Fijo status breakdown for dashboard
    @Query("SELECT COUNT(*) FROM activo_fijo_registros WHERE status_id = 1")
    suspend fun countActivoFijoFound(): Int

    @Query("SELECT COUNT(*) FROM activo_fijo_registros WHERE status_id = 2")
    suspend fun countActivoFijoNotFound(): Int

    @Query("SELECT COUNT(*) FROM activo_fijo_registros WHERE status_id = 3")
    suspend fun countActivoFijoAdded(): Int

    @Query("SELECT COUNT(*) FROM activo_fijo_registros WHERE status_id = 4")
    suspend fun countActivoFijoTransferred(): Int
}
