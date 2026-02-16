package com.seretail.inventarios.printing

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * ESC/POS command builder for receipt printers.
 */
class EscPosBuilder {
    private val buffer = ByteArrayOutputStream()
    private val charset: Charset = Charset.forName("CP437")

    fun init(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x40)) // ESC @
        return this
    }

    fun bold(on: Boolean): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x45, if (on) 1 else 0))
        return this
    }

    fun doubleHeight(on: Boolean): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x21, if (on) 0x10 else 0x00))
        return this
    }

    fun alignLeft(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x00))
        return this
    }

    fun alignCenter(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x01))
        return this
    }

    fun alignRight(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x02))
        return this
    }

    fun text(text: String): EscPosBuilder {
        buffer.write(text.toByteArray(charset))
        return this
    }

    fun newLine(): EscPosBuilder {
        buffer.write("\r\n".toByteArray(charset))
        return this
    }

    fun textLine(text: String): EscPosBuilder = text(text).newLine()

    fun separator(char: Char = '-', width: Int = 32): EscPosBuilder {
        return textLine(char.toString().repeat(width))
    }

    fun barcode128(data: String): EscPosBuilder {
        // CODE128 barcode
        buffer.write(byteArrayOf(0x1D, 0x68, 50))       // Set barcode height
        buffer.write(byteArrayOf(0x1D, 0x77, 2))         // Set barcode width
        buffer.write(byteArrayOf(0x1D, 0x48, 2))         // Print text below barcode
        buffer.write(byteArrayOf(0x1D, 0x6B, 73))        // CODE128 type
        buffer.write(byteArrayOf(data.length.toByte()))
        buffer.write(data.toByteArray(charset))
        return this
    }

    fun feed(lines: Int = 3): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x64, lines.toByte()))
        return this
    }

    fun cut(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1D, 0x56, 0x00))
        return this
    }

    fun build(): ByteArray = buffer.toByteArray()
}
