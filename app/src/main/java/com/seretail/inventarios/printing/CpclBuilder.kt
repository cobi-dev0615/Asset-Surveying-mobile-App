package com.seretail.inventarios.printing

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * CPCL command builder for Zebra label printers.
 */
class CpclBuilder(
    private val labelWidth: Int = 200,
    private val labelHeight: Int = 200,
    private val maxHeight: Int = 210,
) {
    private val buffer = ByteArrayOutputStream()
    private val charset: Charset = Charset.forName("UTF-8")

    fun begin(): CpclBuilder {
        appendLine("! 0 $labelWidth $labelHeight $maxHeight 1")
        return this
    }

    fun text(font: Int = 7, x: Int = 0, y: Int = 0, text: String): CpclBuilder {
        appendLine("T $font 0 $x $y $text")
        return this
    }

    fun textLarge(x: Int = 0, y: Int = 0, text: String): CpclBuilder {
        return text(font = 4, x = x, y = y, text = text)
    }

    fun barcode(type: String = "128", x: Int = 0, y: Int = 0, height: Int = 60, data: String): CpclBuilder {
        appendLine("B $type 1 1 $height $x $y $data")
        return this
    }

    fun center(): CpclBuilder {
        appendLine("CENTER")
        return this
    }

    fun left(): CpclBuilder {
        appendLine("LEFT")
        return this
    }

    fun line(x0: Int, y0: Int, x1: Int, y1: Int, width: Int = 1): CpclBuilder {
        appendLine("LINE $x0 $y0 $x1 $y1 $width")
        return this
    }

    fun print(): CpclBuilder {
        appendLine("PRINT")
        return this
    }

    private fun appendLine(line: String) {
        buffer.write("$line\r\n".toByteArray(charset))
    }

    fun build(): ByteArray = buffer.toByteArray()
}
