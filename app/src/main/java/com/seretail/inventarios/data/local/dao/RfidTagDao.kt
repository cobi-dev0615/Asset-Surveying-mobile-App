package com.seretail.inventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seretail.inventarios.data.local.entity.RfidTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RfidTagDao {
    @Query("SELECT * FROM rfid_tags WHERE session_id = :sessionId ORDER BY timestamp DESC")
    fun observeBySession(sessionId: Long): Flow<List<RfidTagEntity>>

    @Query("SELECT * FROM rfid_tags WHERE session_id = :sessionId AND epc = :epc LIMIT 1")
    suspend fun findByEpc(sessionId: Long, epc: String): RfidTagEntity?

    @Query("SELECT COUNT(*) FROM rfid_tags WHERE session_id = :sessionId")
    suspend fun countBySession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM rfid_tags WHERE session_id = :sessionId AND matched = 1")
    suspend fun countMatchedBySession(sessionId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: RfidTagEntity): Long

    @Update
    suspend fun update(tag: RfidTagEntity)

    @Query("DELETE FROM rfid_tags WHERE session_id = :sessionId")
    suspend fun deleteBySession(sessionId: Long)

    @Query("DELETE FROM rfid_tags")
    suspend fun deleteAll()
}
