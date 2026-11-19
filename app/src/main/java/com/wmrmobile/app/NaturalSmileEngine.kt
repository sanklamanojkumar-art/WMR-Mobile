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
    fun apply(source: Bitmap, mouth: MouthGeometry, intensity: Float): Bitmap {
        val amount = intensity.coerceIn(0f, 1f)
        require(amount > 0f) { "Increase smile intensity before applying." }
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val columns = 48
        val rows = 48
        val vertices = FloatArray((columns + 1) * (rows + 1) * 2)
        val mouthWidth = sqrt(
            (mouth.rightX - mouth.leftX) * (mouth.rightX - mouth.leftX) +
                (mouth.rightY - mouth.leftY) * (mouth.rightY - mouth.leftY)
        ).coerceAtLeast(1f)
        val radius = mouthWidth * 0.34f
        val lift = mouthWidth * 0.075f * amount
        var index = 0
        for (row in 0..rows) {
            val y = source.height * row.toFloat() / rows
            for (column in 0..columns) {
                val x = source.width * column.toFloat() / columns
                fun weight(cx: Float, cy: Float): Float {
                    val dx = x - cx
                    val dy = y - cy
                    val d2 = dx * dx + dy * dy
                    return if (d2 >= radius * radius) 0f
                    else exp((-d2 / (2f * radius * radius * 0.22f)).toDouble()).toFloat()
                }
                val wl = weight(mouth.leftX, mouth.leftY)
                val wr = weight(mouth.rightX, mouth.rightY)
                vertices[index++] = (x + (wr - wl) * lift * 0.22f).coerceIn(0f, source.width.toFloat())
                vertices[index++] = (y - (wl + wr) * lift).coerceIn(0f, source.height.toFloat())
            }
        }
        Canvas(output).drawBitmapMesh(
            source, columns, rows, vertices, 0, null, 0,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
        return output
    }
}
