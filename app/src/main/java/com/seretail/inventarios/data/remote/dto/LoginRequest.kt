package com.seretail.inventarios.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val usuario: String,
    val password: String,
)
