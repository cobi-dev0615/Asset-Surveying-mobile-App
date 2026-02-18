package com.seretail.inventarios.data.repository

import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.data.remote.ApiService
import com.seretail.inventarios.data.remote.dto.CreateSessionRequest
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

    suspend fun createSession(nombre: String, empresaId: Long, sucursalId: Long): Result<InventarioEntity> {
        return try {
            val response = apiService.createInventario(CreateSessionRequest(nombre, empresaId, sucursalId))
            if (response.isSuccessful) {
                val dto = response.body()!!
                val entity = InventarioEntity(
                    id = dto.id,
                    empresaId = dto.empresaId,
                    sucursalId = dto.sucursalId,
                    nombre = dto.nombre,
                    tipo = dto.tipo,
                    estado = dto.estado ?: "activo",
                    fechaCreacion = dto.createdAt,
                    empresaNombre = dto.empresa?.nombre,
                    sucursalNombre = dto.sucursal?.nombre,
                )
                inventarioDao.insert(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception("Error al crear sesión (${response.code()})"))
            }
        } catch (e: Exception) {
            // Create local-only session with negative ID
            val localId = -(System.currentTimeMillis() / 1000)
            val entity = InventarioEntity(
                id = localId,
                empresaId = empresaId,
                sucursalId = sucursalId,
                nombre = nombre,
                estado = "activo",
                fechaCreacion = now(),
            )
            inventarioDao.insert(entity)
            Result.success(entity)
        }
    }

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
                            lote = it.lote,
                            fechaCaducidad = it.caducidad,
                            factor = it.factor,
                            numeroSerie = it.numeroSerie,
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
