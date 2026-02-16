package com.seretail.inventarios.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoHelper {

    fun createImageFile(context: Context): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "SER_${timestamp}"
        val storageDir = File(context.filesDir, "fotos")
        if (!storageDir.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(fileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
    }
}
