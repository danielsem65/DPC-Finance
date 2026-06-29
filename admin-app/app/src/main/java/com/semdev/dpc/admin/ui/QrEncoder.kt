package com.semdev.dpc.admin.ui

import android.graphics.Bitmap
import android.graphics.Color

object QrEncoder {
    fun encode(text: String, size: Int = 512): Bitmap {
        val qrMatrix = generateQrMatrix(text)
        val scale = size / qrMatrix.size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (r in qrMatrix.indices) {
            for (c in qrMatrix[r].indices) {
                val color = if (qrMatrix[r][c]) Color.BLACK else Color.WHITE
                for (x in 0 until scale) {
                    for (y in 0 until scale) {
                        bitmap.setPixel(c * scale + x, r * scale + y, color)
                    }
                }
            }
        }
        return bitmap
    }

    private fun generateQrMatrix(text: String): Array<BooleanArray> {
        val size = 25
        val matrix = Array(size) { BooleanArray(size) }

        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, 0, size - 7)
        drawFinderPattern(matrix, size - 7, 0)

        for (i in 8 until size - 8) {
            matrix[6][i] = i % 2 == 0
            matrix[i][6] = i % 2 == 0
        }

        val dataBytes = text.toByteArray(Charsets.UTF_8)
        val bits = mutableListOf<Boolean>()
        for (b in dataBytes) {
            for (i in 7 downTo 0) {
                bits.add((b.toInt() shr i and 1) == 1)
            }
        }

        var bitIndex = 0
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (row < 8 && col < 8) continue
                if (row < 8 && col >= size - 8) continue
                if (row >= size - 8 && col < 8) continue
                if (row == 6 || col == 6) continue
                if (bitIndex < bits.size) {
                    matrix[row][col] = bits[bitIndex]
                    bitIndex++
                }
            }
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startRow: Int, startCol: Int) {
        for (r in startRow until startRow + 7) {
            for (c in startCol until startCol + 7) {
                val isBorder = r == startRow || r == startRow + 6 || c == startCol || c == startCol + 6
                val isInner = r >= startRow + 2 && r <= startRow + 4 && c >= startCol + 2 && c <= startCol + 4
                matrix[r][c] = isBorder || isInner
            }
        }
    }
}
