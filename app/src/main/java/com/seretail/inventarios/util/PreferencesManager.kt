package com.seretail.inventarios.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFERENCES_NAME,
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_AUTO_SYNC = booleanPreferencesKey("auto_sync")
        private val KEY_SYNC_WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
        private val KEY_LAST_SYNC = stringPreferencesKey("last_sync")
        private val KEY_USE_CAMERA = booleanPreferencesKey("use_camera")
        private val KEY_EMPRESA_ID = longPreferencesKey("empresa_id")
        private val KEY_EMPRESA_NOMBRE = stringPreferencesKey("empresa_nombre")
        private val KEY_SUCURSAL_ID = longPreferencesKey("sucursal_id")
        private val KEY_SUCURSAL_NOMBRE = stringPreferencesKey("sucursal_nombre")
    }

    val token: Flow<String?> = dataStore.data.map { it[KEY_TOKEN] }
    val userId: Flow<Long?> = dataStore.data.map { it[KEY_USER_ID] }
    val serverUrl: Flow<String> = dataStore.data.map { it[KEY_SERVER_URL] ?: Constants.DEFAULT_SERVER_URL }
    val autoSync: Flow<Boolean> = dataStore.data.map { it[KEY_AUTO_SYNC] ?: true }
    val syncWifiOnly: Flow<Boolean> = dataStore.data.map { it[KEY_SYNC_WIFI_ONLY] ?: false }
    val lastSync: Flow<String?> = dataStore.data.map { it[KEY_LAST_SYNC] }
    val useCamera: Flow<Boolean> = dataStore.data.map { it[KEY_USE_CAMERA] ?: true }
    val empresaId: Flow<Long?> = dataStore.data.map { it[KEY_EMPRESA_ID] }
    val empresaNombre: Flow<String?> = dataStore.data.map { it[KEY_EMPRESA_NOMBRE] }
    val sucursalId: Flow<Long?> = dataStore.data.map { it[KEY_SUCURSAL_ID] }
    val sucursalNombre: Flow<String?> = dataStore.data.map { it[KEY_SUCURSAL_NOMBRE] }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun saveUserId(userId: Long) {
        dataStore.edit { it[KEY_USER_ID] = userId }
    }

    suspend fun saveServerUrl(url: String) {
        dataStore.edit { it[KEY_SERVER_URL] = url }
    }

    suspend fun saveAutoSync(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_SYNC] = enabled }
    }

    suspend fun saveSyncWifiOnly(wifiOnly: Boolean) {
        dataStore.edit { it[KEY_SYNC_WIFI_ONLY] = wifiOnly }
    }

    suspend fun saveLastSync(timestamp: String) {
        dataStore.edit { it[KEY_LAST_SYNC] = timestamp }
    }

    suspend fun saveUseCamera(useCamera: Boolean) {
        dataStore.edit { it[KEY_USE_CAMERA] = useCamera }
    }

    suspend fun saveEmpresa(id: Long, nombre: String) {
        dataStore.edit {
            it[KEY_EMPRESA_ID] = id
            it[KEY_EMPRESA_NOMBRE] = nombre
        }
    }

    suspend fun saveSucursal(id: Long, nombre: String) {
        dataStore.edit {
            it[KEY_SUCURSAL_ID] = id
            it[KEY_SUCURSAL_NOMBRE] = nombre
        }
    }

    suspend fun clearSession() {
        dataStore.edit {
            it.remove(KEY_TOKEN)
            it.remove(KEY_USER_ID)
            it.remove(KEY_EMPRESA_ID)
            it.remove(KEY_EMPRESA_NOMBRE)
            it.remove(KEY_SUCURSAL_ID)
            it.remove(KEY_SUCURSAL_NOMBRE)
        }
    }
}
