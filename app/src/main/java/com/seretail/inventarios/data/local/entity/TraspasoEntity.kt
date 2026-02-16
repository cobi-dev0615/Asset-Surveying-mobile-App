package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traspasos")
data class TraspasoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "server_id")
    val serverId: Long? = null,
    @ColumnInfo(name = "registro_id")
    val registroId: Long,
    @ColumnInfo(name = "sucursal_origen_id")
    val sucursalOrigenId: Long,
    @ColumnInfo(name = "sucursal_destino_id")
    val sucursalDestinoId: Long,
    val sincronizado: Boolean = false,
    @ColumnInfo(name = "fecha_captura")
    val fechaCaptura: String? = null,
)
