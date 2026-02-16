package com.seretail.inventarios.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seretail.inventarios.data.repository.ActivoFijoRepository
import com.seretail.inventarios.data.repository.InventarioRepository
import com.seretail.inventarios.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val inventarioRepository: InventarioRepository,
    private val activoFijoRepository: ActivoFijoRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Upload pending data first
            inventarioRepository.uploadPendingRegistros()
            activoFijoRepository.uploadPendingRegistros()

            // Then download fresh data
            syncRepository.syncAll()

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
