package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activo_fijo_registros")
data class ActivoFijoRegistroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "server_id")
    val serverId: Long? = null,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "codigo_barras")
    val codigoBarras: String,
    val descripcion: String? = null,
    val categoria: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val color: String? = null,
    val serie: String? = null,
    val ubicacion: String? = null,
    @ColumnInfo(name = "status_id")
    val statusId: Int = 1,
    val imagen1: String? = null,
    val imagen2: String? = null,
    val imagen3: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val sincronizado: Boolean = false,
    @ColumnInfo(name = "fecha_captura")
    val fechaCaptura: String? = null,
    @ColumnInfo(name = "usuario_id")
    val usuarioId: Long? = null,
)
