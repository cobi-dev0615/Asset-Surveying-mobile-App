package com.seretail.inventarios.export

import android.content.Context
import android.net.Uri
import com.seretail.inventarios.data.local.entity.ProductoEntity
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStreamReader

object CatalogImporter {

    data class ImportResult(
        val products: List<ProductoEntity>,
        val errors: List<String>,
    )

    fun importFromUri(context: Context, uri: Uri, empresaId: Long): ImportResult {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        return when {
            mimeType.contains("csv") || uri.path?.endsWith(".csv") == true ->
                importCsv(context, uri, empresaId)
            else -> importExcel(context, uri, empresaId)
        }
    }

    private fun importCsv(context: Context, uri: Uri, empresaId: Long): ImportResult {
        val products = mutableListOf<ProductoEntity>()
        val errors = mutableListOf<String>()

        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                val header = reader.readLine() ?: return ImportResult(emptyList(), listOf("Archivo vacío"))
                var lineNum = 1

                reader.forEachLine { line ->
                    lineNum++
                    try {
                        val cols = parseCsvLine(line)
                        if (cols.isNotEmpty() && cols[0].isNotBlank()) {
                            products.add(
                                ProductoEntity(
                                    empresaId = empresaId,
                                    codigoBarras = cols[0],
                                    descripcion = cols.getOrNull(1) ?: "",
                                    categoria = cols.getOrNull(2)?.ifBlank { null },
                                    marca = cols.getOrNull(3)?.ifBlank { null },
                                    modelo = cols.getOrNull(4)?.ifBlank { null },
                                    color = cols.getOrNull(5)?.ifBlank { null },
                                    serie = cols.getOrNull(6)?.ifBlank { null },
                                )
                            )
                        }
                    } catch (e: Exception) {
                        errors.add("Línea $lineNum: ${e.message}")
                    }
                }
            }
        }

        return ImportResult(products, errors)
    }

    private fun importExcel(context: Context, uri: Uri, empresaId: Long): ImportResult {
        val products = mutableListOf<ProductoEntity>()
        val errors = mutableListOf<String>()

        context.contentResolver.openInputStream(uri)?.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            val sheet = workbook.getSheetAt(0)

            for (rowIdx in 1..sheet.lastRowNum) {
                try {
                    val row = sheet.getRow(rowIdx) ?: continue
                    val codigo = row.getCell(0)?.toString()?.trim() ?: continue
                    if (codigo.isBlank()) continue

                    products.add(
                        ProductoEntity(
                            empresaId = empresaId,
                            codigoBarras = codigo,
                            descripcion = row.getCell(1)?.toString()?.trim() ?: "",
                            categoria = row.getCell(2)?.toString()?.trim()?.ifBlank { null },
                            marca = row.getCell(3)?.toString()?.trim()?.ifBlank { null },
                            modelo = row.getCell(4)?.toString()?.trim()?.ifBlank { null },
                            color = row.getCell(5)?.toString()?.trim()?.ifBlank { null },
                            serie = row.getCell(6)?.toString()?.trim()?.ifBlank { null },
                        )
                    )
                } catch (e: Exception) {
                    errors.add("Fila ${rowIdx + 1}: ${e.message}")
                }
            }
            workbook.close()
        }

        return ImportResult(products, errors)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
