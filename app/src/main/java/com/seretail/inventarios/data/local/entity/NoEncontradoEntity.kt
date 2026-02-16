package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "no_encontrados")
data class NoEncontradoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "server_id")
    val serverId: Long? = null,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "activo_id")
    val activoId: String,
    @ColumnInfo(name = "usuario_id")
    val usuarioId: Long,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val sincronizado: Boolean = false,
    @ColumnInfo(name = "fecha_captura")
    val fechaCaptura: String? = null,
)
