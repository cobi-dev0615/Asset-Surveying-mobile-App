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
    @ColumnInfo(name = "codigo_2")
    val codigo2: String? = null,
    @ColumnInfo(name = "codigo_3")
    val codigo3: String? = null,
    @ColumnInfo(name = "precio_venta")
    val precioVenta: Double? = null,
    @ColumnInfo(name = "cantidad_teorica")
    val cantidadTeorica: Double? = null,
    @ColumnInfo(name = "unidad_medida")
    val unidadMedida: String? = null,
    val factor: Double? = null,
)
