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
        private val KEY_PRINTER_MAC = stringPreferencesKey("printer_mac")
        private val KEY_PRINTER_NAME = stringPreferencesKey("printer_name")
        private val KEY_PRINTER_TYPE = stringPreferencesKey("printer_type")
        // Capture options
        private val KEY_VALIDATE_CATALOG = booleanPreferencesKey("validate_catalog")
        private val KEY_ALLOW_FORCED = booleanPreferencesKey("allow_forced_codes")
        private val KEY_CAPTURE_FACTOR = booleanPreferencesKey("capture_factor")
        private val KEY_CAPTURE_LOTES = booleanPreferencesKey("capture_lotes")
        private val KEY_CAPTURE_SERIAL = booleanPreferencesKey("capture_serial")
        private val KEY_CAPTURE_NEGATIVES = booleanPreferencesKey("capture_negatives")
        private val KEY_CAPTURE_ZEROS = booleanPreferencesKey("capture_zeros")
        private val KEY_CAPTURE_GPS = booleanPreferencesKey("capture_gps")
        private val KEY_CONTEO_UNIDAD = booleanPreferencesKey("conteo_unidad")
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
    val printerMac: Flow<String?> = dataStore.data.map { it[KEY_PRINTER_MAC] }
    val printerName: Flow<String?> = dataStore.data.map { it[KEY_PRINTER_NAME] }
    val printerType: Flow<String?> = dataStore.data.map { it[KEY_PRINTER_TYPE] }
    // Capture options
    val validateCatalog: Flow<Boolean> = dataStore.data.map { it[KEY_VALIDATE_CATALOG] ?: true }
    val allowForcedCodes: Flow<Boolean> = dataStore.data.map { it[KEY_ALLOW_FORCED] ?: false }
    val captureFactor: Flow<Boolean> = dataStore.data.map { it[KEY_CAPTURE_FACTOR] ?: false }
    val captureLotes: Flow<Boolean> = dataStore.data.map { it[KEY_CAPTURE_LOTES] ?: true }
    val captureSerial: Flow<Boolean> = dataStore.data.map { it[KEY_CAPTURE_SERIAL] ?: false }
    val captureNegatives: Flow<Boolean> = dataStore.data.map { it[KEY_CAPTURE_NEGATIVES] ?: false }
    val captureZeros: Flow<Boolean> = dataStore.data.map { it[KEY_CAPTURE_ZEROS] ?: false }
    val captureGps: Flow<Boolean> = dataStore.data.map { it[KEY_CAPTURE_GPS] ?: false }
    val conteoUnidad: Flow<Boolean> = dataStore.data.map { it[KEY_CONTEO_UNIDAD] ?: true }

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

    suspend fun savePrinter(mac: String, name: String, type: String) {
        dataStore.edit {
            it[KEY_PRINTER_MAC] = mac
            it[KEY_PRINTER_NAME] = name
            it[KEY_PRINTER_TYPE] = type
        }
    }

    // Capture option savers
    suspend fun saveValidateCatalog(v: Boolean) { dataStore.edit { it[KEY_VALIDATE_CATALOG] = v } }
    suspend fun saveAllowForcedCodes(v: Boolean) { dataStore.edit { it[KEY_ALLOW_FORCED] = v } }
    suspend fun saveCaptureFactor(v: Boolean) { dataStore.edit { it[KEY_CAPTURE_FACTOR] = v } }
    suspend fun saveCaptureLotes(v: Boolean) { dataStore.edit { it[KEY_CAPTURE_LOTES] = v } }
    suspend fun saveCaptureSerial(v: Boolean) { dataStore.edit { it[KEY_CAPTURE_SERIAL] = v } }
    suspend fun saveCaptureNegatives(v: Boolean) { dataStore.edit { it[KEY_CAPTURE_NEGATIVES] = v } }
    suspend fun saveCaptureZeros(v: Boolean) { dataStore.edit { it[KEY_CAPTURE_ZEROS] = v } }
    suspend fun saveCaptureGps(v: Boolean) { dataStore.edit { it[KEY_CAPTURE_GPS] = v } }
    suspend fun saveConteoUnidad(v: Boolean) { dataStore.edit { it[KEY_CONTEO_UNIDAD] = v } }

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
