package com.seretail.inventarios.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {

    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token,
            ).addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(Pair(location.latitude, location.longitude))
                } else {
                    cont.resume(null)
                }
            }.addOnFailureListener {
                cont.resume(null)
            }

            cont.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}
