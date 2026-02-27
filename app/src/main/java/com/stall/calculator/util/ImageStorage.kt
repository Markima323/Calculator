package com.stall.calculator.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageStorage(private val context: Context) {
    private val imageDir: File by lazy {
        File(context.filesDir, "images").apply { mkdirs() }
    }

    private val cameraDir: File by lazy {
        File(context.cacheDir, "camera").apply { mkdirs() }
    }

    fun createCameraUri(): Uri {
        val file = File(cameraDir, "camera_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun persistFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val bitmap = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return@withContext null

        val square = centerCropSquare(bitmap)
        val scaled = if (square.width > 1024) {
            Bitmap.createScaledBitmap(square, 1024, 1024, true)
        } else {
            square
        }

        val target = File(imageDir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(target).use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 82, output)
        }

        if (scaled != square) {
            scaled.recycle()
        }
        if (square != bitmap) {
            square.recycle()
        }
        bitmap.recycle()
        target.absolutePath
    }

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    private fun centerCropSquare(source: Bitmap): Bitmap {
        val size = minOf(source.width, source.height)
        val left = (source.width - size) / 2
        val top = (source.height - size) / 2
        return Bitmap.createBitmap(source, left, top, size, size)
    }
}
