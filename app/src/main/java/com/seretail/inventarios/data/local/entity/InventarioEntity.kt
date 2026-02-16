package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventarios")
data class InventarioEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "empresa_id")
    val empresaId: Long,
    @ColumnInfo(name = "sucursal_id")
    val sucursalId: Long,
    val nombre: String,
    val tipo: String? = null,
    val estado: String = "activo",
    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: String? = null,
    @ColumnInfo(name = "empresa_nombre")
    val empresaNombre: String? = null,
    @ColumnInfo(name = "sucursal_nombre")
    val sucursalNombre: String? = null,
)
