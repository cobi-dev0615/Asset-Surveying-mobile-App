package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val usuario: String,
    val nombres: String,
    val email: String? = null,
    @ColumnInfo(name = "rol_id")
    val rolId: Int,
    @ColumnInfo(name = "rol_nombre")
    val rolNombre: String? = null,
    @ColumnInfo(name = "empresa_ids")
    val empresaIds: String = "[]", // JSON array of empresa IDs
    @ColumnInfo(name = "acceso_app")
    val accesoApp: Boolean = true,
)
