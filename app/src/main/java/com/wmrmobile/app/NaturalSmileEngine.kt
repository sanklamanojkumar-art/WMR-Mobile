package com.wmrmobile.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import kotlin.math.exp
import kotlin.math.sqrt

/** On-device face analysis and conservative mouth-corner warp. Source images are never mutated. */
internal object NaturalSmileEngine {
    data class MouthGeometry(val leftX: Float, val leftY: Float, val rightX: Float, val rightY: Float)

    fun detect(context: Context, bitmap: Bitmap): MouthGeometry {
        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("face_landmarker.task").build())
            .setRunningMode(RunningMode.IMAGE)
            .setNumFaces(2)
            .setOutputFaceBlendshapes(true)
            .build()
        val landmarker = FaceLandmarker.createFromOptions(context, options)
        try {
            val faces = landmarker.detect(BitmapImageBuilder(bitmap).build()).faceLandmarks()
            require(faces.size == 1) {
                if (faces.isEmpty()) "No face detected. Use a clear, front-facing portrait."
                else "Multiple faces detected. Crop to one person before applying Natural Smile."
            }
            val points = faces.first()
            require(points.size >= 292) { "Face landmarks are incomplete; edit cancelled." }
            // MediaPipe face-mesh mouth corner landmarks.
            val left = points[61]
            val right = points[291]
            val x1 = left.x() * bitmap.width
            val y1 = left.y() * bitmap.height
            val x2 = right.x() * bitmap.width
            val y2 = right.y() * bitmap.height
            val mouthWidth = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
            require(mouthWidth >= bitmap.width * 0.045f) { "Face is too small or mouth landmarks are uncertain." }
            require(listOf(x1, y1, x2, y2).all { it.isFinite() })
            return MouthGeometry(x1, y1, x2, y2)
        } finally {
            landmarker.close()
        }
    }

    /** Creates a new bitmap using a localized mesh warp around both mouth corners. */
    fun apply(
        source: Bitmap,
        mouth: MouthGeometry,
        intensity: Float,
        colorFilter: android.graphics.ColorFilter? = null
    ): Bitmap {
        val amount = intensity.coerceIn(0f, 1f)
        require(amount > 0f) { "Increase smile intensity before applying." }
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val columns = 48
        val rows = 48
        val vertices = SmileWarpMath.buildVertices(
            source.width, source.height,
            mouth.leftX, mouth.leftY, mouth.rightX, mouth.rightY,
            amount, columns, rows
        )
        Canvas(output).drawBitmapMesh(
            source, columns, rows, vertices, 0, null, 0,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                this.colorFilter = colorFilter
            }
        )
        return output
    }
}
