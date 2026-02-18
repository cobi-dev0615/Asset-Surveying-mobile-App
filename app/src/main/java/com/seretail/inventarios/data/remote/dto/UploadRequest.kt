package com.seretail.inventarios.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActivoFijoUploadRequest(
    @Json(name = "inventario_id") val inventarioId: Long,
    val registros: List<ActivoFijoRegistroDto>,
)

@JsonClass(generateAdapter = true)
data class ActivoFijoRegistroDto(
    @Json(name = "codigo_1") val codigoBarras: String,
    val descripcion: String? = null,
    val categoria: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val color: String? = null,
    @Json(name = "n_serie") val serie: String? = null,
    @Json(name = "ubicacion_1") val ubicacion: String? = null,
    val observaciones: String? = null,
    @Json(name = "n_serie_nuevo") val nSerieNuevo: String? = null,
    @Json(name = "codigo_1_anterior") val codigo1Anterior: String? = null,
    @Json(name = "status_id") val statusId: Int = 1,
    val imagen1: String? = null,
    val imagen2: String? = null,
    val imagen3: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    @Json(name = "tag_rfid") val tagRfid: String? = null,
    val traspasado: Boolean = false,
    @Json(name = "sucursal_origen") val sucursalOrigen: String? = null,
    val forzado: Boolean = false,
    @Json(name = "version_app") val versionApp: String? = null,
    @Json(name = "usuario_id") val usuarioId: Long? = null,
    @Json(name = "fecha_captura") val fechaCaptura: String? = null,
)

@JsonClass(generateAdapter = true)
data class InventarioUploadRequest(
    @Json(name = "inventario_id") val inventarioId: Long,
    val registros: List<InventarioRegistroDto>,
)

@JsonClass(generateAdapter = true)
data class InventarioRegistroDto(
    @Json(name = "codigo_1") val codigoBarras: String,
    val descripcion: String? = null,
    val cantidad: Int = 1,
    @Json(name = "ubicacion_1") val ubicacion: String? = null,
    val lote: String? = null,
    @Json(name = "fecha_caducidad") val fechaCaducidad: String? = null,
    val factor: Int? = null,
    @Json(name = "numero_serie") val numeroSerie: String? = null,
    val forzado: Boolean = false,
    @Json(name = "usuario_id") val usuarioId: Long? = null,
    @Json(name = "fecha_captura") val fechaCaptura: String? = null,
)

@JsonClass(generateAdapter = true)
data class NoEncontradoUploadRequest(
    @Json(name = "inventario_id") val inventarioId: Long,
    @Json(name = "no_encontrados") val noEncontrados: List<NoEncontradoDto>,
)

@JsonClass(generateAdapter = true)
data class NoEncontradoDto(
    @Json(name = "activo_id") val activoId: String,
    @Json(name = "usuario_id") val usuarioId: Long,
    val latitud: Double? = null,
    val longitud: Double? = null,
)

@JsonClass(generateAdapter = true)
data class TraspasoUploadRequest(
    val traspasos: List<TraspasoDto>,
)

@JsonClass(generateAdapter = true)
data class TraspasoDto(
    @Json(name = "activo") val registroId: Long,
    @Json(name = "sucursal_origen_id") val sucursalOrigenId: Long,
    @Json(name = "sucursal_destino_id") val sucursalDestinoId: Long,
)

@JsonClass(generateAdapter = true)
data class CreateSessionRequest(
    val nombre: String,
    @Json(name = "empresa_id") val empresaId: Long,
    @Json(name = "sucursal_id") val sucursalId: Long,
)

@JsonClass(generateAdapter = true)
data class RfidTagUploadRequest(
    @Json(name = "session_id") val sessionId: Long,
    val tags: List<RfidTagDto>,
)

@JsonClass(generateAdapter = true)
data class RfidTagDto(
    val epc: String,
    val rssi: Int,
    @Json(name = "read_count") val readCount: Int,
    val matched: Boolean,
    @Json(name = "matched_registro_id") val matchedRegistroId: Long? = null,
    val timestamp: String? = null,
)

@JsonClass(generateAdapter = true)
data class UploadResponse(
    val message: String,
    val count: Int? = null,
    val id: Long? = null,
)
