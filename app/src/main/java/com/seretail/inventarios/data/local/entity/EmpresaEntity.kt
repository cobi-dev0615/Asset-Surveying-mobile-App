package com.seretail.inventarios.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empresas")
data class EmpresaEntity(
    @PrimaryKey
    val id: Long,
    val nombre: String,
    val codigo: String,
    val eliminado: Boolean = false,
)
