package com.wmrmobile.app

import kotlin.math.exp
import kotlin.math.sqrt

/** Pure geometry for the localized smile mesh; kept JVM-testable. */
internal object SmileWarpMath {
    fun buildVertices(
        width: Int,
        height: Int,
        leftX: Float,
        leftY: Float,
        rightX: Float,
        rightY: Float,
        intensity: Float,
        columns: Int = 48,
        rows: Int = 48
    ): FloatArray {
        require(width > 0 && height > 0 && columns > 0 && rows > 0)
        val amount = intensity.coerceIn(0f, 1f)
        val mouthWidth = sqrt(
            (rightX - leftX) * (rightX - leftX) +
                (rightY - leftY) * (rightY - leftY)
        ).coerceAtLeast(1f)
        val radius = mouthWidth * 0.34f
        val lift = mouthWidth * 0.075f * amount
        val vertices = FloatArray((columns + 1) * (rows + 1) * 2)
        var index = 0
        for (row in 0..rows) {
            val y = height * row.toFloat() / rows
            for (column in 0..columns) {
                val x = width * column.toFloat() / columns
                fun weight(cx: Float, cy: Float): Float {
                    val dx = x - cx
                    val dy = y - cy
                    val distanceSquared = dx * dx + dy * dy
                    if (distanceSquared >= radius * radius) return 0f
                    return exp((-distanceSquared / (2f * radius * radius * 0.22f)).toDouble()).toFloat()
                }
                val leftWeight = weight(leftX, leftY)
                val rightWeight = weight(rightX, rightY)
                vertices[index++] = (x + (rightWeight - leftWeight) * lift * 0.22f)
                    .coerceIn(0f, width.toFloat())
                vertices[index++] = (y - (leftWeight + rightWeight) * lift)
                    .coerceIn(0f, height.toFloat())
            }
        }
        return vertices
    }
}
