package com.seretail.inventarios.data.repository

import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RfidTagDao
import com.seretail.inventarios.data.local.entity.RfidTagEntity
import com.seretail.inventarios.rfid.RfidManager
import com.seretail.inventarios.rfid.RfidState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RfidRepository @Inject constructor(
    private val rfidManager: RfidManager,
    private val rfidTagDao: RfidTagDao,
    private val productoDao: ProductoDao,
) {
    val rfidState: StateFlow<RfidState> = rfidManager.state
    val incomingTags = rfidManager.tags

    fun observeTags(sessionId: Long): Flow<List<RfidTagEntity>> =
        rfidTagDao.observeBySession(sessionId)

    fun connect(serialPort: String = "/dev/ttyS4", baudRate: Int = 115200) {
        rfidManager.connect(serialPort, baudRate)
    }

    fun disconnect() = rfidManager.disconnect()
    fun startInventory() = rfidManager.startInventory()
    fun stopInventory() = rfidManager.stopInventory()
    fun setPower(power: Int) = rfidManager.setPower(power)
    fun getPower(): Int = rfidManager.getPower()

    suspend fun saveOrUpdateTag(sessionId: Long, epc: String, rssi: Int): RfidTagEntity {
        val existing = rfidTagDao.findByEpc(sessionId, epc)
        return if (existing != null) {
            val updated = existing.copy(
                readCount = existing.readCount + 1,
                rssi = rssi,
                timestamp = now(),
            )
            rfidTagDao.update(updated)
            updated
        } else {
            val tag = RfidTagEntity(
                epc = epc,
                rssi = rssi,
                sessionId = sessionId,
                timestamp = now(),
            )
            val id = rfidTagDao.insert(tag)
            tag.copy(id = id)
        }
    }

    suspend fun matchTagToAsset(tag: RfidTagEntity): Boolean {
        val product = productoDao.findByBarcodeGlobal(tag.epc)
        if (product != null) {
            rfidTagDao.update(tag.copy(matched = true, matchedRegistroId = product.id))
            return true
        }
        return false
    }

    suspend fun countTags(sessionId: Long): Int = rfidTagDao.countBySession(sessionId)
    suspend fun countMatched(sessionId: Long): Int = rfidTagDao.countMatchedBySession(sessionId)

    suspend fun clearSession(sessionId: Long) = rfidTagDao.deleteBySession(sessionId)

    private fun now(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
