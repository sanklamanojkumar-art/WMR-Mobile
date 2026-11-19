package com.wmrmobile.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoAdjustmentsTest {
    @Test
    fun neutralSettingsProduceIdentityMatrix() {
        val matrix = buildPhotoMatrix(0f, 1f, 1f, "Natural")
        assertEquals(20, matrix.size)
        val expected = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        expected.indices.forEach { assertEquals("matrix[$it]", expected[it], matrix[it], 0.0001f) }
    }

    @Test
    fun monoPresetUsesSameLumaWeightsForEveryOutputChannel() {
        val matrix = buildPhotoMatrix(0f, 1f, 1f, "Mono")
        assertEquals(matrix[0], matrix[5], 0.0001f)
        assertEquals(matrix[0], matrix[10], 0.0001f)
        assertEquals(matrix[1], matrix[6], 0.0001f)
        assertEquals(matrix[1], matrix[11], 0.0001f)
        assertEquals(matrix[2], matrix[7], 0.0001f)
        assertEquals(matrix[2], matrix[12], 0.0001f)
        assertEquals(0f, matrix[3], 0.0001f)
        assertEquals(0f, matrix[8], 0.0001f)
    }

    @Test
    fun warmAndCoolPresetsApplyOppositeColorTints() {
        val warm = buildPhotoMatrix(0f, 1f, 1f, "Warm")
        val cool = buildPhotoMatrix(0f, 1f, 1f, "Cool")
        assertEquals(8f, warm[4], 0.0001f)
        assertEquals(-8f, warm[14], 0.0001f)
        assertEquals(-8f, cool[4], 0.0001f)
        assertEquals(8f, cool[14], 0.0001f)
    }

    @Test
    fun allPresetsProduceFiniteMatrixValues() {
        listOf("Natural", "Vivid", "Warm", "Cool", "Mono", "Soft").forEach { preset ->
            val matrix = buildPhotoMatrix(12f, 1.2f, 0.8f, preset)
            assertEquals(20, matrix.size)
            assertTrue("$preset contains non-finite values", matrix.all { it.isFinite() })
        }
    }
}


class SmileWarpMathTest {
    @Test
    fun zeroIntensityKeepsEveryMeshVertexUnchanged() {
        val width = 1200
        val height = 900
        val vertices = SmileWarpMath.buildVertices(width, height, 450f, 450f, 750f, 450f, 0f)
        var index = 0
        for (row in 0..48) {
            val y = height * row.toFloat() / 48
            for (column in 0..48) {
                val x = width * column.toFloat() / 48
                assertEquals(x, vertices[index++], 0.0001f)
                assertEquals(y, vertices[index++], 0.0001f)
            }
        }
    }

    @Test
    fun smileWarpVerticesRemainFiniteAndInsideImageBounds() {
        val width = 4032
        val height = 3024
        val vertices = SmileWarpMath.buildVertices(width, height, 1650f, 1650f, 2380f, 1650f, 0.8f)
        assertTrue(vertices.all { it.isFinite() })
        for (i in vertices.indices step 2) {
            assertTrue("x coordinate outside image", vertices[i] in 0f..width.toFloat())
            assertTrue("y coordinate outside image", vertices[i + 1] in 0f..height.toFloat())
        }
    }

    @Test
    fun smileWarpDoesNotMoveMeshPointsOutsideMouthInfluenceRadius() {
        val width = 1200
        val height = 900
        val vertices = SmileWarpMath.buildVertices(width, height, 450f, 450f, 750f, 450f, 1f)
        var index = 0
        for (row in 0..48) {
            val y = height * row.toFloat() / 48
            for (column in 0..48) {
                val x = width * column.toFloat() / 48
                val outX = vertices[index++]
                val outY = vertices[index++]
                val nearLeft = kotlin.math.hypot((x - 450f).toDouble(), (y - 450f).toDouble()) < 102.1
                val nearRight = kotlin.math.hypot((x - 750f).toDouble(), (y - 450f).toDouble()) < 102.1
                if (!nearLeft && !nearRight) {
                    assertEquals(x, outX, 0.0001f)
                    assertEquals(y, outY, 0.0001f)
                }
            }
        }
    }
}
