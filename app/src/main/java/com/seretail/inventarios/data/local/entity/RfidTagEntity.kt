package com.seretail.inventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rfid_tags")
data class RfidTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epc: String,
    val rssi: Int = 0,
    @ColumnInfo(name = "read_count")
    val readCount: Int = 1,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    val timestamp: String,
    val matched: Boolean = false,
    @ColumnInfo(name = "matched_registro_id")
    val matchedRegistroId: Long? = null,
)
