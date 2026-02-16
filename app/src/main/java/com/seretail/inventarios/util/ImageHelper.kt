package com.seretail.inventarios.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageHelper {
    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 75

    /**
     * Reads a local image URI, resizes to max 1024px, and returns base64 string.
     * Returns null if the URI can't be read.
     */
    fun readAsBase64(context: Context, uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }

            val stream2 = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(stream2, null, decodeOptions)
            stream2.close()
            bitmap ?: return null

            val scaled = scaleBitmap(bitmap)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
            if (scaled !== bitmap) scaled.recycle()
            bitmap.recycle()

            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / sample > MAX_DIMENSION * 2 || height / sample > MAX_DIMENSION * 2) {
            sample *= 2
        }
        return sample
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= MAX_DIMENSION && h <= MAX_DIMENSION) return bitmap
        val ratio = minOf(MAX_DIMENSION.toFloat() / w, MAX_DIMENSION.toFloat() / h)
        return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }
}
