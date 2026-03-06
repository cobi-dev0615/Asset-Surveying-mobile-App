package com.seretail.inventarios.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: LoginUserDto,
)

/**
 * User object as returned by the login endpoint.
 * The login endpoint returns `rol` as a string slug and `rol_nombre` as a string,
 * while the /me endpoint returns a full user structure with `rol_id`.
 */
@JsonClass(generateAdapter = true)
data class LoginUserDto(
    val id: Long,
    val usuario: String,
    val nombres: String,
    val email: String? = null,
    val rol: String? = null,
    @Json(name = "rol_nombre") val rolNombre: String? = null,
    val empresas: List<EmpresaDto>? = null,
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long,
    val usuario: String,
    val nombres: String,
    val email: String? = null,
    @Json(name = "rol_id") val rolId: Int? = null,
    val rol: String? = null,
    @Json(name = "rol_nombre") val rolNombre: String? = null,
    @Json(name = "acceso_app") val accesoApp: Boolean? = null,
    val empresas: List<EmpresaDto>? = null,
)

@JsonClass(generateAdapter = true)
data class RolDto(
    val id: Int,
    val nombre: String,
    val slug: String,
)

@JsonClass(generateAdapter = true)
data class EmpresaDto(
    val id: Long,
    val nombre: String,
    val codigo: String,
    val eliminado: Boolean? = false,
)
