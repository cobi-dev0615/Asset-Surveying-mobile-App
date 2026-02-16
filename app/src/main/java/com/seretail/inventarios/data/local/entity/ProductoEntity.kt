package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "empresa_id")
    val empresaId: Long,
    @ColumnInfo(name = "codigo_barras")
    val codigoBarras: String,
    val descripcion: String,
    val categoria: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val color: String? = null,
    val serie: String? = null,
    @ColumnInfo(name = "sucursal_id")
    val sucursalId: Long? = null,
)
