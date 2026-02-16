package com.seretail.inventarios.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    fun exportInventario(
        context: Context,
        sessionName: String,
        registros: List<InventarioRegistroEntity>,
    ): Uri {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Inventario")

        val headerRow = sheet.createRow(0)
        val headers = listOf("Código", "Descripción", "Cantidad", "Ubicación", "Lote", "Caducidad", "Fecha Captura")
        headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        registros.forEachIndexed { idx, r ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(r.codigoBarras)
            row.createCell(1).setCellValue(r.descripcion ?: "")
            row.createCell(2).setCellValue(r.cantidad.toDouble())
            row.createCell(3).setCellValue(r.ubicacion ?: "")
            row.createCell(4).setCellValue(r.lote ?: "")
            row.createCell(5).setCellValue(r.caducidad ?: "")
            row.createCell(6).setCellValue(r.fechaCaptura ?: "")
        }

        return saveWorkbook(context, workbook, "inventario_$sessionName")
    }

    fun exportActivoFijo(
        context: Context,
        sessionName: String,
        registros: List<ActivoFijoRegistroEntity>,
    ): Uri {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Activo Fijo")

        val headerRow = sheet.createRow(0)
        val headers = listOf("Código", "Descripción", "Categoría", "Marca", "Modelo", "Color",
            "Serie", "Ubicación", "Status", "Latitud", "Longitud", "Fecha Captura")
        headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        registros.forEachIndexed { idx, r ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(r.codigoBarras)
            row.createCell(1).setCellValue(r.descripcion ?: "")
            row.createCell(2).setCellValue(r.categoria ?: "")
            row.createCell(3).setCellValue(r.marca ?: "")
            row.createCell(4).setCellValue(r.modelo ?: "")
            row.createCell(5).setCellValue(r.color ?: "")
            row.createCell(6).setCellValue(r.serie ?: "")
            row.createCell(7).setCellValue(r.ubicacion ?: "")
            row.createCell(8).setCellValue(r.statusId.toDouble())
            row.createCell(9).setCellValue(r.latitud ?: 0.0)
            row.createCell(10).setCellValue(r.longitud ?: 0.0)
            row.createCell(11).setCellValue(r.fechaCaptura ?: "")
        }

        return saveWorkbook(context, workbook, "activo_fijo_$sessionName")
    }

    private fun saveWorkbook(context: Context, workbook: XSSFWorkbook, prefix: String): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = File(context.getExternalFilesDir(null), "Documents")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "${prefix}_$timestamp.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
