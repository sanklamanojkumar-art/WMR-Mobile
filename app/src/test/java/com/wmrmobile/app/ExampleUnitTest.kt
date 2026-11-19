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
