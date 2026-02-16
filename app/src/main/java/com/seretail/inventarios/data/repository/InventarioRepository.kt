package com.seretail.inventarios.data.repository

import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.data.remote.ApiService
import com.seretail.inventarios.data.remote.dto.InventarioRegistroDto
import com.seretail.inventarios.data.remote.dto.InventarioUploadRequest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventarioRepository @Inject constructor(
    private val apiService: ApiService,
    private val inventarioDao: InventarioDao,
    private val registroDao: RegistroDao,
    private val productoDao: ProductoDao,
) {
    fun observeSessions(): Flow<List<InventarioEntity>> = inventarioDao.observeAll()

    fun observeRegistros(sessionId: Long): Flow<List<InventarioRegistroEntity>> =
        registroDao.observeInventarioBySession(sessionId)

    suspend fun getSession(id: Long): InventarioEntity? = inventarioDao.getById(id)

    suspend fun findProduct(barcode: String, empresaId: Long): ProductoEntity? =
        productoDao.findByBarcode(barcode, empresaId)

    suspend fun saveRegistro(registro: InventarioRegistroEntity): Long {
        return registroDao.insertInventario(registro)
    }

    suspend fun deleteRegistro(id: Long) {
        registroDao.deleteInventario(id)
    }

    suspend fun countRegistros(sessionId: Long): Int =
        registroDao.countInventarioBySession(sessionId)

    suspend fun uploadPendingRegistros(): Result<Int> {
        val unsynced = registroDao.getUnsyncedInventario()
        if (unsynced.isEmpty()) return Result.success(0)

        val grouped = unsynced.groupBy { it.sessionId }
        var totalUploaded = 0

        for ((sessionId, registros) in grouped) {
            try {
                val request = InventarioUploadRequest(
                    inventarioId = sessionId,
                    registros = registros.map {
                        InventarioRegistroDto(
                            codigoBarras = it.codigoBarras,
                            descripcion = it.descripcion,
                            cantidad = it.cantidad,
                            ubicacion = it.ubicacion,
                            usuarioId = it.usuarioId,
                            fechaCaptura = it.fechaCaptura,
                        )
                    },
                )
                val response = apiService.uploadInventario(request)
                if (response.isSuccessful) {
                    for (reg in registros) {
                        registroDao.updateInventario(reg.copy(sincronizado = true))
                    }
                    totalUploaded += registros.size
                }
            } catch (_: Exception) {
                // Will retry on next sync
            }
        }
        return Result.success(totalUploaded)
    }

    fun now(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
