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
    @Json(name = "codigo_barras") val codigoBarras: String,
    val descripcion: String? = null,
    val categoria: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val color: String? = null,
    val serie: String? = null,
    val ubicacion: String? = null,
    @Json(name = "status_id") val statusId: Int = 1,
    val imagen1: String? = null,
    val imagen2: String? = null,
    val imagen3: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
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
    @Json(name = "codigo_barras") val codigoBarras: String,
    val descripcion: String? = null,
    val cantidad: Int = 1,
    val ubicacion: String? = null,
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
    @Json(name = "inventario_id") val inventarioId: Long,
    val traspasos: List<TraspasoDto>,
)

@JsonClass(generateAdapter = true)
data class TraspasoDto(
    @Json(name = "registro_id") val registroId: Long,
    @Json(name = "sucursal_origen_id") val sucursalOrigenId: Long,
    @Json(name = "sucursal_destino_id") val sucursalDestinoId: Long,
)

@JsonClass(generateAdapter = true)
data class UploadResponse(
    val message: String,
    val count: Int? = null,
)
