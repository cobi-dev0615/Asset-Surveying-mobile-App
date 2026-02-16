package com.seretail.inventarios.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SucursalDto(
    val id: Long,
    @Json(name = "empresa_id") val empresaId: Long,
    val nombre: String,
    val codigo: String? = null,
    val direccion: String? = null,
)

@JsonClass(generateAdapter = true)
data class ProductoDto(
    val id: Long,
    @Json(name = "empresa_id") val empresaId: Long,
    @Json(name = "codigo_barras") val codigoBarras: String,
    val descripcion: String,
    val categoria: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val color: String? = null,
    val serie: String? = null,
    @Json(name = "sucursal_id") val sucursalId: Long? = null,
)

@JsonClass(generateAdapter = true)
data class LoteDto(
    val id: Long,
    @Json(name = "empresa_id") val empresaId: Long,
    @Json(name = "producto_id") val productoId: Long? = null,
    @Json(name = "codigo_barras") val codigoBarras: String? = null,
    val lote: String,
    val caducidad: String? = null,
    val existencia: Int? = null,
)

@JsonClass(generateAdapter = true)
data class StatusDto(
    val id: Int,
    val status: String,
    val nombre: String,
)

@JsonClass(generateAdapter = true)
data class InventarioSessionDto(
    val id: Long,
    @Json(name = "empresa_id") val empresaId: Long,
    @Json(name = "sucursal_id") val sucursalId: Long,
    val nombre: String,
    val tipo: String? = null,
    val estado: String? = "activo",
    @Json(name = "created_at") val createdAt: String? = null,
    val empresa: EmpresaDto? = null,
    val sucursal: SucursalDto? = null,
)

@JsonClass(generateAdapter = true)
data class ActivoFijoSessionDto(
    val id: Long,
    @Json(name = "empresa_id") val empresaId: Long,
    @Json(name = "sucursal_id") val sucursalId: Long,
    val nombre: String,
    val estado: String? = "activo",
    @Json(name = "created_at") val createdAt: String? = null,
    val empresa: EmpresaDto? = null,
    val sucursal: SucursalDto? = null,
)

@JsonClass(generateAdapter = true)
data class ActivoFijoProductoDto(
    val id: Long,
    @Json(name = "inventario_id") val inventarioId: Long,
    @Json(name = "empresa_id") val empresaId: Long,
    @Json(name = "codigo_1") val codigo1: String? = null,
    @Json(name = "codigo_2") val codigo2: String? = null,
    @Json(name = "codigo_3") val codigo3: String? = null,
    @Json(name = "tag_rfid") val tagRfid: String? = null,
    val descripcion: String? = null,
    @Json(name = "n_serie") val nSerie: String? = null,
    @Json(name = "categoria_1") val categoria1: String? = null,
    @Json(name = "categoria_2") val categoria2: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    @Json(name = "tipo_activo") val tipoActivo: String? = null,
)

@JsonClass(generateAdapter = true)
data class PaginatedResponse<T>(
    val data: List<T>,
    @Json(name = "current_page") val currentPage: Int? = null,
    @Json(name = "last_page") val lastPage: Int? = null,
    val total: Int? = null,
)
