package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seretail.inventarios.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos WHERE empresa_id = :empresaId ORDER BY descripcion")
    fun observeByEmpresa(empresaId: Long): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE codigo_barras = :codigoBarras AND empresa_id = :empresaId LIMIT 1")
    suspend fun findByBarcode(codigoBarras: String, empresaId: Long): ProductoEntity?

    @Query("SELECT * FROM productos WHERE codigo_barras = :codigoBarras LIMIT 1")
    suspend fun findByBarcodeGlobal(codigoBarras: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getById(id: Long): ProductoEntity?

    @Query("SELECT COUNT(*) FROM productos WHERE empresa_id = :empresaId")
    suspend fun countByEmpresa(empresaId: Long): Int

    @Query("SELECT * FROM productos WHERE empresa_id = :empresaId AND (descripcion LIKE '%' || :query || '%' OR codigo_barras LIKE '%' || :query || '%') ORDER BY descripcion LIMIT 100")
    suspend fun search(empresaId: Long, query: String): List<ProductoEntity>

    @Query("SELECT DISTINCT categoria FROM productos WHERE empresa_id = :empresaId AND categoria IS NOT NULL ORDER BY categoria")
    suspend fun getCategories(empresaId: Long): List<String>

    @Query("SELECT * FROM productos WHERE empresa_id = :empresaId AND categoria = :category ORDER BY descripcion")
    suspend fun getByCategory(empresaId: Long, category: String): List<ProductoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoEntity>)

    @Query("DELETE FROM productos WHERE empresa_id = :empresaId")
    suspend fun deleteByEmpresa(empresaId: Long)

    @Query("DELETE FROM productos")
    suspend fun deleteAll()
}
