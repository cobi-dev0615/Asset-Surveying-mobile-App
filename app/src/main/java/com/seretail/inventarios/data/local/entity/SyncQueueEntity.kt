package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "registro_af", "registro_inv", "no_encontrado", "traspaso", "imagen"
    @ColumnInfo(name = "entity_id")
    val entityId: Long,
    val payload: String = "", // JSON payload for upload
    val status: String = "pending", // "pending", "uploading", "done", "error"
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
)
