package com.wmrmobile.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var selectedImage by mutableStateOf<Uri?>(null)
    private var pendingExport: Bitmap? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImage = uri }

    private val exportPicker = registerForActivityResult(
        ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri ->
        if (uri != null) {
            val bitmap = pendingExport
            Thread {
                val saved = runCatching {
                    contentResolver.openOutputStream(uri)?.use { output ->
                        checkNotNull(bitmap) { "No edited image to save" }
                            .compress(Bitmap.CompressFormat.JPEG, 95, output)
                    } ?: error("Unable to open destination")
                }.getOrDefault(false)
                runOnUiThread {
                    Toast.makeText(
                        this,
                        if (saved) "Edited photo saved" else "Could not save photo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WmrEditor(
                    imageUri = selectedImage,
                    decodeImage = ::decodeSelectedImage,
                    onChoosePhoto = {
                        imagePicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onClear = { selectedImage = null },
                    onExport = { bitmap ->
                        pendingExport = bitmap
                        exportPicker.launch("WMR-Edited.jpg")
                    }
                )
            }
        }
    }

    private fun decodeSelectedImage(uri: Uri): Bitmap {
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.isMutableRequired = false
                val width = info.size.width
                val height = info.size.height
                val longest = maxOf(width, height)
                if (longest > 2048) {
                    val scale = 2048f / longest
                    decoder.setTargetSize(
                        (width * scale).toInt().coerceAtLeast(1),
                        (height * scale).toInt().coerceAtLeast(1)
                    )
                }
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: error("Could not open the selected photo")
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Unsupported or invalid image" }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2048) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("Could not decode the selected photo")
    }
}

@Composable
private fun WmrEditor(
    imageUri: Uri?,
    decodeImage: (Uri) -> Bitmap?,
    onChoosePhoto: () -> Unit,
    onClear: () -> Unit,
    onExport: (Bitmap) -> Unit
) {
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    var showOriginal by remember { mutableStateOf(false) }

    LaunchedEffect(imageUri) {
        loadError = null
        originalBitmap = if (imageUri == null) null else {
            runCatching {
                withContext(Dispatchers.IO) { decodeImage(imageUri) }
            }.onFailure { loadError = it.localizedMessage ?: "Unable to read this image" }
                .getOrNull()
        }
        brightness = 0f
        contrast = 1f
        saturation = 1f
        showOriginal = false
    }

    val editedBitmap by produceState<Bitmap?>(null, originalBitmap, brightness, contrast, saturation) {
        value = originalBitmap?.let { source ->
            withContext(Dispatchers.Default) {
                applyPhotoAdjustments(source, brightness, contrast, saturation)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF10131B)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("WMR MOBILE", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Your photos. Your control.", color = Color(0xFFB8C0D4))

            Surface(
                modifier = Modifier.fillMaxWidth().height(340.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1C2230)
            ) {
                val preview = if (showOriginal) originalBitmap else editedBitmap
                if (preview != null) {
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = if (showOriginal) "Original photo" else "Edited photo preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            when {
                                imageUri == null -> "No photo selected"
                                loadError != null -> "Couldn't load this photo"
                                originalBitmap == null -> "Loading photo…"
                                else -> "Preparing preview…"
                            },
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            loadError ?: "Choose another image if loading does not finish.",
                            color = Color(0xFFB8C0D4)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onChoosePhoto, modifier = Modifier.weight(1f)) { Text("Choose photo") }
                OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Clear") }
            }

            if (originalBitmap != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showOriginal = !showOriginal },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (showOriginal) "Show edited" else "Before / After") }
                    OutlinedButton(
                        onClick = {
                            brightness = 0f
                            contrast = 1f
                            saturation = 1f
                            showOriginal = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Reset") }
                }

                Text("Brightness  ${brightness.toInt()}", color = Color.White)
                Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -80f..80f)
                Text("Contrast  ${(contrast * 100).toInt()}%", color = Color.White)
                Slider(value = contrast, onValueChange = { contrast = it }, valueRange = 0.5f..1.5f)
                Text("Saturation  ${(saturation * 100).toInt()}%", color = Color.White)
                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..2f)

                Button(
                    onClick = { editedBitmap?.let(onExport) },
                    enabled = editedBitmap != null && !showOriginal,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save edited photo") }

                Text(
                    "Edits export to a new file. Your original photo is never overwritten.",
                    color = Color(0xFF9DA8BE),
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (imageUri == null) {
                Text("Select a photo to unlock editing controls.", color = Color(0xFF9DA8BE))
            } else if (loadError != null) {
                Text("Try choosing a JPG or PNG image.", color = Color(0xFFFFB4AB))
            }
        }
    }
}

private fun applyPhotoAdjustments(
    source: Bitmap,
    brightness: Float,
    contrast: Float,
    saturation: Float
): Bitmap {
    val matrix = ColorMatrix().apply { setSaturation(saturation) }
    matrix.postConcat(
        ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(matrix)
    }
    Canvas(output).drawBitmap(source, 0f, 0f, paint)
    return output
}
