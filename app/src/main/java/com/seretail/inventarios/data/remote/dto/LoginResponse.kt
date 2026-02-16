package com.seretail.inventarios.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: UserDto,
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long,
    val usuario: String,
    val nombres: String,
    val email: String? = null,
    @Json(name = "rol_id") val rolId: Int,
    val rol: RolDto? = null,
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
