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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private fun decodeSelectedImage(uri: Uri, maxDimension: Int?): Bitmap {
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.isMutableRequired = false
                val width = info.size.width
                val height = info.size.height
                val longest = maxOf(width, height)
                if (maxDimension != null && longest > maxDimension) {
                    val scale = maxDimension.toFloat() / longest
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
        while (maxDimension != null && maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDimension) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("Could not decode the selected photo")
    }
}

@Composable
private fun WmrEditor(
    imageUri: Uri?,
    decodeImage: (Uri, Int?) -> Bitmap?,
    onChoosePhoto: () -> Unit,
    onClear: () -> Unit,
    onExport: (Bitmap) -> Unit
) {
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    var selectedFilter by remember { mutableStateOf("Natural") }
    var showOriginal by remember { mutableStateOf(false) }
    var smileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var smileIntensity by remember { mutableStateOf(0f) }
    var smileError by remember { mutableStateOf<String?>(null) }
    var smileBusy by remember { mutableStateOf(false) }
    var appliedSmileIntensity by remember { mutableStateOf(0f) }
    var exporting by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(imageUri) {
        loadError = null
        originalBitmap = if (imageUri == null) null else {
            runCatching {
                withContext(Dispatchers.IO) { decodeImage(imageUri, 1600) }
            }.onFailure { loadError = it.localizedMessage ?: "Unable to read this image" }
                .getOrNull()
        }
        brightness = 0f
        contrast = 1f
        saturation = 1f
        selectedFilter = "Natural"
        showOriginal = false
        smileBitmap = null
        smileIntensity = 0f
        appliedSmileIntensity = 0f
        smileError = null
        exportError = null
    }

    // Preview uses a GPU color filter; do not allocate a new full-size Bitmap
    // on every slider movement (that can exhaust memory and crash the app).
    val previewColorMatrix = remember(brightness, contrast, saturation, selectedFilter) {
        buildPreviewColorMatrix(brightness, contrast, saturation, selectedFilter)
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
                val preview = if (showOriginal) originalBitmap else (smileBitmap ?: originalBitmap)
                if (preview != null) {
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = if (showOriginal) "Original photo" else "Edited photo preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (showOriginal) null else ColorFilter.colorMatrix(previewColorMatrix)
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
                            selectedFilter = "Natural"
                            smileBitmap = null
                            appliedSmileIntensity = 0f
                            smileIntensity = 0f
                            smileError = null
                            showOriginal = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Reset") }
                }

                Text("NATURAL SMILE", color = Color.White, style = MaterialTheme.typography.titleSmall)
                Text("On-device face landmarks • localized mouth adjustment", color = Color(0xFFB8C0D4))
                Text("Smile intensity  ${(smileIntensity * 100).toInt()}%", color = Color.White)
                Slider(value = smileIntensity, onValueChange = { smileIntensity = it }, valueRange = 0f..1f)
                Button(
                    onClick = {
                        val source = originalBitmap ?: return@Button
                        smileBusy = true
                        smileError = null
                        val requestedIntensity = smileIntensity
                        scope.launch {
                            val result = runCatching {
                                withContext(Dispatchers.Default) {
                                    val geometry = NaturalSmileEngine.detect(context, source)
                                    NaturalSmileEngine.apply(source, geometry, requestedIntensity)
                                }
                            }
                            result.onSuccess { smileBitmap = it; appliedSmileIntensity = requestedIntensity }
                                .onFailure { smileError = it.localizedMessage ?: "Smile editing failed safely." }
                            smileBusy = false
                        }
                    },
                    enabled = !smileBusy && smileIntensity > 0f,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (smileBusy) "Analyzing face…" else "Apply Natural Smile") }
                if (smileBitmap != null) {
                    OutlinedButton(
                        onClick = { smileBitmap = null; appliedSmileIntensity = 0f },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Undo smile") }
                }
                smileError?.let { Text(it, color = Color(0xFFFFB4AB)) }

                Text("FILTERS", color = Color.White, style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Natural", "Vivid", "Warm", "Cool", "Mono", "Soft").forEach { preset ->
                        if (selectedFilter == preset) {
                            Button(onClick = { selectedFilter = preset }) { Text(preset) }
                        } else {
                            OutlinedButton(onClick = { selectedFilter = preset }) { Text(preset) }
                        }
                    }
                }

                Text("Brightness  ${brightness.toInt()}", color = Color.White)
                Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -80f..80f)
                Text("Contrast  ${(contrast * 100).toInt()}%", color = Color.White)
                Slider(value = contrast, onValueChange = { contrast = it }, valueRange = 0.5f..1.5f)
                Text("Saturation  ${(saturation * 100).toInt()}%", color = Color.White)
                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..2f)

                Button(
                    onClick = {
                        val uri = imageUri ?: return@Button
                        exporting = true
                        exportError = null
                        val exportBrightness = brightness
                        val exportContrast = contrast
                        val exportSaturation = saturation
                        val exportFilter = selectedFilter
                        val exportSmile = appliedSmileIntensity
                        scope.launch {
                            val result = runCatching {
                                withContext(Dispatchers.IO) {
                                    // The 1600px bitmap is preview-only; export re-reads the original.
                                    val fullResolution = decodeImage(uri, null)
                                        ?: error("Could not decode the original-resolution photo")
                                    val portraitAdjusted = if (exportSmile > 0f) {
                                        val geometry = NaturalSmileEngine.detect(context, fullResolution)
                                        NaturalSmileEngine.apply(fullResolution, geometry, exportSmile)
                                    } else fullResolution
                                    applyPhotoAdjustments(
                                        portraitAdjusted, exportBrightness, exportContrast,
                                        exportSaturation, exportFilter
                                    )
                                }
                            }
                            result.onSuccess(onExport)
                                .onFailure { exportError = it.localizedMessage ?: "Could not export full-resolution photo." }
                            exporting = false
                        }
                    },
                    enabled = originalBitmap != null && !showOriginal && !exporting,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (exporting) "Preparing full-resolution photo…" else "Save full-resolution photo") }

                exportError?.let { Text(it, color = Color(0xFFFFB4AB)) }
                Text(
                    "Export re-reads the original photo at its native dimensions. Your original file is never overwritten.",
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

private fun buildPreviewColorMatrix(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    preset: String
): ComposeColorMatrix {
    return ComposeColorMatrix(buildPhotoMatrix(brightness, contrast, saturation, preset))
}

private fun applyPhotoAdjustments(
    source: Bitmap,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    preset: String
): Bitmap {
    val matrix = ColorMatrix(buildPhotoMatrix(brightness, contrast, saturation, preset))

    val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(matrix)
    }
    Canvas(output).drawBitmap(source, 0f, 0f, paint)
    return output
}
