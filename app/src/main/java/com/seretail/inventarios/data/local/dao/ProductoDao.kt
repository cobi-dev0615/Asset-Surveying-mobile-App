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

    @Query("SELECT * FROM productos WHERE empresa_id = :empresaId ORDER BY descripcion LIMIT :limit")
    suspend fun getByEmpresaLimited(empresaId: Long, limit: Int): List<ProductoEntity>

    @Query("""
        SELECT COUNT(*) FROM productos
        WHERE empresa_id = :empresaId
        AND (codigo_barras = :code OR codigo_2 = :code OR codigo_3 = :code)
        AND id != :excludeId
    """)
    suspend fun countDuplicateCode(empresaId: Long, code: String, excludeId: Long = 0): Int

    @Query("SELECT * FROM productos WHERE empresa_id = :empresaId")
    suspend fun getAllByEmpresa(empresaId: Long): List<ProductoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: ProductoEntity): Long

    @Query("UPDATE productos SET codigo_barras = :codigoBarras, codigo_2 = :codigo2, codigo_3 = :codigo3, descripcion = :descripcion, categoria = :categoria, marca = :marca, modelo = :modelo, color = :color, serie = :serie, unidad_medida = :unidadMedida, precio_venta = :precioVenta, cantidad_teorica = :cantidadTeorica, factor = :factor WHERE id = :id")
    suspend fun update(id: Long, codigoBarras: String, codigo2: String?, codigo3: String?, descripcion: String, categoria: String?, marca: String?, modelo: String?, color: String?, serie: String?, unidadMedida: String?, precioVenta: Double?, cantidadTeorica: Double?, factor: Double?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoEntity>)

    @Query("DELETE FROM productos WHERE empresa_id = :empresaId")
    suspend fun deleteByEmpresa(empresaId: Long)

    @Query("DELETE FROM productos")
    suspend fun deleteAll()
}
