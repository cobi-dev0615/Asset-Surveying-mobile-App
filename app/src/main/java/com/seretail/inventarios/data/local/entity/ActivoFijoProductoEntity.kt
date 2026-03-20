package com.seretail.inventarios.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activo_fijo_productos")
data class ActivoFijoProductoEntity(
    @PrimaryKey val id: Long = 0,
    val inventarioId: Long,
    val empresaId: Long,
    val codigo1: String? = null,
    val codigo2: String? = null,
    val codigo3: String? = null,
    val tagRfid: String? = null,
    val descripcion: String? = null,
    val nSerie: String? = null,
    val categoria1: String? = null,
    val categoria2: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val tipoActivo: String? = null,
)
