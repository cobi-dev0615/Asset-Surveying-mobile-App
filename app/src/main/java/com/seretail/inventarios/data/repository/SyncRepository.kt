package com.seretail.inventarios.data.repository

import com.seretail.inventarios.data.local.dao.ActivoFijoDao
import com.seretail.inventarios.data.local.dao.EmpresaDao
import com.seretail.inventarios.data.local.dao.InventarioDao
import com.seretail.inventarios.data.local.dao.LoteDao
import com.seretail.inventarios.data.local.dao.ProductoDao
import com.seretail.inventarios.data.local.dao.SucursalDao
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.local.entity.EmpresaEntity
import com.seretail.inventarios.data.local.entity.InventarioEntity
import com.seretail.inventarios.data.local.entity.LoteEntity
import com.seretail.inventarios.data.local.entity.ProductoEntity
import com.seretail.inventarios.data.local.entity.SucursalEntity
import com.seretail.inventarios.data.remote.ApiService
import com.seretail.inventarios.util.PreferencesManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val apiService: ApiService,
    private val empresaDao: EmpresaDao,
    private val sucursalDao: SucursalDao,
    private val productoDao: ProductoDao,
    private val loteDao: LoteDao,
    private val inventarioDao: InventarioDao,
    private val activoFijoDao: ActivoFijoDao,
    private val preferencesManager: PreferencesManager,
) {
    suspend fun syncEmpresas(): Result<Int> {
        return try {
            val response = apiService.getEmpresas()
            if (response.isSuccessful) {
                val empresas = response.body()!!.map {
                    EmpresaEntity(
                        id = it.id,
                        nombre = it.nombre,
                        codigo = it.codigo,
                        eliminado = it.eliminado ?: false,
                    )
                }
                empresaDao.insertAll(empresas)
                Result.success(empresas.size)
            } else {
                Result.failure(Exception("Error al sincronizar empresas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncSucursales(empresaId: Long): Result<Int> {
        return try {
            val response = apiService.getSucursales(empresaId)
            if (response.isSuccessful) {
                val sucursales = response.body()!!.map {
                    SucursalEntity(
                        id = it.id,
                        empresaId = it.empresaId,
                        nombre = it.nombre,
                        codigo = it.codigo,
                        direccion = it.direccion,
                    )
                }
                sucursalDao.deleteByEmpresa(empresaId)
                sucursalDao.insertAll(sucursales)
                Result.success(sucursales.size)
            } else {
                Result.failure(Exception("Error al sincronizar sucursales"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncProductos(empresaId: Long): Result<Int> {
        return try {
            var page = 1
            var totalInserted = 0
            productoDao.deleteByEmpresa(empresaId)

            while (true) {
                val response = apiService.getProductos(empresaId, page)
                if (!response.isSuccessful) break

                val body = response.body() ?: break
                val productos = body.data.map {
                    ProductoEntity(
                        id = it.id,
                        empresaId = it.empresaId,
                        codigoBarras = it.codigoBarras,
                        descripcion = it.descripcion,
                        categoria = it.categoria,
                        marca = it.marca,
                        modelo = it.modelo,
                        color = it.color,
                        serie = it.serie,
                        sucursalId = it.sucursalId,
                        codigo2 = it.codigo2,
                        codigo3 = it.codigo3,
                        precioVenta = it.precioVenta,
                        cantidadTeorica = it.cantidadTeorica,
                        unidadMedida = it.unidadMedida,
                        factor = it.factor,
                    )
                }
                productoDao.insertAll(productos)
                totalInserted += productos.size

                if (page >= (body.lastPage ?: 1)) break
                page++
            }
            Result.success(totalInserted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncLotes(empresaId: Long): Result<Int> {
        return try {
            val response = apiService.getLotes(empresaId)
            if (response.isSuccessful) {
                val body = response.body()!!
                val lotes = body.data.map {
                    LoteEntity(
                        id = it.id,
                        empresaId = it.empresaId,
                        productoId = null,
                        codigoBarras = it.sku,
                        lote = it.lote,
                        caducidad = it.fechaCaducidad,
                        existencia = it.cantidad?.toInt(),
                    )
                }
                loteDao.deleteByEmpresa(empresaId)
                loteDao.insertAll(lotes)
                Result.success(lotes.size)
            } else {
                Result.failure(Exception("Error al sincronizar lotes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncInventarioSessions(): Result<Int> {
        return try {
            val response = apiService.getInventarios()
            if (response.isSuccessful) {
                val sessions = response.body()!!.map {
                    val estado = when {
                        it.finalizado == true -> "finalizado"
                        else -> it.status?.nombre ?: "activo"
                    }
                    InventarioEntity(
                        id = it.id,
                        empresaId = it.empresaId,
                        sucursalId = it.sucursalId,
                        nombre = it.nombre,
                        tipo = null,
                        estado = estado,
                        fechaCreacion = it.createdAt,
                        empresaNombre = it.empresa?.nombre,
                        sucursalNombre = it.sucursal?.nombre,
                    )
                }
                // Use insertAll with REPLACE strategy (defined in DAO) instead of deleteAll + insertAll
                // to avoid race condition where a session disappears while user navigates to it
                inventarioDao.insertAll(sessions)
                Result.success(sessions.size)
            } else {
                Result.failure(Exception("Error al sincronizar inventarios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncActivoFijoSessions(): Result<Int> {
        return try {
            val response = apiService.getActivoFijoSessions()
            if (response.isSuccessful) {
                val sessions = response.body()!!.map {
                    val estado = when {
                        it.finalizado == true -> "finalizado"
                        else -> it.status?.nombre ?: "activo"
                    }
                    ActivoFijoSessionEntity(
                        id = it.id,
                        empresaId = it.empresaId,
                        sucursalId = it.sucursalId,
                        nombre = it.nombre,
                        estado = estado,
                        fechaCreacion = it.createdAt,
                        empresaNombre = it.empresa?.nombre,
                        sucursalNombre = it.sucursal?.nombre,
                    )
                }
                // Use REPLACE instead of deleteAll to avoid race condition
                activoFijoDao.insertAll(sessions)
                Result.success(sessions.size)
            } else {
                Result.failure(Exception("Error al sincronizar activo fijo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAll(): Result<Unit> {
        return try {
            syncEmpresas()

            val empresas = empresaDao.getAll()
            for (empresa in empresas) {
                syncSucursales(empresa.id)
                syncProductos(empresa.id)
                syncLotes(empresa.id)
            }
            syncInventarioSessions()
            syncActivoFijoSessions()

            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            preferencesManager.saveLastSync(now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
