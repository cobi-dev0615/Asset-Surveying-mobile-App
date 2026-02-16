package com.seretail.inventarios.printing

import com.seretail.inventarios.data.local.entity.ActivoFijoRegistroEntity
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.local.entity.InventarioRegistroEntity

object PrintTemplates {

    fun buildAssetLabel(registro: ActivoFijoRegistroEntity): ByteArray {
        return CpclBuilder(labelWidth = 400, labelHeight = 200, maxHeight = 300)
            .begin()
            .center()
            .textLarge(x = 0, y = 10, text = "SER INVENTARIOS")
            .text(x = 0, y = 50, text = registro.descripcion ?: "Sin descripción")
            .barcode(x = 50, y = 80, height = 60, data = registro.codigoBarras)
            .text(x = 0, y = 170, text = registro.codigoBarras)
            .text(x = 0, y = 200, text = "Cat: ${registro.categoria ?: "N/A"}")
            .text(x = 0, y = 230, text = "Ubi: ${registro.ubicacion ?: "N/A"}")
            .print()
            .build()
    }

    fun buildInventoryTicket(
        session: String,
        registros: List<InventarioRegistroEntity>,
    ): ByteArray {
        val builder = EscPosBuilder()
            .init()
            .alignCenter()
            .bold(true)
            .doubleHeight(true)
            .textLine("SER INVENTARIOS")
            .doubleHeight(false)
            .bold(false)
            .textLine(session)
            .separator()
            .alignLeft()

        registros.take(50).forEach { reg ->
            builder
                .text("${reg.codigoBarras}  x${reg.cantidad}")
                .newLine()
            if (!reg.descripcion.isNullOrBlank()) {
                builder.textLine("  ${reg.descripcion}")
            }
        }

        builder
            .separator()
            .alignCenter()
            .bold(true)
            .textLine("Total: ${registros.size} registros")
            .bold(false)
            .feed()
            .cut()

        return builder.build()
    }

    fun buildSummaryReport(
        sessionName: String,
        total: Int,
        found: Int,
        notFound: Int,
        added: Int,
        transferred: Int,
    ): ByteArray {
        return EscPosBuilder()
            .init()
            .alignCenter()
            .bold(true)
            .doubleHeight(true)
            .textLine("RESUMEN ACTIVO FIJO")
            .doubleHeight(false)
            .bold(false)
            .textLine(sessionName)
            .separator()
            .alignLeft()
            .textLine("Encontrados:    $found")
            .textLine("No Encontrados: $notFound")
            .textLine("Agregados:      $added")
            .textLine("Traspasados:    $transferred")
            .separator()
            .bold(true)
            .textLine("TOTAL:          $total")
            .bold(false)
            .feed()
            .cut()
            .build()
    }
}
