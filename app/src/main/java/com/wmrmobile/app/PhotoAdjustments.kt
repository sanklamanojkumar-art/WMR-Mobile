package com.wmrmobile.app

/**
 * Creates the Android ColorMatrix values used by both live preview and export.
 * Kept free of Android UI dependencies so the photo math can be unit-tested.
 */
internal fun buildPhotoMatrix(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    preset: String
): FloatArray {
    val presetSaturation = when (preset) {
        "Vivid" -> 1.35f
        "Mono" -> 0f
        "Soft" -> 0.82f
        else -> 1f
    }
    val s = saturation * presetSaturation
    val c = contrast * if (preset == "Vivid") 1.08f else if (preset == "Soft") 0.96f else 1f
    val warm = when (preset) {
        "Warm" -> 8f
        "Cool" -> -8f
        else -> 0f
    }
    return floatArrayOf(
        c * (0.213f + 0.787f * s), c * (0.715f - 0.715f * s), c * (0.072f - 0.072f * s), 0f, brightness + warm,
        c * (0.213f - 0.213f * s), c * (0.715f + 0.285f * s), c * (0.072f - 0.072f * s), 0f, brightness,
        c * (0.213f - 0.213f * s), c * (0.715f - 0.715f * s), c * (0.072f + 0.928f * s), 0f, brightness - warm,
        0f, 0f, 0f, 1f, 0f
    )
}
