package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sucursales")
data class SucursalEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "empresa_id")
    val empresaId: Long,
    val nombre: String,
    val codigo: String? = null,
    val direccion: String? = null,
)
