package com.seretail.inventarios.data.repository

import com.seretail.inventarios.data.local.dao.ActivoFijoDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.RegistroDao
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.local.entity.NoEncontradoEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.data.local.entity.TraspasoEntity
import com.seretail.inventarios.data.remote.ApiService
import com.seretail.inventarios.data.remote.dto.ActivoFijoRegistroDto
import com.seretail.inventarios.data.remote.dto.ActivoFijoUploadRequest
import com.seretail.inventarios.data.remote.dto.NoEncontradoDto
import com.seretail.inventarios.data.remote.dto.NoEncontradoUploadRequest
import com.seretail.inventarios.data.remote.dto.TraspasoDto
import com.seretail.inventarios.data.remote.dto.TraspasoUploadRequest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivoFijoRepository @Inject constructor(
    private val apiService: ApiService,
    private val activoFijoDao: ActivoFijoDao,
    private val registroDao: RegistroDao,
    private val productoDao: ProductoDao,
) {
    fun observeSessions(): Flow<List<ActivoFijoSessionEntity>> = activoFijoDao.observeAll()

    fun observeRegistros(sessionId: Long): Flow<List<ActivoFijoRegistroEntity>> =
        registroDao.observeActivoFijoBySession(sessionId)

    suspend fun getSession(id: Long): ActivoFijoSessionEntity? = activoFijoDao.getById(id)

    suspend fun findProduct(barcode: String): ProductoEntity? =
        productoDao.findByBarcodeGlobal(barcode)

    suspend fun saveRegistro(registro: ActivoFijoRegistroEntity): Long {
        return registroDao.insertActivoFijo(registro)
    }

    suspend fun deleteRegistro(id: Long) {
        registroDao.deleteActivoFijo(id)
    }

    suspend fun saveNoEncontrado(noEncontrado: NoEncontradoEntity): Long {
        return registroDao.insertNoEncontrado(noEncontrado)
    }

    suspend fun saveTraspaso(traspaso: TraspasoEntity): Long {
        return registroDao.insertTraspaso(traspaso)
    }

    suspend fun countRegistros(sessionId: Long): Int =
        registroDao.countActivoFijoBySession(sessionId)

    suspend fun uploadPendingRegistros(): Result<Int> {
        val unsynced = registroDao.getUnsyncedActivoFijo()
        if (unsynced.isEmpty()) return Result.success(0)

        val grouped = unsynced.groupBy { it.sessionId }
        var totalUploaded = 0

        for ((sessionId, registros) in grouped) {
            try {
                val request = ActivoFijoUploadRequest(
                    inventarioId = sessionId,
                    registros = registros.map {
                        ActivoFijoRegistroDto(
                            codigoBarras = it.codigoBarras,
                            descripcion = it.descripcion,
                            categoria = it.categoria,
                            marca = it.marca,
                            modelo = it.modelo,
                            color = it.color,
                            serie = it.serie,
                            ubicacion = it.ubicacion,
                            statusId = it.statusId,
                            imagen1 = it.imagen1,
                            imagen2 = it.imagen2,
                            imagen3 = it.imagen3,
                            latitud = it.latitud,
                            longitud = it.longitud,
                            usuarioId = it.usuarioId,
                            fechaCaptura = it.fechaCaptura,
                        )
                    },
                )
                val response = apiService.uploadActivoFijo(request)
                if (response.isSuccessful) {
                    for (reg in registros) {
                        registroDao.updateActivoFijo(reg.copy(sincronizado = true))
                    }
                    totalUploaded += registros.size
                }
            } catch (_: Exception) {
                // Will retry on next sync
            }
        }
        return Result.success(totalUploaded)
    }

    suspend fun uploadPendingNoEncontrados(sessionId: Long): Result<Int> {
        val unsynced = registroDao.getUnsyncedNoEncontrados()
        if (unsynced.isEmpty()) return Result.success(0)

        try {
            val request = NoEncontradoUploadRequest(
                inventarioId = sessionId,
                noEncontrados = unsynced.map {
                    NoEncontradoDto(
                        activoId = it.activoId,
                        usuarioId = it.usuarioId,
                        latitud = it.latitud,
                        longitud = it.longitud,
                    )
                },
            )
            val response = apiService.uploadNoEncontrados(request)
            return if (response.isSuccessful) {
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Error al subir no encontrados"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun uploadPendingTraspasos(sessionId: Long): Result<Int> {
        val unsynced = registroDao.getUnsyncedTraspasos()
        if (unsynced.isEmpty()) return Result.success(0)

        try {
            val request = TraspasoUploadRequest(
                inventarioId = sessionId,
                traspasos = unsynced.map {
                    TraspasoDto(
                        registroId = it.registroId,
                        sucursalOrigenId = it.sucursalOrigenId,
                        sucursalDestinoId = it.sucursalDestinoId,
                    )
                },
            )
            val response = apiService.uploadTraspasos(request)
            return if (response.isSuccessful) {
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Error al subir traspasos"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun now(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
