package com.seretail.inventarios.data.repository

import com.seretail.inventarios.data.local.dao.UserDao
import com.seretail.inventarios.data.local.entity.UserEntity
import com.seretail.inventarios.data.remote.ApiService
import com.seretail.inventarios.data.remote.dto.LoginRequest
import com.seretail.inventarios.util.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager,
) {
    val currentUser: Flow<UserEntity?> = userDao.observeCurrentUser()
    val token: Flow<String?> = preferencesManager.token

    suspend fun login(usuario: String, password: String): Result<UserEntity> {
        return try {
            val response = apiService.login(LoginRequest(usuario, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                preferencesManager.saveToken(body.token)
                preferencesManager.saveUserId(body.user.id)

                val empresaIds = body.user.empresas?.map { it.id } ?: emptyList()
                val userEntity = UserEntity(
                    id = body.user.id,
                    usuario = body.user.usuario,
                    nombres = body.user.nombres,
                    email = body.user.email,
                    rolId = body.user.rolId,
                    rolNombre = body.user.rol?.nombre,
                    empresaIds = empresaIds.joinToString(","),
                    accesoApp = true,
                )
                userDao.deleteAll()
                userDao.insert(userEntity)
                Result.success(userEntity)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Usuario o contraseña incorrectos"
                    403 -> "No tienes acceso a la aplicación"
                    else -> "Error del servidor (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // Try offline login
            val cachedUser = userDao.getByUsuario(usuario)
            if (cachedUser != null) {
                Result.success(cachedUser)
            } else {
                Result.failure(Exception("Sin conexión al servidor"))
            }
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } catch (_: Exception) {
            // Ignore network errors on logout
        }
        preferencesManager.clearSession()
        userDao.deleteAll()
    }

    suspend fun isLoggedIn(): Boolean {
        val token = preferencesManager.token.first()
        return !token.isNullOrEmpty()
    }

    suspend fun getCurrentUser(): UserEntity? {
        val userId = preferencesManager.userId.first() ?: return null
        return userDao.getById(userId)
    }
}
