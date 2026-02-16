package com.seretail.inventarios.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportInventario(
        context: Context,
        sessionName: String,
        registros: List<InventarioRegistroEntity>,
    ): Uri {
        val file = createFile(context, "inventario_$sessionName")
        FileWriter(file).use { writer ->
            writer.appendLine("Código,Descripción,Cantidad,Ubicación,Lote,Caducidad,Fecha Captura")
            registros.forEach { r ->
                writer.appendLine(
                    csvLine(r.codigoBarras, r.descripcion, r.cantidad.toString(),
                        r.ubicacion, r.lote, r.caducidad, r.fechaCaptura)
                )
            }
        }
        return getUri(context, file)
    }

    fun exportActivoFijo(
        context: Context,
        sessionName: String,
        registros: List<ActivoFijoRegistroEntity>,
    ): Uri {
        val file = createFile(context, "activo_fijo_$sessionName")
        FileWriter(file).use { writer ->
            writer.appendLine("Código,Descripción,Categoría,Marca,Modelo,Color,Serie,Ubicación,Status,Latitud,Longitud,Fecha Captura")
            registros.forEach { r ->
                writer.appendLine(
                    csvLine(r.codigoBarras, r.descripcion, r.categoria, r.marca, r.modelo,
                        r.color, r.serie, r.ubicacion, r.statusId.toString(),
                        r.latitud?.toString(), r.longitud?.toString(), r.fechaCaptura)
                )
            }
        }
        return getUri(context, file)
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String = "text/csv") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
    }

    private fun createFile(context: Context, prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = File(context.getExternalFilesDir(null), "Documents")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${prefix}_$timestamp.csv")
    }

    private fun getUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun csvLine(vararg fields: String?): String {
        return fields.joinToString(",") { escapeCsv(it ?: "") }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
