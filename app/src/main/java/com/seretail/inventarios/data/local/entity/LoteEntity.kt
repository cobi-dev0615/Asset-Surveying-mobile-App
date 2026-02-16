package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotes")
data class LoteEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "empresa_id")
    val empresaId: Long,
    @ColumnInfo(name = "producto_id")
    val productoId: Long? = null,
    @ColumnInfo(name = "codigo_barras")
    val codigoBarras: String? = null,
    val lote: String,
    val caducidad: String? = null, // YYYY-MM-DD
    val existencia: Int? = null,
)
