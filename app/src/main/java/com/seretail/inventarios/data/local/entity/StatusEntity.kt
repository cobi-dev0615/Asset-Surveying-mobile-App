package com.seretail.inventarios.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey
    val id: Int,
    val status: String,
    val nombre: String,
)
