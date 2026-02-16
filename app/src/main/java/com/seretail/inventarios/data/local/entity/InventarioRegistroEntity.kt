package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_registros")
data class InventarioRegistroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "server_id")
    val serverId: Long? = null,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "codigo_barras")
    val codigoBarras: String,
    val descripcion: String? = null,
    val cantidad: Int = 1,
    val ubicacion: String? = null,
    val lote: String? = null,
    val caducidad: String? = null, // YYYY-MM-DD
    val factor: Int? = null, // Box multiplier (e.g., 48 pieces per box)
    @ColumnInfo(name = "numero_serie")
    val numeroSerie: String? = null,
    val sincronizado: Boolean = false,
    @ColumnInfo(name = "fecha_captura")
    val fechaCaptura: String? = null,
    @ColumnInfo(name = "usuario_id")
    val usuarioId: Long? = null,
)
